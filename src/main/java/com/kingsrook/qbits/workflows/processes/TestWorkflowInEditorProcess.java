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


import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qbits.workflows.execution.WorkflowTester;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterInput;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterOutput;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;


/*******************************************************************************
 ** process to test a workflow within the editor.
 *******************************************************************************/
public class TestWorkflowInEditorProcess implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "testWorkflowInEditor";

   private static final QLogger LOG = QLogger.getLogger(TestWorkflowInEditorProcess.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override

   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withPermissionRules(new QPermissionRules().withLevel(PermissionLevel.NOT_PROTECTED))
         .withStep(new QBackendStepMetaData()
            .withName("execute")
            .withCode(new QCodeReference(getClass()))
            .withInputData(new QFunctionInputMetaData()
               .withField(new QFieldMetaData("workflowId", QFieldType.INTEGER))
               .withField(new QFieldMetaData("workflowTestScenarioId", QFieldType.INTEGER))
               .withField(new QFieldMetaData("apiName", QFieldType.STRING).withPossibleValueSourceName("apiName"))
               .withField(new QFieldMetaData("apiVersion", QFieldType.STRING).withPossibleValueSourceName("apiVersion"))
               .withField(new QFieldMetaData("steps", QFieldType.STRING)) // JSON
               .withField(new QFieldMetaData("links", QFieldType.STRING)) // JSON
            ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      Integer workflowId             = runBackendStepInput.getValueInteger("workflowId");
      Integer workflowTestScenarioId = runBackendStepInput.getValueInteger("workflowTestScenarioId");

      String        name = "All Scenarios";
      List<QRecord> workflowTestScenarioList;
      if(workflowTestScenarioId == null)
      {
         workflowTestScenarioList = QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))
            .withOrderBy(new QFilterOrderBy("id")));
         if(CollectionUtils.nullSafeIsEmpty(workflowTestScenarioList))
         {
            throw (new QUserFacingException("No test scenarios found for this workflow"));
         }

         runBackendStepOutput.addValue("scenarioNames", new HashMap<>(CollectionUtils.listToMap(workflowTestScenarioList, r -> r.getValue("id"), r -> r.getValue("name"))));
      }
      else
      {
         QRecord scenario = GetAction.execute(WorkflowTestScenario.TABLE_NAME, workflowTestScenarioId);
         if(scenario == null)
         {
            throw (new QUserFacingException("Could not find scenario with id: " + workflowTestScenarioId));
         }
         workflowTestScenarioList = List.of(scenario);
         name = scenario.getValueString("name");
      }
      runBackendStepOutput.addValue("name", name);

      QRecord workflow = GetAction.execute(Workflow.TABLE_NAME, workflowId);
      if(workflow == null)
      {
         throw (new QUserFacingException("Could not find workflow with id: " + workflowId));
      }

      List<QRecord> assertions = QueryAction.execute(WorkflowTestAssertion.TABLE_NAME,
         new QQueryFilter(new QFilterCriteria("workflowTestScenarioId", QCriteriaOperator.IN, workflowTestScenarioList.stream().map(r -> r.getValue("id")).toList())));
      runBackendStepOutput.addValue("assertionNames", new HashMap<>(CollectionUtils.listToMap(assertions, r -> r.getValue("id"), r -> r.getValue("name"))));

      WorkflowTesterInput input = new WorkflowTesterInput();
      input.setWorkflowTestScenarioList(workflowTestScenarioList);
      input.setOverrideWorkflowRevision(buildOverrideWorkflowRevision(runBackendStepInput));
      input.setWorkflow(workflow);
      WorkflowTesterOutput output = new WorkflowTesterOutput();
      new WorkflowTester().execute(input, output);

      QRecord insertedWorkflowTestRun = new InsertAction().execute(new InsertInput(WorkflowTestRun.TABLE_NAME).withRecordEntity(output.getWorkflowTestRun())).getRecords().get(0);

      runBackendStepOutput.addValue("status", WorkflowTestStatus.getById(output.getWorkflowTestRun().getStatus()));
      runBackendStepOutput.addValue("testRun", insertedWorkflowTestRun);

      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // todo - finish this when UI knows how to present them - but also - need to store stepNo in workflowRunLogStep too...      //
      // look up the runLogSteps that were recorded, to get the stepNos that should be highlighted in the UI, for each scenarioId //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      /*
      Map<Long, Integer> runLogIdToScenarioIdMap = CollectionUtils.listToMap(output.getWorkflowTestRun().getScenarios(), s -> s.getWorkflowRunLogId(), s -> s.getWorkflowTestScenarioId());
      List<QRecord>      workflowRunLogStepList  = QueryAction.execute(WorkflowRunLogStep.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowRunLogId", QCriteriaOperator.IN, runLogIdToScenarioIdMap.keySet())));

      ListingHash<Integer, Integer> scenarioIdToStepNoMap = new ListingHash<>();
      for(QRecord record : workflowRunLogStepList)
      {
         Long workflowRunLogId = record.getValueLong("workflowRunLogId");
         Integer scenarioId = runLogIdToScenarioIdMap.get(workflowRunLogId);
         scenarioIdToStepNoMap.add(scenarioId, record.getValueInteger("stepNo"));
      }
      runBackendStepOutput.addValue("scenarioIdToStepNoMap", scenarioIdToStepNoMap);
      */
   }



   /***************************************************************************
    * create a QRecord to represent the workflow (and its steps and links) that
    * will run instead of a stored revision.
    ***************************************************************************/
   private QRecord buildOverrideWorkflowRevision(RunBackendStepInput runBackendStepInput) throws QException
   {
      //////////////////////////////////////////////////
      // todo - any validation of the input workflow? //
      //////////////////////////////////////////////////
      try
      {
         List<WorkflowStep> workflowSteps = JsonUtils.toObject(runBackendStepInput.getValueString("steps"), new TypeReference<>() {});
         List<WorkflowLink> workflowLinks = JsonUtils.toObject(runBackendStepInput.getValueString("links"), new TypeReference<>() {});

         if(CollectionUtils.nullSafeIsEmpty(workflowSteps) || CollectionUtils.nullSafeIsEmpty(workflowLinks))
         {
            throw (new QUserFacingException("Workflow steps and/or links were empty"));
         }

         List<QRecord> stepRecords = workflowSteps.stream().map(ws -> ws.toQRecord()).toList();
         List<QRecord> linkRecords = workflowLinks.stream().map(wl -> wl.toQRecord()).toList();

         QRecord workflowRevision = new QRecord();
         workflowRevision.setValue("startStepNo", workflowSteps.get(0).getStepNo());
         workflowRevision.setValue("apiName", runBackendStepInput.getValueString("apiName"));
         workflowRevision.setValue("apiVersion", runBackendStepInput.getValueString("apiVersion"));
         workflowRevision.withAssociatedRecords(WorkflowRevision.ASSOCIATION_NAME_WORKFLOW_STEP, stepRecords);
         workflowRevision.withAssociatedRecords(WorkflowRevision.ASSOCIATION_NAME_WORKFLOW_LINK, linkRecords);
         return (workflowRevision);
      }
      catch(QException e)
      {
         throw (e);
      }
      catch(Exception e)
      {
         throw (new QException("Error building workflow revision for testing", e));
      }
   }

}
