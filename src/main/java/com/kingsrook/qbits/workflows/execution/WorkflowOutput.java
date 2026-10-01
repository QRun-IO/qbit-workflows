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
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowOutput extends AbstractActionOutput implements Serializable
{
   private Exception                exception;
   private WorkflowExecutionContext context;
   private WorkflowRunLog           workflowRunLog;



   /*******************************************************************************
    ** Getter for exception
    *******************************************************************************/
   public Exception getException()
   {
      return (this.exception);
   }



   /*******************************************************************************
    ** Setter for exception
    *******************************************************************************/
   public void setException(Exception exception)
   {
      this.exception = exception;
   }



   /*******************************************************************************
    ** Fluent setter for exception
    *******************************************************************************/
   public WorkflowOutput withException(Exception exception)
   {
      this.exception = exception;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRunLog
    *******************************************************************************/
   public WorkflowRunLog getWorkflowRunLog()
   {
      return (this.workflowRunLog);
   }



   /*******************************************************************************
    ** Setter for workflowRunLog
    *******************************************************************************/
   public void setWorkflowRunLog(WorkflowRunLog workflowRunLog)
   {
      this.workflowRunLog = workflowRunLog;
   }



   /*******************************************************************************
    ** Fluent setter for workflowRunLog
    *******************************************************************************/
   public WorkflowOutput withWorkflowRunLog(WorkflowRunLog workflowRunLog)
   {
      this.workflowRunLog = workflowRunLog;
      return (this);
   }



   /*******************************************************************************
    * Getter for context
    * @see #withContext(WorkflowExecutionContext)
    *******************************************************************************/
   public WorkflowExecutionContext getContext()
   {
      return (this.context);
   }



   /*******************************************************************************
    * Setter for context
    * @see #withContext(WorkflowExecutionContext)
    *******************************************************************************/
   public void setContext(WorkflowExecutionContext context)
   {
      this.context = context;
   }



   /*******************************************************************************
    * Fluent setter for context
    *
    * @param context the workflow execution context that was used during execution.
    * @return this
    *******************************************************************************/
   public WorkflowOutput withContext(WorkflowExecutionContext context)
   {
      this.context = context;
      return (this);
   }

}
