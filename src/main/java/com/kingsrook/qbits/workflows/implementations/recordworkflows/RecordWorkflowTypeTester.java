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

package com.kingsrook.qbits.workflows.implementations.recordworkflows;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qbits.workflows.execution.WorkflowInput;
import com.kingsrook.qbits.workflows.execution.WorkflowOutput;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeTesterInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowTestOutput;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithJoinedRecords;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAndJoinTable;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 * Workflow-type tester used for record workflows.
 *******************************************************************************/
public class RecordWorkflowTypeTester implements WorkflowTypeTesterInterface
{

   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public WorkflowInput setupWorkflowInputForTestScenario(QRecord workflow, QRecord scenario) throws QException
   {
      WorkflowInput workflowInput = WorkflowTypeTesterInterface.super.setupWorkflowInputForTestScenario(workflow, scenario);

      RecordWorkflowContext recordWorkflowContext = new RecordWorkflowContext();
      workflowInput.setWorkflowExecutionContext(recordWorkflowContext);

      String  tableName   = workflow.getValueString("tableName");
      QRecord inputRecord = makeRecordFromTestScenario(scenario, tableName);

      recordWorkflowContext.setWorkflow(new Workflow(workflow));
      recordWorkflowContext.record.set(inputRecord);

      putAssociatedRecordsFromInputRecordIntoWorkflowContext(inputRecord, tableName, recordWorkflowContext);

      workflowInput.setTransaction(QBackendTransaction.openFor(new InsertInput(tableName)));
      return workflowInput;
   }



   /***************************************************************************
    * for an input record that came from API, in case it has associations in it,
    * put those associated records into the workflow context (because otherwise
    * those records won't be able to be looked up in the database later, because
    * they aren't in there!)
    ***************************************************************************/
   private void putAssociatedRecordsFromInputRecordIntoWorkflowContext(QRecord inputRecord, String tableName, RecordWorkflowContext recordWorkflowContext)
   {
      int generatedId = -1;
      for(Map.Entry<String, List<QRecord>> entry : CollectionUtils.nonNullMap(inputRecord.getAssociatedRecords()).entrySet())
      {
         String                associationName   = entry.getKey();
         List<QRecord>         associatedRecords = entry.getValue();
         Optional<Association> association       = QContext.getQInstance().getTable(tableName).getAssociationByName(associationName);
         if(association.isPresent())
         {
            String associatedTableName       = association.get().getAssociatedTableName();
            String associatedPrimaryKeyField = QContext.getQInstance().getTable(associatedTableName).getPrimaryKeyField();
            for(QRecord associatedRecord : associatedRecords)
            {
               if(associatedRecord.getValue(associatedPrimaryKeyField) == null)
               {
                  associatedRecord.setValue(associatedPrimaryKeyField, generatedId--);
               }
            }

            QueryJoin queryJoin = new QueryJoin()
               .withJoinTable(associatedTableName)
               .withBaseTableOrAlias(tableName)
               .withJoinMetaData(QContext.getQInstance().getJoin(association.get().getJoinName()));

            recordWorkflowContext.setJoinRecords(queryJoin, associatedRecords);
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public String buildTestRunScenarioOutputData(QRecord workflowRecord, WorkflowOutput workflowOutput) throws QException
   {
      QRecord          record           = (QRecord) workflowOutput.getContext().getValues().get("record");
      WorkflowRevision workflowRevision = workflowOutput.getContext().getWorkflowRevision();
      String           tableName        = workflowRecord.getValueString("tableName");
      String           apiName          = workflowRevision.getApiName();
      String           apiVersion       = workflowRevision.getApiVersion();

      Map<String, Object> outputData = new HashMap<>();

      if(WorkflowsQBitConfig.isApiModuleAvailableAndDoesQBitIncludeApiVersions() && StringUtils.hasContent(apiName) && StringUtils.hasContent(apiVersion))
      {
         outputData.put("record", QRecordApiAdapter.qRecordToApiMap(record, tableName, apiName, apiVersion));
      }
      else
      {
         outputData.put("record", record.getValues());
      }

      return JsonUtils.toJsonCustomized(outputData, builder -> builder.serializationInclusion(JsonInclude.Include.ALWAYS));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public Serializable getValueForTestAssertion(WorkflowOutput workflowOutput, QRecord assertion) throws QException
   {
      RecordWorkflowContext context = (RecordWorkflowContext) workflowOutput.getContext();
      QRecord               record  = context.record.get();

      String            variableName      = assertion.getValueString("variableName");
      String            tableName         = workflowOutput.getContext().getWorkflow().getTableName();
      QTableMetaData    table             = QContext.getQInstance().getTable(tableName);
      FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, variableName);
      if(fieldAndJoinTable.joinTable().getName().equals(tableName))
      {
         return (record.getValueString(fieldAndJoinTable.field().getName()));
      }
      else
      {
         ////////////////////////////////////////////////////////////////////////////////////////
         // if the field is from a different table, get list of values from associated records //
         ////////////////////////////////////////////////////////////////////////////////////////
         for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
         {
            if(association.getAssociatedTableName().equals(fieldAndJoinTable.joinTable().getName()))
            {
               List<QRecord> associatedRecords = CollectionUtils.nonNullMap(record.getAssociatedRecords()).get(association.getName());
               return (new ArrayList<>(associatedRecords.stream().map(li -> li.getValueString(fieldAndJoinTable.field().getName())).toList()));
            }
         }
      }

      return (record.getValueString(assertion.getValueString("variableName")));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public boolean doesFilterMatch(WorkflowTestOutput workflowTestOutput, WorkflowOutput workflowOutput, QRecord assertion, String queryFilterJson) throws QException, IOException
   {
      RecordWorkflowContext context = (RecordWorkflowContext) workflowOutput.getContext();

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // reset the pre-looked up joins in this object (since after the workflow is complete, records //
      // will be stored, which wouldn't have been before, so it might have cached old record lists.) //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      context.recordWorkflowContextJoinedRecordHelper.get().reset();

      QRecord record = getRecordForTestAssertionFilter(workflowOutput, assertion);
      if(record == null)
      {
         setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.ERROR, "Could not find record to apply filter assertion");
      }

      QQueryFilter filter = JsonUtils.toObject(queryFilterJson, QQueryFilter.class);

      //////////////////////////////////////////////////////////////////////////////////////
      // re-use the order filtering logic of InputRecordFilterStep (e.g., building joins) //
      //////////////////////////////////////////////////////////////////////////////////////
      InputRecordFilterStep          inputRecordFilterStep  = new InputRecordFilterStep();
      List<QRecordWithJoinedRecords> orderWithJoinedRecords = InputRecordFilterStep.buildCrossProduct(record, filter, context.recordWorkflowContextJoinedRecordHelper.get());
      WorkflowStepOutput             workflowStepOutput     = inputRecordFilterStep.evaluateCrossProduct(orderWithJoinedRecords, filter);
      return (Objects.equals(workflowStepOutput.outputData(), true));

   }
}
