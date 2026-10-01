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

package com.kingsrook.qbits.workflows.processes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qbits.workflows.execution.WorkflowTester;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterInput;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterOutput;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryFilterLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryRecordLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.BackendStepPostRunInput;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.BackendStepPostRunOutput;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** run the test scenarios and build workflow run records
 *******************************************************************************/
public class RunWorkflowTestScenarioLoadStep extends AbstractLoadStep implements ProcessSummaryProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(RunWorkflowTestScenarioLoadStep.class);

   private ListingHash<Integer, Integer> workflowIdToScenarioId = new ListingHash<>();
   private List<Integer>                 insertedTestRunIds;
   private List<Integer>                 workflowIdsThatHadErrors = new ArrayList<>();

   private List<ProcessSummaryRecordLink> testRunLinks = new ArrayList<>();

   private boolean didPostRun = false;



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      /////////////////////////////////////////////////////////////
      // just put record ids into the map for post-run to handle //
      /////////////////////////////////////////////////////////////
      for(WorkflowTestScenario workflowTestScenario : runBackendStepInput.getRecordsAsEntities(WorkflowTestScenario.class))
      {
         workflowIdToScenarioId.add(workflowTestScenario.getWorkflowId(), workflowTestScenario.getId());
         runBackendStepOutput.addRecordEntity(workflowTestScenario);
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void postRun(BackendStepPostRunInput runBackendStepInput, BackendStepPostRunOutput runBackendStepOutput) throws QException
   {
      didPostRun = true;

      Map<Integer, String> workflowNameByIdMap = new HashMap<>();
      if(!workflowIdToScenarioId.isEmpty())
      {
         int i = 0;

         List<QRecord> recordsToInsert = new ArrayList<>();
         for(Map.Entry<Integer, List<Integer>> entry : workflowIdToScenarioId.entrySet())
         {
            Integer       workflowId  = entry.getKey();
            List<Integer> scenarioIds = entry.getValue();

            runBackendStepInput.getAsyncJobCallback().updateStatus("Running workflow test", ++i, workflowIdToScenarioId.size());
            try
            {
               QRecord       workflow  = GetAction.execute(Workflow.TABLE_NAME, workflowId);
               List<QRecord> scenarios = QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, scenarioIds)));

               workflowNameByIdMap.put(workflowId, workflow.getValueString("name"));

               WorkflowTesterInput input = new WorkflowTesterInput();
               input.setWorkflow(workflow);
               input.setWorkflowTestScenarioList(scenarios);

               WorkflowTesterOutput output = new WorkflowTesterOutput();
               new WorkflowTester().execute(input, output);

               recordsToInsert.add(output.getWorkflowTestRun().toQRecord());
            }
            catch(Exception e)
            {
               workflowIdsThatHadErrors.add(workflowId);
               LOG.warn("Error running a workflow test", e, logPair("workflowId", workflowId));
            }
         }

         List<QRecord> insertedTestRuns = new InsertAction().execute(new InsertInput(WorkflowTestRun.TABLE_NAME).withRecords(recordsToInsert)).getRecords();
         insertedTestRunIds = insertedTestRuns.stream().map(r -> r.getValueInteger("id")).filter(Objects::nonNull).toList();

         if(insertedTestRuns.size() < 50)
         {
            makeTestRunLinks(insertedTestRuns, workflowNameByIdMap, testRunLinks);
         }
      }

      /////////////////////////////////////////////////////////////////////
      // now that we've done the post-run, we can do the process summary //
      // normally doGetProcessSummary runs before postRun...             //
      /////////////////////////////////////////////////////////////////////
      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_PROCESS_SUMMARY, doGetProcessSummary(runBackendStepOutput, true));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   static void makeTestRunLinks(List<QRecord> insertedTestRuns, Map<Integer, String> workflowNameByIdMap, List<ProcessSummaryRecordLink> testRunLinks)
   {
      for(QRecord insertedTestRun : insertedTestRuns)
      {
         Status linkStatus;
         String linkText = "Workflow Test Run (" + insertedTestRun.getValue("id") + ") for " + workflowNameByIdMap.get(insertedTestRun.getValueInteger("workflowId"));
         if(WorkflowTestStatus.PASS.getId().equals(insertedTestRun.getValue("status")))
         {
            linkStatus = Status.OK;
            linkText += " has status: Pass";
         }
         else if(WorkflowTestStatus.FAIL.getId().equals(insertedTestRun.getValue("status")))
         {
            linkStatus = Status.ERROR;
            linkText += " has status: Fail";
         }
         else
         {
            linkStatus = Status.WARNING;
            linkText += " has status: Error";
         }

         testRunLinks.add(new ProcessSummaryRecordLink(linkStatus, WorkflowTestRun.TABLE_NAME, insertedTestRun.getValueInteger("id"), linkText));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean b)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();

      if(didPostRun)
      {
         addWorkflowTestRunStatusLines(rs, insertedTestRunIds, workflowIdsThatHadErrors, testRunLinks);
      }

      return rs;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   static void addWorkflowTestRunStatusLines(ArrayList<ProcessSummaryLineInterface> rs, List<Integer> insertedTestRunIds, List<Integer> workflowIdsThatHadErrors, List<ProcessSummaryRecordLink> testRunLinks)
   {
      if(CollectionUtils.nullSafeHasContents(insertedTestRunIds))
      {
         QQueryFilter insertedTestRunsFilter = new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, insertedTestRunIds));
         String       linkText               = insertedTestRunIds.size() + " Workflow Test " + StringUtils.plural(insertedTestRunIds, "Run was created", "Runs were created");
         rs.add(new ProcessSummaryFilterLink(Status.INFO, WorkflowTestRun.TABLE_NAME, insertedTestRunsFilter, linkText));
      }

      if(CollectionUtils.nullSafeHasContents(workflowIdsThatHadErrors))
      {
         QQueryFilter workflowsWithErrorsFilter = new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, workflowIdsThatHadErrors));
         String       linkText                  = workflowIdsThatHadErrors.size() + " Workflow" + StringUtils.plural(workflowIdsThatHadErrors) + " failed to produce a complete Test Run.";
         rs.add(new ProcessSummaryFilterLink(Status.ERROR, Workflow.TABLE_NAME, workflowsWithErrorsFilter, linkText));
      }

      rs.addAll(testRunLinks);
   }
}
