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
import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowInput extends AbstractActionInput implements Serializable
{
   private Integer                   workflowId;
   private Map<String, Serializable> values;

   private QRecord overrideWorkflowRevision;

   private WorkflowExecutionContext workflowExecutionContext;

   private QBackendTransaction transaction;



   /*******************************************************************************
    ** Getter for workflowId
    *******************************************************************************/
   public Integer getWorkflowId()
   {
      return (this.workflowId);
   }



   /*******************************************************************************
    ** Setter for workflowId
    *******************************************************************************/
   public void setWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowId
    *******************************************************************************/
   public WorkflowInput withWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for values
    *******************************************************************************/
   public Map<String, Serializable> getValues()
   {
      return (this.values);
   }



   /*******************************************************************************
    ** Setter for values
    *******************************************************************************/
   public void setValues(Map<String, Serializable> values)
   {
      this.values = values;
   }



   /*******************************************************************************
    ** Fluent setter for values
    *******************************************************************************/
   public WorkflowInput withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    * Getter for transaction
    * @see #withTransaction(QBackendTransaction)
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return (this.transaction);
   }



   /*******************************************************************************
    * Setter for transaction
    * @see #withTransaction(QBackendTransaction)
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    * Fluent setter for transaction
    *
    * @param transaction
    * a backend transaction owned by the caller, which will be used in the workflow's
    * execution context.  If non-null, it is assumed that the caller 100% owns the
    * transaction, and will manage its end-of-life (commit, rollback, close).  If
    * null, then the workflow executor will create a new transaction (going through
    * WorkflowTypeExecutorInterface.openTransaction), and will do the commit/rollback/
    * close on it.
    *
    * @return this
    *******************************************************************************/
   public WorkflowInput withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
      return (this);
   }



   /*******************************************************************************
    * Getter for workflowExecutionContext
    * @see #withWorkflowExecutionContext(WorkflowExecutionContext)
    *******************************************************************************/
   public WorkflowExecutionContext getWorkflowExecutionContext()
   {
      return (this.workflowExecutionContext);
   }



   /*******************************************************************************
    * Setter for workflowExecutionContext
    * @see #withWorkflowExecutionContext(WorkflowExecutionContext)
    *******************************************************************************/
   public void setWorkflowExecutionContext(WorkflowExecutionContext workflowExecutionContext)
   {
      this.workflowExecutionContext = workflowExecutionContext;
   }



   /*******************************************************************************
    * Fluent setter for workflowExecutionContext
    *
    * @param workflowExecutionContext
    * Allow caller to supply a WorkflowExecutionContext object (or subclass)
    *
    * @return this
    *******************************************************************************/
   public WorkflowInput withWorkflowExecutionContext(WorkflowExecutionContext workflowExecutionContext)
   {
      this.workflowExecutionContext = workflowExecutionContext;
      return (this);
   }


   /*******************************************************************************
    * Getter for overrideWorkflowRevision
    * @see #withOverrideWorkflowRevision(QRecord)
    *******************************************************************************/
   public QRecord getOverrideWorkflowRevision()
   {
      return (this.overrideWorkflowRevision);
   }



   /*******************************************************************************
    * Setter for overrideWorkflowRevision
    * @see #withOverrideWorkflowRevision(QRecord)
    *******************************************************************************/
   public void setOverrideWorkflowRevision(QRecord overrideWorkflowRevision)
   {
      this.overrideWorkflowRevision = overrideWorkflowRevision;
   }



   /*******************************************************************************
    * Fluent setter for overrideWorkflowRevision
    *
    * @param overrideWorkflowRevision
    * Allow a revision (populated with steps and links) to be passed in, to run instead
    * of the current revision assigned to the workflow record.  Useful for use-cases
    * where a user is editing and wants to run an unsaved set of steps & links.
    * @return this
    *******************************************************************************/
   public WorkflowInput withOverrideWorkflowRevision(QRecord overrideWorkflowRevision)
   {
      this.overrideWorkflowRevision = overrideWorkflowRevision;
      return (this);
   }


}
