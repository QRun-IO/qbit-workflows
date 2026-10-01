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
import java.util.List;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;


/*******************************************************************************
 ** validate if there are test scenarios for selected workflows
 *******************************************************************************/
public class RunTestScenariosForWorkflowTransformStep extends AbstractTransformStep
{
   private static final QLogger LOG = QLogger.getLogger(RunTestScenariosForWorkflowTransformStep.class);

   private ProcessSummaryLine okLine = new ProcessSummaryLine(Status.OK)
      .withMessageSuffix(" scenarios run.")
      .withSingularFutureMessage("will have its")
      .withPluralFutureMessage("will have their")
      .withSingularPastMessage("had its")
      .withPluralPastMessage("had their");

   private ProcessSummaryLine noScenariosLine = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix(" not have any test scenarios.")
      .withSingularFutureMessage("does")
      .withPluralFutureMessage("do not")
      .withSingularPastMessage("did not")
      .withPluralPastMessage("did not");

   private ProcessSummaryLine noRevisionsLine = new ProcessSummaryLine(Status.ERROR)
      .withMessageSuffix(" not have any revisions.")
      .withSingularFutureMessage("does")
      .withPluralFutureMessage("do not")
      .withSingularPastMessage("did not")
      .withPluralPastMessage("did not");



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> rs = new ArrayList<>();
      okLine.addSelfToListIfAnyCount(rs);
      noScenariosLine.addSelfToListIfAnyCount(rs);
      noRevisionsLine.addSelfToListIfAnyCount(rs);
      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      ListingHash<Integer, QRecord> testScenariosPerWorkflow = getTestScenariosPerWorkflow(runBackendStepInput);
      if(CollectionUtils.nullSafeIsEmpty(testScenariosPerWorkflow))
      {
         return;
      }

      for(QRecord workflow : runBackendStepInput.getRecords())
      {
         Integer workflowId = workflow.getValueInteger("id");
         if(workflow.getValue("currentWorkflowRevisionId") == null)
         {
            noRevisionsLine.incrementCountAndAddPrimaryKey(workflowId);
         }
         else if(CollectionUtils.nullSafeIsEmpty(testScenariosPerWorkflow.get(workflowId)))
         {
            noScenariosLine.incrementCountAndAddPrimaryKey(workflowId);
         }
         else
         {
            okLine.incrementCountAndAddPrimaryKey(workflowId);
            runBackendStepOutput.addRecord(workflow);
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   static ListingHash<Integer, QRecord> getTestScenariosPerWorkflow(RunBackendStepInput runBackendStepInput) throws QException
   {
      List<Integer> workflowIds = runBackendStepInput.getRecords().stream().map(r -> r.getValueInteger("id")).toList();
      if(CollectionUtils.nullSafeIsEmpty(workflowIds))
      {
         return null;
      }

      ListingHash<Integer, QRecord> testScenariosPerWorkflow = CollectionUtils.listToListingHash(
         QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.IN, workflowIds))),
         record -> record.getValueInteger("workflowId"));

      return (testScenariosPerWorkflow);
   }

}
