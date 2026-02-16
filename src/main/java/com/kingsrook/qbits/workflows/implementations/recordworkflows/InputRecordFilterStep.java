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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.kingsrook.qqq.backend.core.utils.StringUtils;


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
   public WorkflowStepOutput execute(WorkflowStep step, Map<String, Serializable> inputValues, WorkflowExecutionContext workflowExecutionContext) throws QException
   {
      RecordWorkflowContext context = (RecordWorkflowContext) workflowExecutionContext;

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

      List<QRecordWithJoinedRecords> recordWithJoins = buildCrossProduct(record, filter, context.recordWorkflowContextJoinedRecordHelper.get());
      return evaluateCrossProduct(recordWithJoins, filter);
   }



   /***************************************************************************
    * For a given record, and a filter, and a {@link RecordWorkflowContextJoinedRecordHelper}
    * object from a live workflow context, build a List of {@link QRecordWithJoinedRecords}
    * that can be used to evaluate if a record (with joins) matches a filter.
    *
    * <p>Note that this method is public, and called by application-defined workflows.
    * In other words, this method is part of our published API in here!</p>
    *
    * @param record the main record in the workflow
    * @param filter the filter which may have joins (thus the need for a cross-product)
    * @param joinedRecordHelper object from context of the workflow that knows
    *                           about cached join records and records being inserted
    *                           and/or deleted.
    * @return {@code List<QRecordWithJoinedRecords>} the cross product that the
    * filter should be evaluated against.
    ***************************************************************************/
   public static List<QRecordWithJoinedRecords> buildCrossProduct(QRecord record, QQueryFilter filter, RecordWorkflowContextJoinedRecordHelper joinedRecordHelper) throws QException
   {
      List<QRecordWithJoinedRecords> crossProduct = new ArrayList<>();
      crossProduct.add(new QRecordWithJoinedRecords(record));

      List<QueryJoin> joinsInFilter = BackendQueryFilterUtils.identifyJoinsInFilter(joinedRecordHelper.getTableName(), filter);
      joinsInFilter = sortQueryJoinsFromMainTableOutward(joinedRecordHelper.getTableName(), joinsInFilter);
      for(QueryJoin join : joinsInFilter)
      {
         crossProduct = expandCrossProductViaJoin(crossProduct, join, joinedRecordHelper);
      }

      return crossProduct;
   }



   /*******************************************************************************
    * Sort the query joins so that they fan outward from the main table to ones
    * farther away.
    *
    * <p>This is so, if we've got a -> b -> c -- we'll process 'b' before 'c',
    * as we need the records in 'b' to find the records in 'c'.</p>
    *
    * <p><i>Note that this method was copied from {@code AbstractRDBMSAction}</i></p>
    *******************************************************************************/
   private static List<QueryJoin> sortQueryJoinsFromMainTableOutward(String mainTableName, List<QueryJoin> queryJoins)
   {
      List<QueryJoin> rs = new ArrayList<>();

      ////////////////////////////////////////////////////////////////////////////////
      // make a copy of the input list that we can feel safe removing elements from //
      ////////////////////////////////////////////////////////////////////////////////
      List<QueryJoin> inputListCopy = new ArrayList<>(queryJoins);

      ///////////////////////////////////////////////////////////////////////////////////////////////////
      // keep track of the tables (or aliases) that we've seen - that's what we'll "grow" outward from //
      ///////////////////////////////////////////////////////////////////////////////////////////////////
      Set<String> seenTablesOrAliases = new HashSet<>();
      seenTablesOrAliases.add(mainTableName);

      ////////////////////////////////////////////////////////////////////////////////////
      // loop as long as there are more tables in the inputList, and the keepGoing flag //
      // is set (e.g., indicating that we added something in the last iteration)        //
      ////////////////////////////////////////////////////////////////////////////////////
      boolean keepGoing = true;
      while(!inputListCopy.isEmpty() && keepGoing)
      {
         keepGoing = false;

         Iterator<QueryJoin> iterator = inputListCopy.iterator();
         while(iterator.hasNext())
         {
            QueryJoin nextQueryJoin = iterator.next();

            //////////////////////////////////////////////////////////////////////////
            // get the baseTableOrAlias from this join - and if it isn't set in the //
            // QueryJoin, then get it from the left-side of the join's metaData     //
            //////////////////////////////////////////////////////////////////////////
            String baseTableOrAlias = nextQueryJoin.getBaseTableOrAlias();
            if(baseTableOrAlias == null && nextQueryJoin.getJoinMetaData() != null)
            {
               baseTableOrAlias = nextQueryJoin.getJoinMetaData().getLeftTable();
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // if we have a baseTableOrAlias (would we ever not?), and we've seen it before - OR - we've seen this query join's joinTableOrAlias,   //
            // then we can add this pair of namesOrAliases to our seen-set, remove this queryJoin from the inputListCopy (iterator), and keep going //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if((StringUtils.hasContent(baseTableOrAlias) && seenTablesOrAliases.contains(baseTableOrAlias)) || seenTablesOrAliases.contains(nextQueryJoin.getJoinTableOrItsAlias()))
            {
               rs.add(nextQueryJoin);
               if(StringUtils.hasContent(baseTableOrAlias))
               {
                  seenTablesOrAliases.add(baseTableOrAlias);
               }

               seenTablesOrAliases.add(nextQueryJoin.getJoinTableOrItsAlias());
               iterator.remove();
               keepGoing = true;
            }
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // in case any are left, add them all here - does this ever happen?                                          //
      // the only time a conditional breakpoint here fires in the RDBMS test suite, is in query designed to throw. //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      rs.addAll(inputListCopy);

      return (rs);
   }





   /***************************************************************************
    *
    ***************************************************************************/
   private static List<QRecordWithJoinedRecords> expandCrossProductViaJoin(List<QRecordWithJoinedRecords> recordWithJoinedRecords, QueryJoin queryJoin, RecordWorkflowContextJoinedRecordHelper joinedRecordHelper) throws QException
   {
      String            joinTableName           = queryJoin.getJoinTable();
      List<QRecord>     recordsToBeInserted     = joinedRecordHelper.getRecordsToInsert(joinTableName);
      List<QRecord>     recordsAlreadyInBackend = joinedRecordHelper.getJoinRecords(queryJoin);
      Set<Serializable> idsToDelete             = joinedRecordHelper.getPrimaryKeysToDelete(joinTableName);
      String            primaryKeyField         = QContext.getQInstance().getTable(joinTableName).getPrimaryKeyField();

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

      recordWithJoinedRecords = makeCrossProduct(recordWithJoinedRecords, joinTableName, recordsToCross);
      return recordWithJoinedRecords;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static List<QRecordWithJoinedRecords> makeCrossProduct(List<QRecordWithJoinedRecords> recordsWithJoinedRecords, String joinTableName, List<QRecord> joinRecordsToCross)
   {
      if(recordsWithJoinedRecords.isEmpty() || joinRecordsToCross.isEmpty())
      {
         return new ArrayList<>();
      }

      List<QRecordWithJoinedRecords> newCrossProduct = new ArrayList<>();
      for(QRecordWithJoinedRecords recordWithJoinedRecord : recordsWithJoinedRecords)
      {
         newCrossProduct.addAll(recordWithJoinedRecord.buildCrossProduct(joinTableName, joinRecordsToCross));
      }
      recordsWithJoinedRecords = newCrossProduct;

      return recordsWithJoinedRecords;
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
            Map<String, QFieldMetaData> tableApiFields     = GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsAction.ApiNameVersionAndTableName(apiName, apiVersion, tableName));
            ArrayList<String>           badRequestMessages = new ArrayList<>();
            ApiQueryFilterUtils.manageCriteriaFields(filter, tableApiFields, badRequestMessages, apiName, apiVersion, new QueryInput(tableName).withFilter(filter));
            errors.addAll(badRequestMessages);
         }
      }
   }

}
