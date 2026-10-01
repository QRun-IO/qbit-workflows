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

package com.kingsrook.qbits.workflows.execution;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 * specialization of ObjectInWorkflowContext, for QRecordEntities - with
 * conversion to and from QRecord provided here.
 *******************************************************************************/
public class QRecordEntityInWorkflowContext<T extends QRecordEntity & Serializable> extends ObjectInWorkflowContext<T>
{
   private final Class<T> entityClass;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QRecordEntityInWorkflowContext(WorkflowExecutionContext context, String key, Class<T> entityClass)
   {
      super(context, key);
      this.entityClass = entityClass;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public QRecord getRecord()
   {
      return get().toQRecord();
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void set(QRecord record)
   {
      try
      {
         super.set(QRecordEntity.fromQRecord(entityClass, record));
      }
      catch(QException e)
      {
         throw new QRuntimeException("Error setting RecordEntityInContext", e);
      }
   }
}
