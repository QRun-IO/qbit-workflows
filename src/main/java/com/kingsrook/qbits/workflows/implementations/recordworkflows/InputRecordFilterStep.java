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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.OutboundLinkOption;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.execution.WorkflowStepValidatorInterface;
import com.kingsrook.qbits.workflows.implementations.WorkflowStepUtils;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.utils.ApiQueryFilterUtils;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.CriteriaOption;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordWithJoinedRecords;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.utils.BackendQueryFilterUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 ** workflow step that compares the input record to a filter
 *******************************************************************************/
public class InputRecordFilterStep extends WorkflowStepType implements WorkflowStepExecutorInterface, WorkflowStepValidatorInterface
{
   private static final QLogger LOG = QLogger.getLogger(InputRecordFilterStep.class);

   public static final String NAME = "inputRecordFilter";



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public InputRecordFilterStep()
   {
      this.withName(NAME)
         .withOutboundLinkMode(OutboundLinkMode.TWO)
         .withOutboundLinkOptions(List.of(
            new OutboundLinkOption().withValue("true").withLabel("Then"),
            new OutboundLinkOption().withValue("false").withLabel("Otherwise")
         ))
         .withLabel("If Record Matches Filter")
         .withIconUrl("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0Ij4KICAgPHBhdGggZD0iTTQuMjUgNS42MUM2LjI3IDguMiAxMCAxMyAxMCAxM3Y2YzAgLjU1LjQ1IDEgMSAxaDJjLjU1IDAgMS0uNDUgMS0xdi02czMuNzItNC44IDUuNzQtNy4zOWMuNTEtLjY2LjA0LTEuNjEtLjc5LTEuNjFINS4wNGMtLjgzIDAtMS4zLjk1LS43OSAxLjYxeiIvPgo8L3N2Zz4K")
         .withExecutor(new QCodeReference(getClass()))
         .withValidator(new QCodeReference(getClass()))
         .withDescription("Choose a different set of actions based on if the record being processed matches a filter")
         .withInputWidgetNames(List.of(RecordWorkflowInputRecordFilterWidget.NAME));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getDynamicStepSummary(Integer workflowId, Map<String, Serializable> inputValues) throws QException
   {
      QRecord workflowRecord = GetAction.execute(Workflow.TABLE_NAME, workflowId);
      String  tableName      = workflowRecord.getValueString("tableName");
      return (RecordWorkflowUtils.getDynamicStepSummaryForFilter(tableName, inputValues));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext context) throws QException
   {
      QRecord record = (QRecord) context.getValues().get("record");
      if(record == null)
      {
         throw (new QException("Missing record input in InputRecordFilterStep"));
      }

      QQueryFilter filter = RecordWorkflowUtils.getFilterFromInput(inputValues);
      if(filter == null)
      {
         throw (new QException("Missing filter input in InputRecordFilterStep"));
      }

      if(WorkflowStepUtils.useApi(context.getWorkflowRevision()))
      {
         RecordWorkflowUtils.updateFilterForApi(context, filter);
      }

      ///////////////////////////////////////////////////////////////////////////////
      // todo unclear if this should always happen or if it should be configurable //
      ///////////////////////////////////////////////////////////////////////////////
      filter.applyCriteriaOptionToAllCriteria(CriteriaOption.CASE_INSENSITIVE);

      List<QRecordWithJoinedRecords> recordWithJoins = buildCrossProduct(record, filter, context);
      return evaluateCrossProduct(recordWithJoins, filter);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public List<QRecordWithJoinedRecords> buildCrossProduct(QRecord record, QQueryFilter filter, WorkflowExecutionContext workflowExecutionContext) throws QException
   {
      RecordWorkflowContext context = (RecordWorkflowContext) workflowExecutionContext;

      List<QRecordWithJoinedRecords> crossProduct = new ArrayList<>();
      crossProduct.add(new QRecordWithJoinedRecords(record));

      List<QueryJoin> joinsInFilter = BackendQueryFilterUtils.identifyJoinsInFilter(context.getWorkflow().getTableName(), filter);
      for(QueryJoin join : joinsInFilter)
      {
         crossProduct = expandCrossProductViaJoin(crossProduct, join, context);
      }

      return crossProduct;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static List<QRecordWithJoinedRecords> expandCrossProductViaJoin(List<QRecordWithJoinedRecords> orderWithJoinedRecords, QueryJoin queryJoin, RecordWorkflowContext context) throws QException
   {
      String                joinTableName           = queryJoin.getJoinTable();
      ArrayList<QRecord>    recordsToBeInserted     = context.recordsToInsert.get().computeIfAbsent(joinTableName, k -> new ArrayList<>());
      List<QRecord>         recordsAlreadyInBackend = context.getJoinRecords(queryJoin);
      HashSet<Serializable> idsToDelete             = context.primaryKeysToDelete.get().computeIfAbsent(joinTableName, k -> new HashSet<>());
      String                primaryKeyField         = QContext.getQInstance().getTable(joinTableName).getPrimaryKeyField();

      //////////////////////////////////////////////////////////////////////////////////////////////////////
      // add records that already existed to the cross product, filtering out ones that are to be deleted //
      //////////////////////////////////////////////////////////////////////////////////////////////////////
      List<QRecord> recordsToCross = new ArrayList<>();
      for(QRecord record : CollectionUtils.nonNullList(recordsAlreadyInBackend))
      {
         if(!idsToDelete.contains(record.getValue(primaryKeyField)))
         {
            recordsToCross.add(record);
         }
      }

      ////////////////////////////////////
      // add any records to be inserted //
      ////////////////////////////////////
      CollectionUtils.addAllIfNotNull(recordsToCross, recordsToBeInserted);

      orderWithJoinedRecords = makeCrossProduct(orderWithJoinedRecords, joinTableName, recordsToCross);
      return orderWithJoinedRecords;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static List<QRecordWithJoinedRecords> makeCrossProduct(List<QRecordWithJoinedRecords> orderWithJoinedRecords, String joinTableName, List<QRecord> joinRecodsToCross)
   {
      if(!joinRecodsToCross.isEmpty())
      {
         List<QRecordWithJoinedRecords> newCrossProduct = new ArrayList<>();
         for(QRecordWithJoinedRecords orderWithJoinedRecord : orderWithJoinedRecords)
         {
            newCrossProduct.addAll(orderWithJoinedRecord.buildCrossProduct(joinTableName, joinRecodsToCross));
         }
         orderWithJoinedRecords = newCrossProduct;
      }
      return orderWithJoinedRecords;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public WorkflowStepOutput evaluateCrossProduct(List<QRecordWithJoinedRecords> crossProduct, QQueryFilter filter)
   {
      for(QRecordWithJoinedRecords record : crossProduct)
      {
         if(BackendQueryFilterUtils.doesRecordMatch(filter, record))
         {
            return new WorkflowStepOutput(true);
         }
      }

      return new WorkflowStepOutput(false);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void validate(WorkflowStep step, Map<String, Serializable> inputValues, QRecord workflowRevision, QRecord workflow, List<String> errors) throws QException
   {
      if(WorkflowStepUtils.useApi(new WorkflowRevision(workflowRevision)))
      {
         QQueryFilter filter = null;
         try
         {
            filter = RecordWorkflowUtils.getFilterFromInput(inputValues);
         }
         catch(Exception e)
         {
            //////////////////////////////////////////////////////////////////////////////////
            // let's assume if we can't find the filter, that it isn't invalid - just empty //
            //////////////////////////////////////////////////////////////////////////////////
            return;
         }

         if(filter != null)
         {
            String                      apiName            = workflowRevision.getValueString("apiName");
            String                      apiVersion         = workflowRevision.getValueString("apiVersion");
            String                      tableName          = workflow.getValueString("tableName");
            Map<String, QFieldMetaData> tableApiFields     = GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsInput().withApiName(apiName).withVersion(apiVersion).withTableName(tableName));
            ArrayList<String>           badRequestMessages = new ArrayList<>();
            ApiQueryFilterUtils.manageCriteriaFields(filter, tableApiFields, badRequestMessages, apiName, apiVersion, new QueryInput(tableName).withFilter(filter));
            errors.addAll(badRequestMessages);
         }
      }
   }

}
