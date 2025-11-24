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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRunLogStep;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/***************************************************************************
 * interface for workflow step executors that are of a variable output type,
 * and which can execute 0 or more of their outbound links.
 ***************************************************************************/
public interface WorkflowMultiForkingStepExecutorInterface extends WorkflowStepExecutorInterface
{
   QLogger LOG = QLogger.getLogger(WorkflowMultiForkingStepExecutorInterface.class);

   /***************************************************************************
    * the Execute method for such steps should perform an executor.runStepLoop
    * for each of the outbound links of the step (assuming they are supposed
    * allowed to run).
    ***************************************************************************/
   @Override
   default WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      ///////////////////////////////////////////////////////////////////////
      // get the various sub-workflows ("fork links") to run from the step //
      ///////////////////////////////////////////////////////////////////////
      WorkflowExecutor executor = context.getExecutor();
      ExecutionPayload payload  = executor.getPayload();

      List<WorkflowLink> forkLinks = payload.linkMap().get(step.getStepNo());
      preRun(step, inputValues, context);
      sortForks(forkLinks, step, inputValues, context);

      Integer stopStepNo                    = FindFirstJoinPoint.execute(forkLinks.stream().map(wl -> wl.getToStepNo()).toList(), payload.stepMap(), payload.linkMap());
      boolean shouldWorkflowImmediatelyStop = false;
      for(WorkflowLink link : forkLinks)
      {
         WorkflowRunLogStep workflowRunLogStep = new WorkflowRunLogStep();
         workflowRunLogStep.setWorkflowStepId(step.getId());
         workflowRunLogStep.setSeqNo(payload.seqNo().getAndIncrement());
         workflowRunLogStep.setStartTimestamp(Instant.now());

         WorkflowStepOutput workflowStepOutput = conditionallyExecuteFork(step, link, inputValues, context);
         if(Objects.equals(workflowStepOutput.outputData(), true))
         {
            shouldWorkflowImmediatelyStop = executor.runStepLoop(link.getToStepNo(), context, stopStepNo);
         }

         workflowRunLogStep.setOutputData(ValueUtils.getValueAsString(workflowStepOutput.outputData()));
         workflowRunLogStep.setMessage(workflowStepOutput.message());
         workflowRunLogStep.setEndTimestamp(Instant.now());
         payload.logStepList().add(workflowRunLogStep);

         if(shouldWorkflowImmediatelyStop)
         {
            LOG.info("Stopping fork-link loop, due to step output indicating workflow should immediately stop", logPair("linkStartStepNo", link.getToStepNo()));
            break;
         }
      }

      WorkflowStepOutput workflowStepOutput = makeWorkflowStepOutput(step, inputValues, context);
      postRun(step, inputValues, context);

      if(shouldWorkflowImmediatelyStop)
      {
         workflowStepOutput = new WorkflowStepOutput(workflowStepOutput.outputData(), workflowStepOutput.message(), true);
      }

      return workflowStepOutput;
   }

   /***************************************************************************
    * make sure the forks are executed in the order that the step expects.
    ***************************************************************************/
   void sortForks(List<WorkflowLink> forkLinks, WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context);

   /***************************************************************************
    * decide if a particular fork should be executed - and if so, then do so.
    ***************************************************************************/
   WorkflowStepOutput conditionallyExecuteFork(WorkflowStep step, WorkflowLink fork, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException;

   /***************************************************************************
    * make output for the entire step, e.g., taking into account all of its
    * forks.
    ***************************************************************************/
   WorkflowStepOutput makeWorkflowStepOutput(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException;

   /***************************************************************************
    * optional code to run before anything else in this interface
    ***************************************************************************/
   default void preRun(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {

   }

   /***************************************************************************
    * optional code to run after anything else in this interface
    ***************************************************************************/
   default void postRun(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {

   }
}
