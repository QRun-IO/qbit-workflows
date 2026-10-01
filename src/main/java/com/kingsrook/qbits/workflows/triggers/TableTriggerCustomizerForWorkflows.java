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

package com.kingsrook.qbits.workflows.triggers;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.savedviews.SavedView;
import com.kingsrook.qqq.backend.core.model.scripts.Script;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** table-customizer to apply to the TableTrigger table, for after the
 ** workflowId field is added to it.
 *******************************************************************************/
public class TableTriggerCustomizerForWorkflows implements TableCustomizerInterface
{
   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> preInsertOrUpdate(AbstractActionInput input, List<QRecord> records, boolean isPreview, Optional<List<QRecord>> oldRecordList) throws QException
   {
      QRecord                              emptyRecord  = new QRecord();
      Optional<Map<Serializable, QRecord>> oldRecordMap = oldRecordListToMap("id", oldRecordList);

      for(QRecord record : CollectionUtils.nonNullList(records))
      {
         QRecord oldRecord = oldRecordMap.orElse(Collections.emptyMap()).getOrDefault(record.getValue("id"), emptyRecord);

         /////////////////////////////////////////////////////
         // require either scriptId or workflowId to be set //
         /////////////////////////////////////////////////////
         if(record.getValue("scriptId") == null && record.getValue("workflowId") == null)
         {
            record.addError(new BadInputStatusMessage("Either a Script or a Workflow must be given"));
         }

         String tableName = record.getValueString("tableName");
         if(!StringUtils.hasContent(tableName))
         {
            tableName = oldRecord.getValueString("tableName");
         }

         if(StringUtils.hasContent(tableName))
         {
            checkTableNameMatchesOnRelatedRecord(record, oldRecord, tableName, "scriptId", Script.TABLE_NAME);
            checkTableNameMatchesOnRelatedRecord(record, oldRecord, tableName, "workflowId", Workflow.TABLE_NAME);
            checkTableNameMatchesOnRelatedRecord(record, oldRecord, tableName, "filterId", SavedView.TABLE_NAME);
         }
      }

      return (records);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private void checkTableNameMatchesOnRelatedRecord(QRecord record, QRecord oldRecord, String tableName, String relatedRecordIdFieldName, String relatedRecordTableName) throws QException
   {
      Integer relatedRecordId = null;
      if(record.getValues().containsKey(relatedRecordIdFieldName))
      {
         relatedRecordId = ValueUtils.getValueAsInteger(record.getValues().get(relatedRecordIdFieldName));
      }
      else if(oldRecord.getValues().containsKey(relatedRecordIdFieldName))
      {
         relatedRecordId = ValueUtils.getValueAsInteger(oldRecord.getValues().get(relatedRecordIdFieldName));
      }

      if(relatedRecordId != null)
      {
         QRecord relatedRecord = GetAction.execute(relatedRecordTableName, relatedRecordId);
         if(relatedRecord == null)
         {
            return;
         }

         String relatedRecordTableNameValue = relatedRecord.getValueString("tableName");
         if(!Objects.equals(tableName, relatedRecordTableNameValue))
         {
            QTableMetaData relatedTable       = QContext.getQInstance().getTable(relatedRecordTableName);
            QTableMetaData relatedRecordTable = QContext.getQInstance().getTable(relatedRecordTableNameValue);
            QTableMetaData triggerTable       = QContext.getQInstance().getTable(tableName);

            record.addError(new BadInputStatusMessage("The selected " + relatedTable.getLabel() + " is not associated with the same table ("
               + (relatedRecordTable == null ? "--" : relatedRecordTable.getLabel())
               + ") as the Table Trigger ("
               + (triggerTable == null ? "--" : triggerTable.getLabel())
               + ")"));
         }
      }
   }

}
