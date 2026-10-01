/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kingsrook.qbits.workflows.execution;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qbits.workflows.model.WorkflowTestOutput;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestRunScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qbits.workflows.tracing.WorkflowTracerInterface;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionBiConsumer;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.model.tables.QQQTableTableManager;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 * class that knows how to execute a workflow in test mode against a set of
 * scenarios.
 *******************************************************************************/
public class WorkflowTester extends AbstractQActionBiConsumer<WorkflowTesterInput, WorkflowTesterOutput>
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowTester.class);



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void execute(WorkflowTesterInput input, WorkflowTesterOutput output) throws QException
   {
      QRecord      workflowRecord = input.getWorkflow();
      Workflow     workflow       = new Workflow(workflowRecord);
      WorkflowType workflowType   = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowType(workflow.getWorkflowTypeName());
      if(workflowType == null)
      {
         throw new QException("Workflow type not found by name: " + workflow.getWorkflowTypeName());
      }

      WorkflowTestRun workflowTestRun = new WorkflowTestRun();
      workflowTestRun.setWorkflowId(workflow.getId());
      if(input.getOverrideWorkflowRevision() == null)
      {
         workflowTestRun.setWorkflowRevisionId(workflow.getCurrentWorkflowRevisionId());
      }
      workflowTestRun.setScenarios(new ArrayList<>());

      int scenarioCount      = 0;
      int scenarioPassCount  = 0;
      int scenarioFailCount  = 0;
      int assertionCount     = 0;
      int assertionPassCount = 0;
      int assertionFailCount = 0;

      //////////////////////////////////////////
      // loop over the scenarios running each //
      //////////////////////////////////////////
      WorkflowTestStatus status = WorkflowTestStatus.PASS;
      for(QRecord workflowTestScenarioRecord : CollectionUtils.nonNullList(input.getWorkflowTestScenarioList()))
      {
         WorkflowTestRunScenario workflowTestRunScenario = runScenario(workflowTestScenarioRecord, input, workflowType);
         workflowTestRun.getScenarios().add(workflowTestRunScenario);

         /////////////////////////////////////////////////////////////////////
         // aggregate data from this run-scenario to the overall run object //
         /////////////////////////////////////////////////////////////////////
         assertionCount += Objects.requireNonNullElse(workflowTestRunScenario.getAssertionCount(), 0);
         assertionPassCount += Objects.requireNonNullElse(workflowTestRunScenario.getAssertionPassCount(), 0);
         assertionFailCount += Objects.requireNonNullElse(workflowTestRunScenario.getAssertionFailCount(), 0);

         scenarioCount++;
         if(WorkflowTestStatus.ERROR.getId().equals(workflowTestRunScenario.getStatus()))
         {
            status = WorkflowTestStatus.ERROR;
            scenarioFailCount++;
         }
         else if(WorkflowTestStatus.FAIL.getId().equals(workflowTestRunScenario.getStatus()) && status != WorkflowTestStatus.ERROR)
         {
            status = WorkflowTestStatus.FAIL;
            scenarioFailCount++;
         }
         else
         {
            scenarioPassCount++;
         }

      }

      workflowTestRun.setStatus(status.getId());
      workflowTestRun.setScenarioCount(scenarioCount);
      workflowTestRun.setScenarioPassCount(scenarioPassCount);
      workflowTestRun.setScenarioFailCount(scenarioFailCount);
      workflowTestRun.setAssertionCount(assertionCount);
      workflowTestRun.setAssertionPassCount(assertionPassCount);
      workflowTestRun.setAssertionFailCount(assertionFailCount);
      output.setWorkflowTestRun(workflowTestRun);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private WorkflowTestRunScenario runScenario(QRecord workflowTestScenarioRecord, WorkflowTesterInput workflowTesterInput, WorkflowType workflowType)
   {
      QRecord workflowRecord = workflowTesterInput.getWorkflow();

      WorkflowTestRunScenario workflowTestRunScenario = new WorkflowTestRunScenario();
      workflowTestRunScenario.setOutputs(new ArrayList<>());

      QBackendTransaction transaction = null;
      try
      {
         WorkflowTestScenario workflowTestScenario = new WorkflowTestScenario(workflowTestScenarioRecord);

         workflowTestRunScenario.setWorkflowTestScenarioId(workflowTestScenario.getId());
         workflowTestRunScenario.setWorkflowId(workflowTestScenario.getWorkflowId());

         WorkflowTypeTesterInterface workflowTypeTester = QCodeLoader.getAdHoc(WorkflowTypeTesterInterface.class, workflowType.getTester());
         WorkflowInput               workflowInput      = workflowTypeTester.setupWorkflowInputForTestScenario(workflowRecord, workflowTestScenarioRecord);

         WorkflowRunLog workflowRunLog = new WorkflowRunLog()
            .withInputRecordQqqTableId(QQQTableTableManager.getQQQTableId(QContext.getQInstance(), WorkflowTestScenario.TABLE_NAME))
            .withInputRecordId(workflowTestScenario.getId())
            .withInputDataJson("""
               {"tableName":"%s","id", %s}""".formatted(WorkflowTestScenario.TABLE_NAME, workflowTestScenario.getId()));

         ////////////////////////////////////////
         // actually execute the workflow here //
         ////////////////////////////////////////
         workflowInput.getWorkflowExecutionContext().setIsTestRun(true);
         workflowInput.setOverrideWorkflowRevision(workflowTesterInput.getOverrideWorkflowRevision());
         WorkflowOutput workflowOutput = new WorkflowOutput();

         transaction = workflowInput.getTransaction();

         new WorkflowExecutor()
            .withInputWorkflowRunLog(workflowRunLog)
            .withWorkflowTracer(getWorkflowTracer())
            .execute(workflowInput, workflowOutput);

         //////////////////////////////////
         // populate run-scenario object //
         //////////////////////////////////
         if(workflowOutput.getWorkflowRunLog() != null)
         {
            workflowTestRunScenario.setWorkflowRunLogId(workflowOutput.getWorkflowRunLog().getId());
            workflowTestRunScenario.setWorkflowRevisionId(workflowOutput.getWorkflowRunLog().getWorkflowRevisionId());
         }

         ///////////////////////////////////////////////////////////////////
         // put output data in the testRunScenario - and if that specific //
         // method fails, make output data with an exception message      //
         ///////////////////////////////////////////////////////////////////
         try
         {
            workflowTestRunScenario.setOutputData(workflowTypeTester.buildTestRunScenarioOutputData(workflowRecord, workflowOutput));
         }
         catch(Exception e)
         {
            LOG.warn("Error building workflow test scenario output", e, logPair("scenarioId", workflowTestScenario.getId()));
            workflowTestRunScenario.setOutputData(JsonUtils.toJson(Map.of("errorBuildingOutputData", e.getMessage())));
         }

         int assertionCount     = 0;
         int assertionPassCount = 0;
         int assertionFailCount = 0;

         if(workflowOutput.getException() != null)
         {
            //////////////////////
            // handle exception //
            //////////////////////
            workflowTestRunScenario.setStatus(WorkflowTestStatus.ERROR.getId());
            workflowTestRunScenario.setMessage("Error running workflow: " + workflowOutput.getException().getMessage());
         }
         else
         {
            ////////////////////////
            // process assertions //
            ////////////////////////
            List<QRecord> assertions = CollectionUtils.nonNullMap(workflowTestScenarioRecord.getAssociatedRecords()).get(WorkflowTestScenario.ASSOCIATION_NAME_ASSERTIONS);
            if(assertions == null)
            {
               if(workflowTestScenario.getId() != null)
               {
                  assertions = QueryAction.execute(WorkflowTestAssertion.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowTestScenarioId", QCriteriaOperator.EQUALS, workflowTestScenario.getId())));
               }
            }

            ///////////////////////////////////////////////////////
            // loop over assertions, checking each for pass/fail //
            ///////////////////////////////////////////////////////
            for(QRecord assertion : CollectionUtils.nonNullList(assertions))
            {
               assertionCount++;

               WorkflowTestOutput workflowTestOutput = workflowTypeTester.evaluateTestAssertion(assertion, workflowOutput);
               workflowTestOutput.setWorkflowTestAssertionId(assertion.getValueInteger("id"));
               workflowTestRunScenario.getOutputs().add(workflowTestOutput);

               if(WorkflowTestStatus.PASS.getId().equals(workflowTestOutput.getStatus()))
               {
                  assertionPassCount++;
               }
               else
               {
                  ////////////////////////////////////////////////////////////////////////////////////
                  // todo do we need to count errors separately here, or just lump them into fails? //
                  ////////////////////////////////////////////////////////////////////////////////////
                  assertionFailCount++;
               }
            }

            if(assertionFailCount > 0)
            {
               workflowTestRunScenario.setStatus(WorkflowTestStatus.FAIL.getId());
               workflowTestRunScenario.setMessage("Failed " + assertionFailCount + " of " + assertionCount + " assertion" + StringUtils.plural(assertionCount));
            }
            else
            {
               workflowTestRunScenario.setStatus(WorkflowTestStatus.PASS.getId());
               workflowTestRunScenario.setMessage("Passed " + assertionPassCount + " assertion" + StringUtils.plural(assertionPassCount));
            }
         }

         workflowTestRunScenario.setAssertionCount(assertionCount);
         workflowTestRunScenario.setAssertionPassCount(assertionPassCount);
         workflowTestRunScenario.setAssertionFailCount(assertionFailCount);
      }
      catch(Exception e)
      {
         LOG.info("Workflow test run failed with exception", e, logPair("workflowId", () -> workflowRecord.getValue("id")));
         workflowTestRunScenario.setStatus(WorkflowTestStatus.ERROR.getId());
         workflowTestRunScenario.setMessage("Error running workflow: " + e.getMessage());
      }
      finally
      {
         ////////////////////////////////////////////////////
         // make sure transactions don't leak out of here. //
         ////////////////////////////////////////////////////
         if(transaction != null)
         {
            transaction.close();
         }
      }

      return (workflowTestRunScenario);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static WorkflowTracerInterface getWorkflowTracer()
   {
      WorkflowTracerInterface workflowTracer = null;
      QBitConfig              sourceQBitConfig = QContext.getQInstance().getTable(WorkflowTestRun.TABLE_NAME).getSourceQBitConfig();
      if(sourceQBitConfig instanceof WorkflowsQBitConfig workflowsQBitConfig)
      {
         workflowTracer = QCodeLoader.getAdHoc(WorkflowTracerInterface.class, workflowsQBitConfig.getWorkflowTracerCodeReference());
      }
      return workflowTracer;
   }

}
