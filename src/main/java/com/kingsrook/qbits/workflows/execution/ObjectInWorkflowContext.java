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


/*******************************************************************************
 * Helper object for working with named and typed objects into a workflow context.
 *
 * The object itself is stored in the `context`, under the name specified by
 * the `key`.  The type is driven by the type-parameter to the class.
 *
 * The envisioned usage pattern would have class designed for a particular
 * workflow-type that `extends WorkflowExecutionContext`.  In that class would
 * be a number of public final members of this class, such as:
 * ```
 * public final ObjectInWorkflowContext[String] someString = new ObjectInWorkflowContext(this, "someString");
 * ```
 *
 * Then within workflow steps:
 * ```
 * context.someString.set("some value");
 * String theValue = context.someString.get();
 * ```
 *******************************************************************************/
public class ObjectInWorkflowContext<T extends Serializable>
{
   private final WorkflowExecutionContext context;
   private final String                   key;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ObjectInWorkflowContext(WorkflowExecutionContext context, String key)
   {
      this.context = context;
      this.key = key;
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public ObjectInWorkflowContext(WorkflowExecutionContext context, String key, T initialValue)
   {
      this.context = context;
      this.key = key;

      set(initialValue);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private String getKey()
   {
      return key;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public T get()
   {
      return (T) context.getValues().get(getKey());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void set(T value)
   {
      context.getValues().put(getKey(), value);
   }
}
