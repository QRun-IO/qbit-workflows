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

package com.kingsrook.qbits.workflows.tables;


import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qqq.backend.core.actions.customizers.OldRecordHelper;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTestAssertionTableCustomizer implements TableCustomizerInterface
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowTestAssertionTableCustomizer.class);



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public List<QRecord> preInsertOrUpdate(AbstractActionInput input, List<QRecord> records, boolean isPreview, Optional<List<QRecord>> oldRecordList) throws QException
   {
      OldRecordHelper oldRecordHelper = new OldRecordHelper(WorkflowTestAssertion.TABLE_NAME, oldRecordList);

      for(QRecord record : records)
      {
         try
         {
            String variableName    = ValueUtils.getValueAsString(getValue(record, oldRecordHelper.getOldRecord(record), "variableName"));
            String expectedValue   = ValueUtils.getValueAsString(getValue(record, oldRecordHelper.getOldRecord(record), "expectedValue"));
            String queryFilterJson = ValueUtils.getValueAsString(getValue(record, oldRecordHelper.getOldRecord(record), "queryFilterJson"));

            boolean hasFilter = false;
            if(StringUtils.hasContent(queryFilterJson))
            {
               QQueryFilter filter = JsonUtils.toObject(queryFilterJson, QQueryFilter.class);
               if(filter.hasAnyCriteria())
               {
                  hasFilter = true;
               }
            }

            if(!StringUtils.hasContent(variableName) && !hasFilter)
            {
               record.addError(new BadInputStatusMessage("Either Field or Filter must be given."));
            }
            else if(StringUtils.hasContent(variableName) && hasFilter)
            {
               record.addError(new BadInputStatusMessage("Field and Filter may not both be given."));
            }
            else if(hasFilter && StringUtils.hasContent(expectedValue))
            {
               record.addError(new BadInputStatusMessage("Expected Value and Filter may not both be given."));
            }
         }
         catch(Exception e)
         {
            LOG.warn("Error validating workflow test assertion values", e);
            record.addError(new BadInputStatusMessage("Error validating values: " + e.getMessage()));
         }
      }

      return (records);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private Serializable getValue(QRecord record, Optional<QRecord> oldRecord, String fieldName)
   {
      if(record.getValues().containsKey(fieldName))
      {
         return (record.getValues().get(fieldName));
      }

      if(oldRecord.isPresent() && oldRecord.get().getValues().containsKey(fieldName))
      {
         return (oldRecord.get().getValues().get(fieldName));
      }

      return null;
   }

}
