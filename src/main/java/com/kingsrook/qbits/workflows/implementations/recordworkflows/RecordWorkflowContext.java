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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.kingsrook.qbits.workflows.execution.LazyInitObjectInWorkflowContext;
import com.kingsrook.qbits.workflows.execution.ObjectInWorkflowContext;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 *
 *******************************************************************************/
public class RecordWorkflowContext extends WorkflowExecutionContext
{
   public final ObjectInWorkflowContext<QRecord> record = new ObjectInWorkflowContext<>(this, "record");

   public final ObjectInWorkflowContext<Boolean> doesRecordNeedUpdated = new ObjectInWorkflowContext<>(this, "doesRecordNeedUpdated", false);

   public final ObjectInWorkflowContext<HashMap<String, ArrayList<QRecord>>>    recordsToInsert     = new ObjectInWorkflowContext<>(this, "recordsToInsert", new HashMap<>());
   public final ObjectInWorkflowContext<HashMap<String, HashSet<Serializable>>> primaryKeysToDelete = new ObjectInWorkflowContext<>(this, "primaryKeysToDelete", new HashMap<>());

   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   // records already stored in the backend that are joined with the main record - they used to be stored in a map kept //
   // right here, but they've migrated into the RecordWorkflowContextJoinedRecordHelper.                                //
   ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public final ObjectInWorkflowContext<RecordWorkflowContextJoinedRecordHelper> recordWorkflowContextJoinedRecordHelper = new LazyInitObjectInWorkflowContext<>(this, "recordWorkflowContextJoinedRecordHelper", () -> newRecordWorkflowContextJoinedRecordHelper(this));



   /***************************************************************************
    * Construct a new RecordWorkflowContextJoinedRecordHelper, which uses this
    * context class's members to get the list of records to insert and the
    * set of ids to delete
    ***************************************************************************/
   private RecordWorkflowContextJoinedRecordHelper newRecordWorkflowContextJoinedRecordHelper(RecordWorkflowContext recordWorkflowContext)
   {
      return new RecordWorkflowContextJoinedRecordHelper(getWorkflow().getTableName(), record.get())
      {
         /***************************************************************************
          *
          ***************************************************************************/
         @Override
         public List<QRecord> getRecordsToInsert(String tableName)
         {
            return recordWorkflowContext.recordsToInsert.get().computeIfAbsent(tableName, (k) -> new ArrayList<>());
         }



         /***************************************************************************
          *
          ***************************************************************************/
         @Override
         public Set<Serializable> getPrimaryKeysToDelete(String tableName)
         {
            return recordWorkflowContext.primaryKeysToDelete.get().computeIfAbsent(tableName, (k) -> new HashSet<>());
         }
      };
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addRecordToInsert(String tableName, QRecordEntity entity)
   {
      addRecordsToInsert(tableName, List.of(entity.toQRecord()));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addRecordToInsert(String tableName, QRecord record)
   {
      addRecordsToInsert(tableName, List.of(record));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addRecordsToInsert(String tableName, List<QRecord> records)
   {
      HashMap<String, ArrayList<QRecord>> map = recordsToInsert.get();
      map.computeIfAbsent(tableName, k -> new ArrayList<>()).addAll(records);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public List<QRecord> getRecordsToInsert(String tableName)
   {
      HashMap<String, ArrayList<QRecord>> map = recordsToInsert.get();
      return (map.computeIfAbsent(tableName, k -> new ArrayList<>()));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addPrimaryKeyToDelete(String tableName, Serializable primaryKey)
   {
      addPrimaryKeysToDelete(tableName, List.of(primaryKey));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void addPrimaryKeysToDelete(String tableName, Collection<Serializable> primaryKeys)
   {
      HashMap<String, HashSet<Serializable>> map = primaryKeysToDelete.get();
      map.computeIfAbsent(tableName, k -> new HashSet<>()).addAll(primaryKeys);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public Set<Serializable> getPrimaryKeysToDelete(String tableName)
   {
      HashMap<String, HashSet<Serializable>> map = primaryKeysToDelete.get();
      return (map.computeIfAbsent(tableName, k -> new HashSet<>()));
   }



   /***************************************************************************
    * allow join-records, which are normally managed by the recordWorkflowContextJoinedRecordHelper
    * to be explicitly set in that object.  Useful for test scenarios, maybe more.
    ***************************************************************************/
   public void setJoinRecords(QueryJoin queryJoin, List<QRecord> associatedRecords)
   {
      recordWorkflowContextJoinedRecordHelper.get().setJoinRecords(queryJoin, associatedRecords);
   }
}
