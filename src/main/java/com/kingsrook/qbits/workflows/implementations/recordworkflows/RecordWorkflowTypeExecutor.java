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

package com.kingsrook.qbits.workflows.implementations.recordworkflows;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeExecutorInterface;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 * Workflow-type executor used for record workflows.
 *******************************************************************************/
public class RecordWorkflowTypeExecutor implements WorkflowTypeExecutorInterface
{


   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void postRun(WorkflowExecutionContext workflowExecutionContext) throws QException
   {
      RecordWorkflowContext context = (RecordWorkflowContext) workflowExecutionContext;

      if(context.getIsTestRun())
      {
         updateRecordAssociations(context);
      }
      else
      {
         updateRecord(context);
         deleteRecords(context);
         insertRecords(context);
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void updateRecordAssociations(RecordWorkflowContext context)
   {
      QRecord        record    = context.record.get();
      String         tableName = context.getWorkflow().getTableName();
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);

      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         String         associatedTableName = association.getAssociatedTableName();
         QTableMetaData associatedTable     = QContext.getQInstance().getTable(associatedTableName);

         //////////////////////////////
         // add any queued to insert //
         //////////////////////////////
         List<QRecord> recordsToInsert = context.getRecordsToInsert(associatedTableName);
         for(QRecord recordToInsert : CollectionUtils.nonNullList(recordsToInsert))
         {
            record.withAssociatedRecord(association.getName(), recordToInsert);
         }

         ////////////////////////////////////
         // delete anything marked as such //
         ////////////////////////////////////
         Set<Serializable> primaryKeysToDelete = context.getPrimaryKeysToDelete(associatedTableName);
         if(CollectionUtils.nullSafeHasContents(primaryKeysToDelete))
         {
            List<QRecord>     associatedRecords        = CollectionUtils.nonNullMap(record.getAssociatedRecords()).getOrDefault(association.getName(), Collections.emptyList());
            Iterator<QRecord> associatedRecordIterator = associatedRecords.iterator();
            while(associatedRecordIterator.hasNext())
            {
               QRecord      associatedRecord     = associatedRecordIterator.next();
               Serializable associatedPrimaryKey = associatedRecord.getValue(associatedTable.getPrimaryKeyField());
               if(primaryKeysToDelete.contains(associatedPrimaryKey))
               {
                  associatedRecordIterator.remove();
               }
            }
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void deleteRecords(RecordWorkflowContext context) throws QException
   {
      for(Map.Entry<String, HashSet<Serializable>> entry : context.primaryKeysToDelete.get().entrySet())
      {
         String                tableName   = entry.getKey();
         HashSet<Serializable> primaryKeys = entry.getValue();

         DeleteOutput deleteOutput = new DeleteAction().execute(new DeleteInput(tableName)
            .withPrimaryKeys(new ArrayList<>(primaryKeys))
            .withTransaction(context.getTransaction()));

         if(CollectionUtils.nullSafeHasContents(deleteOutput.getRecordsWithErrors()))
         {
            String errors = getErrorsFromRecords(deleteOutput.getRecordsWithErrors());
            throw (new QException("Error deleting from " + tableName + " table: " + errors));
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void insertRecords(RecordWorkflowContext context) throws QException
   {
      for(Map.Entry<String, ArrayList<QRecord>> entry : context.recordsToInsert.get().entrySet())
      {
         String             tableName = entry.getKey();
         ArrayList<QRecord> records   = entry.getValue();

         List<QRecord> insertedRecords = new InsertAction().execute(new InsertInput(tableName)
            .withRecords(records)
            .withTransaction(context.getTransaction())
         ).getRecords();

         String errors = getErrorsFromRecords(insertedRecords);
         if(!errors.isEmpty())
         {
            throw (new QException("Error inserting into " + tableName + " table: " + errors));
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static String getErrorsFromRecords(List<QRecord> insertedRecords)
   {
      String errors = insertedRecords.stream()
         .filter(r -> CollectionUtils.nullSafeHasContents(r.getErrors()))
         .map(r -> r.getErrorsAsString())
         .collect(Collectors.joining("; "));
      return errors;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void updateRecord(RecordWorkflowContext context) throws QException
   {
      if(context.doesRecordNeedUpdated.get())
      {
         QRecord record = context.record.get();
         UpdateOutput updateOutput = new UpdateAction().execute(new UpdateInput(context.getWorkflow().getTableName())
            .withRecord(record)
            .withTransaction(context.getTransaction()));

         QRecord updatedRecord = updateOutput.getRecords().get(0);
         if(CollectionUtils.nullSafeHasContents(updatedRecord.getErrors()))
         {
            throw new QException("Error updating record: " + updatedRecord.getErrorsAsString());
         }
      }
   }

}
