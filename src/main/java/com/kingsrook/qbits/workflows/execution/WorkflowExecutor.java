/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qbits.workflows.execution;


import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.model.WorkflowRunLogStep;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qbits.workflows.tracing.WorkflowTracerInterface;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionBiConsumer;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** class that executes a workflow.  evaluating its steps and navigating links.
 *******************************************************************************/
public class WorkflowExecutor extends AbstractQActionBiConsumer<WorkflowInput, WorkflowOutput>
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowExecutor.class);

   private WorkflowTracerInterface workflowTracer;
   private WorkflowRunLog          inputWorkflowRunLog;

   private Stack<Integer> containerStack = new Stack<>();
   private ExecutionPayload payload;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void execute(WorkflowInput workflowInput, WorkflowOutput workflowOutput) throws QException
   {
      /////////////////////////////////////////////////////////////////////////////////////////
      // get values map - initializing it if needed, and wrapping in modifiable ds if needed //
      /////////////////////////////////////////////////////////////////////////////////////////
      LinkedHashMap<String, Serializable> inputValues = CollectionUtils.useOrWrap(workflowInput.getValues(), new TypeToken<>() {});
      inputValues = Objects.requireNonNullElseGet(inputValues, () -> new LinkedHashMap<>());

      //////////////////////////////////
      // initialize execution context //
      //////////////////////////////////
      WorkflowExecutionContext context;
      if(workflowInput.getWorkflowExecutionContext() != null)
      {
         context = workflowInput.getWorkflowExecutionContext();
      }
      else
      {
         context = new WorkflowExecutionContext();
      }
      workflowOutput.setContext(context);

      //////////////////////////////////////////////////////////////////////
      // if the context didn't already have a values map, then create one //
      //////////////////////////////////////////////////////////////////////
      if(context.getValues() == null)
      {
         context.setValues(inputValues);
      }
      else
      {
         ////////////////////////////////////////////////////////
         // else, add all input values to the context's values //
         ////////////////////////////////////////////////////////
         context.getValues().putAll(inputValues);
      }

      ///////////////////////////
      // initialize trace list //
      ///////////////////////////
      WorkflowRunLog workflowRunLog = Objects.requireNonNullElseGet(inputWorkflowRunLog, () -> new WorkflowRunLog());
      workflowRunLog.setStartTimestamp(Instant.now());
      workflowRunLog.setWorkflowId(workflowInput.getWorkflowId());
      workflowOutput.setWorkflowRunLog(workflowRunLog);

      List<WorkflowRunLogStep> logStepList = new ArrayList<>();
      workflowRunLog.setSteps(logStepList);

      WorkflowTypeExecutorInterface workflowTypeExecutor = null;
      boolean                       weOwnTheTransaction  = false;

      try
      {
         ////////////////////////////////////////////////////////////
         // load the workflow (current revision), steps, and links //
         ////////////////////////////////////////////////////////////
         Workflow                           workflow         = getWorkflow(workflowInput.getWorkflowId());
         WorkflowRevision                   workflowRevision = getWorkflowRevision(workflowInput, workflow.getCurrentWorkflowRevisionId());
         Map<Integer, WorkflowStep>         stepMap          = loadSteps(workflowRevision);
         ListingHash<Integer, WorkflowLink> linkMap          = loadLinks(workflowRevision);

         context.setWorkflow(workflow);
         context.setWorkflowRevision(workflowRevision);
         workflowRunLog.setWorkflowRevisionId(workflowRevision.getId());

         ////////////////////////////////////////////
         // load type-executor, and do its pre-run //
         ////////////////////////////////////////////
         WorkflowType workflowType = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowType(workflow.getWorkflowTypeName());
         if(workflowType == null)
         {
            throw new QException("Workflow type not found by name: " + workflow.getWorkflowTypeName());
         }
         workflowTypeExecutor = QCodeLoader.getAdHoc(WorkflowTypeExecutorInterface.class, workflowType.getExecutor());
         workflowTypeExecutor.preRun(context, workflow, workflowRevision);

         if(workflowInput.getTransaction() != null)
         {
            context.setTransaction(workflowInput.getTransaction());
         }
         else
         {
            context.setTransaction(workflowTypeExecutor.openTransaction(workflow, workflowRevision));
            weOwnTheTransaction = true;
         }

         ///////////////
         // step loop //
         ///////////////
         ExecutionPayload payload = new ExecutionPayload(stepMap, linkMap, workflowTypeExecutor, new AtomicInteger(1), logStepList);
         this.payload = payload;
         context.setExecutor(this);
         runStepLoop(workflowRevision.getStartStepNo(), context, null);

         workflowTypeExecutor.postRun(context);

         if(weOwnTheTransaction && context.getTransaction() != null)
         {
            context.getTransaction().commit();
         }

         workflowRunLog.setHadError(false);
      }
      catch(Exception e)
      {
         LOG.info("Exception running workflow", e, logPair("workflowId", workflowInput.getWorkflowId()));

         if(workflowTypeExecutor != null)
         {
            workflowTypeExecutor.handleException(e, context);
         }

         if(weOwnTheTransaction && context.getTransaction() != null)
         {
            context.getTransaction().rollback();
         }

         workflowOutput.setException(e);
         workflowRunLog.setHadError(true);

         ////////////////////////////////////////////////////////////////////////////////////////////////////////
         // the executor may have set an error message in the run log - but if not, set one from the exception //
         ////////////////////////////////////////////////////////////////////////////////////////////////////////
         if(!StringUtils.hasContent(workflowRunLog.getErrorMessage()))
         {
            workflowRunLog.setErrorMessage(e.getMessage());
         }
      }
      finally
      {
         storeWorkflowRunLog(workflowRunLog);
         if(weOwnTheTransaction)
         {
            closeTransaction(context);
         }
      }
   }



   /***************************************************************************
    * @return true if the workflow should immediately stop
    ***************************************************************************/
   boolean runStepLoop(Integer stepNo, WorkflowExecutionContext context, Integer stopStepNo) throws QException
   {
      while(stepNo != null)
      {
         WorkflowStep step = payload.stepMap().get(stepNo);
         if(step == null)
         {
            throw new QException("Step not found by stepNo: " + stepNo);
         }

         WorkflowRunLogStep workflowRunLogStep = new WorkflowRunLogStep();
         workflowRunLogStep.setWorkflowStepId(step.getId());
         workflowRunLogStep.setStartTimestamp(Instant.now());

         WorkflowStepOutput workflowStepOutput = executeStep(step, payload, context);

         stepNo = getNextStepNo(workflowStepOutput.outputData(), step, payload.linkMap(), payload.stepMap(), false);

         workflowRunLogStep.setEndTimestamp(Instant.now());
         workflowRunLogStep.setOutputData(ValueUtils.getValueAsString(workflowStepOutput.outputData()));
         workflowRunLogStep.setMessage(workflowStepOutput.message());
         workflowRunLogStep.setSeqNo(payload.seqNo().getAndIncrement());
         payload.logStepList().add(workflowRunLogStep);

         if(stopStepNo != null && Objects.equals(stepNo, stopStepNo))
         {
            LOG.debug("Stopping at requested step", logPair("stopStepNo", stepNo));
            return (false);
         }

         if(workflowStepOutput.shouldWorkflowImmediatelyStop())
         {
            LOG.info("Stopping step loop due to step output indicating workflow should immediately stop", logPair("stepNo", stepNo));
            return (true);
         }
      }

      return (false);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void storeWorkflowRunLog(WorkflowRunLog workflowRunLog)
   {
      try
      {
         if(workflowTracer != null)
         {
            workflowRunLog.setEndTimestamp(Instant.now());
            workflowTracer.handleWorkflowFinish(workflowRunLog);
         }
      }
      catch(Exception e)
      {
         LOG.warn("Exception running workflow log tracer", e);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void closeTransaction(WorkflowExecutionContext context)
   {
      try
      {
         if(context.getTransaction() != null)
         {
            context.getTransaction().close();
         }
      }
      catch(Exception e)
      {
         LOG.warn("Exception closing transaction", e);
      }
   }



   /***************************************************************************
    * Normal steps (OutboundLinkMode.ONE):
    * - should just have 1 outbound link, with no conditionValue on it.
    *
    * Branch/Conditional steps (OutboundLinkMode.TWO):
    * - will have ... 2 (more in future?  seems supported in here) outbound links
    * - each with a conditionValue that represents the value that the stepOutput
    * of the fromStep itself will be tested for.  e.g., true & false for a boolean
    * conditional.  could be numbers or strings (in future) for a switch.
    *
    * Container steps (OutboundLinkMode.CONTAINER):
    * - will have up to 2 outbound links:
    * - one with a conditionValue of "push" going in to the 1st step
    * inside the container (unless it's empty, then this link doesn't exist).
    * - one with a condition value of "pop" going to the next step
    * after the container (unless the container is at the end of the program.
    * - the containerStack is used with a recursive call in here for popping.
    *
    * Interrupting / terminating steps (OutboundLinkMode.ZERO):
    * - will probably still have an outbound link (to help draw the graph) - but
    * this method ignores that link and returns null based on the link mode!
    ***************************************************************************/
   private Integer getNextStepNo(Serializable stepOutput, WorkflowStep fromStep, ListingHash<Integer, WorkflowLink> linkMap, Map<Integer, WorkflowStep> stepMap, boolean isPop) throws QException
   {
      WorkflowStepType fromWorkflowStepType = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowStepType(fromStep.getWorkflowStepTypeName());
      if(OutboundLinkMode.ZERO.equals(fromWorkflowStepType.getOutboundLinkMode()))
      {
         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         // graphs are stored with a link from a zero-outbound step to the step that comes after them in the UI, //
         // to allow it to distinguish between the code after the `if` closes being inside or outside the `if`.  //
         // SO - the point is - never return a next step for a from-step that is zero outbound-link mode.        //
         //////////////////////////////////////////////////////////////////////////////////////////////////////////
         return (null);
      }

      if(OutboundLinkMode.CONTAINER.equals(fromWorkflowStepType.getOutboundLinkMode()))
      {
         ///////////////////////////////////////////////////////////////////////////////////////////////////////
         // container steps have a "push${stepNo}" outbound link for pushing their contents onto a stack      //
         // (unless they're empty - then they'd just have a "pop" (unless they're at the end of the program)) //
         ///////////////////////////////////////////////////////////////////////////////////////////////////////
         if(isPop)
         {
            stepOutput = "pop";
         }
         else
         {
            stepOutput = "push";
            containerStack.push(fromStep.getStepNo());
         }
      }

      Integer            fromStepNo = fromStep.getStepNo();
      List<WorkflowLink> links      = linkMap.get(fromStepNo);

      if(OutboundLinkMode.VARIABLE.equals(fromWorkflowStepType.getOutboundLinkMode()))
      {
         /////////////////////////////////////////////////////////////////////////////
         // after a VARIABLE step (where its branches were all executed in          //
         // processMultiForkingStep), find the first join point, and step to there. //
         /////////////////////////////////////////////////////////////////////////////
         Integer joinStepNo = FindFirstJoinPoint.execute(links.stream().map(wl -> wl.getToStepNo()).toList(), stepMap, linkMap);
         return (joinStepNo);
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // look for a link between the fromStepNo matching the stepOutput / condition value //
      //////////////////////////////////////////////////////////////////////////////////////
      List<String> evaluatedConditionValues = new ArrayList<>();
      for(WorkflowLink link : CollectionUtils.nonNullList(links))
      {
         if(link.getConditionValue() == null)
         {
            ///////////////////////////////////////////////////////
            // a link w/o any condition means to always be taken //
            ///////////////////////////////////////////////////////
            return (link.getToStepNo());
         }
         else
         {
            evaluatedConditionValues.add(link.getConditionValue());
            try
            {
               Class<? extends Serializable> stepOutputClass = stepOutput == null ? null : stepOutput.getClass();
               Serializable valueAsType = stepOutputClass == null ? link.getConditionValue() : ValueUtils.getValueAsType(stepOutputClass, link.getConditionValue());
               if(Objects.equals(valueAsType, stepOutput))
               {
                  return link.getToStepNo();
               }
            }
            catch(Exception e)
            {
               LOG.debug("Unable to evaluate condition value: " + link.getConditionValue() + " for step: " + fromStepNo, e);
            }
         }
      }

      if(CollectionUtils.nullSafeHasContents(evaluatedConditionValues))
      {
         LOG.info("Evaluated at least 1 link condition value, but found no matches", logPair("evaluatedConditionValues", evaluatedConditionValues));
      }

      /////////////////////////////////////////////////////////////////////////
      // if we didn't find a next-step, but there is something on the stack, //
      // then look for a pop out of that frame                               //
      /////////////////////////////////////////////////////////////////////////
      if(!containerStack.isEmpty())
      {
         Integer      popStepNo = containerStack.pop();
         WorkflowStep popStep   = stepMap.get(popStepNo);

         return getNextStepNo(popStepNo, popStep, linkMap, stepMap, true);
      }

      return (null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowStepOutput executeStep(WorkflowStep step, ExecutionPayload payload, WorkflowExecutionContext context) throws QException
   {
      WorkflowStepType workflowStepType = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowStepType(step.getWorkflowStepTypeName());
      if(workflowStepType == null)
      {
         throw new QException("Workflow step type not found by name: " + step.getWorkflowStepTypeName());
      }

      if(OutboundLinkMode.CONTAINER.equals(workflowStepType.getOutboundLinkMode()))
      {
         return (new WorkflowStepOutput());
      }

      WorkflowStepExecutorInterface workflowStepExecutor = QCodeLoader.getAdHoc(WorkflowStepExecutorInterface.class, workflowStepType.getExecutor());
      payload.workflowTypeExecutor().preStep(step, context);

      JSONObject                jsonObject  = JsonUtils.toJSONObject(step.getInputValuesJson());
      Map<String, Object>       mapValues   = jsonObject.toMap();
      Map<String, Serializable> inputValues = new LinkedHashMap<>();

      for(Map.Entry<String, Object> entry : mapValues.entrySet())
      {
         if(entry.getValue() instanceof Serializable s)
         {
            inputValues.put(entry.getKey(), s);
         }
      }

      WorkflowStepOutput workflowStepOutput = workflowStepExecutor.execute(step, inputValues, context);
      workflowStepOutput = payload.workflowTypeExecutor().postStep(step, context, workflowStepOutput);

      return workflowStepOutput;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   ExecutionPayload getPayload()
   {
      return (this.payload);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Map<Integer, WorkflowStep> loadSteps(WorkflowRevision workflowRevision)
   {
      Map<Integer, WorkflowStep> steps = new HashMap<>();
      for(WorkflowStep workflowStep : CollectionUtils.nonNullList(workflowRevision.getSteps()))
      {
         steps.put(workflowStep.getStepNo(), workflowStep);
      }
      return steps;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private ListingHash<Integer, WorkflowLink> loadLinks(WorkflowRevision workflowRevision)
   {
      ListingHash<Integer, WorkflowLink> links = new ListingHash<>();
      for(WorkflowLink workflowLink : CollectionUtils.nonNullList(workflowRevision.getLinks()))
      {
         links.add(workflowLink.getFromStepNo(), workflowLink);
      }
      return links;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowRevision getWorkflowRevision(WorkflowInput workflowInput, Integer workflowRevisionId) throws QException
   {
      if(workflowInput.getOverrideWorkflowRevision() != null)
      {
         return new WorkflowRevision(workflowInput.getOverrideWorkflowRevision());
      }

      QRecord workflowRevision = new GetAction().executeForRecord(new GetInput(WorkflowRevision.TABLE_NAME)
         .withIncludeAssociations(true)
         .withPrimaryKey(workflowRevisionId));
      if(workflowRevision == null)
      {
         throw new QException("Workflow Revision not found by id: " + workflowRevisionId);
      }

      return (new WorkflowRevision(workflowRevision));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static Workflow getWorkflow(Integer workflowId) throws QException
   {
      QRecord workflow = new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId));
      if(workflow == null)
      {
         throw new QException("Workflow not found by id: " + workflowId);
      }
      return new Workflow(workflow);
   }



   /*******************************************************************************
    ** Getter for workflowTracer
    *******************************************************************************/
   public WorkflowTracerInterface getWorkflowTracer()
   {
      return (this.workflowTracer);
   }



   /*******************************************************************************
    ** Setter for workflowTracer
    *******************************************************************************/
   public void setWorkflowTracer(WorkflowTracerInterface workflowTracer)
   {
      this.workflowTracer = workflowTracer;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTracer
    *******************************************************************************/
   public WorkflowExecutor withWorkflowTracer(WorkflowTracerInterface workflowTracer)
   {
      this.workflowTracer = workflowTracer;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputWorkflowRunLog
    *******************************************************************************/
   public WorkflowRunLog getInputWorkflowRunLog()
   {
      return (this.inputWorkflowRunLog);
   }



   /*******************************************************************************
    ** Setter for inputWorkflowRunLog
    *******************************************************************************/
   public void setInputWorkflowRunLog(WorkflowRunLog inputWorkflowRunLog)
   {
      this.inputWorkflowRunLog = inputWorkflowRunLog;
   }



   /*******************************************************************************
    ** Fluent setter for inputWorkflowRunLog
    *******************************************************************************/
   public WorkflowExecutor withInputWorkflowRunLog(WorkflowRunLog inputWorkflowRunLog)
   {
      this.inputWorkflowRunLog = inputWorkflowRunLog;
      return (this);
   }

}
