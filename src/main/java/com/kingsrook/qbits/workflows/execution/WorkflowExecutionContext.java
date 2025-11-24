/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qbits.workflows.execution;


import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;


/*******************************************************************************
 ** data that moves along with a workflow as it is being executed
 *******************************************************************************/
public class WorkflowExecutionContext
{
   private Workflow                  workflow;
   private WorkflowRevision          workflowRevision;
   private Map<String, Serializable> values = new LinkedHashMap<>();
   private QBackendTransaction       transaction;
   private WorkflowRunLog            workflowRunLog;

   private WorkflowExecutor executor;

   private boolean isTestRun = false;



   /*******************************************************************************
    ** Getter for workflow
    *******************************************************************************/
   public Workflow getWorkflow()
   {
      return (this.workflow);
   }



   /*******************************************************************************
    ** Setter for workflow
    *******************************************************************************/
   public void setWorkflow(Workflow workflow)
   {
      this.workflow = workflow;
   }



   /*******************************************************************************
    ** Fluent setter for workflow
    *******************************************************************************/
   public WorkflowExecutionContext withWorkflow(Workflow workflow)
   {
      this.workflow = workflow;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRevision
    *******************************************************************************/
   public WorkflowRevision getWorkflowRevision()
   {
      return (this.workflowRevision);
   }



   /*******************************************************************************
    ** Setter for workflowRevision
    *******************************************************************************/
   public void setWorkflowRevision(WorkflowRevision workflowRevision)
   {
      this.workflowRevision = workflowRevision;
   }



   /*******************************************************************************
    ** Fluent setter for workflowRevision
    *******************************************************************************/
   public WorkflowExecutionContext withWorkflowRevision(WorkflowRevision workflowRevision)
   {
      this.workflowRevision = workflowRevision;
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
   public WorkflowExecutionContext withValues(Map<String, Serializable> values)
   {
      this.values = values;
      return (this);
   }



   /*******************************************************************************
    ** Getter for transaction
    *******************************************************************************/
   public QBackendTransaction getTransaction()
   {
      return (this.transaction);
   }



   /*******************************************************************************
    ** Setter for transaction
    *******************************************************************************/
   public void setTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
   }



   /*******************************************************************************
    ** Fluent setter for transaction
    *******************************************************************************/
   public WorkflowExecutionContext withTransaction(QBackendTransaction transaction)
   {
      this.transaction = transaction;
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
   public WorkflowExecutionContext withWorkflowRunLog(WorkflowRunLog workflowRunLog)
   {
      this.workflowRunLog = workflowRunLog;
      return (this);
   }



   /*******************************************************************************
    * Getter for isTestRun
    * @see #withIsTestRun(boolean)
    *******************************************************************************/
   public boolean getIsTestRun()
   {
      return (this.isTestRun);
   }



   /*******************************************************************************
    * Setter for isTestRun
    * @see #withIsTestRun(boolean)
    *******************************************************************************/
   public void setIsTestRun(boolean isTestRun)
   {
      this.isTestRun = isTestRun;
   }



   /*******************************************************************************
    * Fluent setter for isTestRun
    *
    * @param isTestRun indicates if the execution context is a test-run or not.
    * @return this
    *******************************************************************************/
   public WorkflowExecutionContext withIsTestRun(boolean isTestRun)
   {
      this.isTestRun = isTestRun;
      return (this);
   }



   /*******************************************************************************
    * Getter for executor
    * @see #setExecutor(WorkflowExecutor)
    *******************************************************************************/
   WorkflowExecutor getExecutor()
   {
      return (this.executor);
   }



   /*******************************************************************************
    * Setter for executor
    * @param executor the object executing the workflow.
    *******************************************************************************/
   void setExecutor(WorkflowExecutor executor)
   {
      this.executor = executor;
   }

}
