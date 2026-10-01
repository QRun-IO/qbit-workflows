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
import java.util.function.Supplier;


/*******************************************************************************
 * specialization of {@link ObjectInWorkflowContext} lazily initializes the
 * object any time it is get()ed.  doesn't make sense for al objects in context!
 * but, does make sense, e.g., for list of records to insert.
 *******************************************************************************/
public class LazyInitObjectInWorkflowContext<T extends Serializable> extends ObjectInWorkflowContext<T>
{
   private final Supplier<T> supplier;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public LazyInitObjectInWorkflowContext(WorkflowExecutionContext context, String key, Supplier<T> supplier)
   {
      super(context, key);
      this.supplier = supplier;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public T get()
   {
      T t = super.get();
      if(t == null)
      {
         t = supplier.get();
         set(t);
      }
      return (t);
   }
}
