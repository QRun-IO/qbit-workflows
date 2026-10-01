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


import java.util.List;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTableCustomizer implements TableCustomizerInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QRecord> preInsert(InsertInput insertInput, List<QRecord> records, boolean isPreview) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // when inserting, there shouldn't be a revision id (but note, in the case of a duplicate/copy, there might be one! //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for(QRecord record : CollectionUtils.nonNullList(records))
      {
         record.setValue("currentWorkflowRevisionId", null);
      }
      return records;
   }

}
