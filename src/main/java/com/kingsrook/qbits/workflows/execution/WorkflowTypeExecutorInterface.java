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


import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.QBackendTransaction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** interface for the code that executes a workflow type - though really just
 ** pre & post, plus pre- & post- individual steps
 *******************************************************************************/
public interface WorkflowTypeExecutorInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   default void preRun(WorkflowExecutionContext context, Workflow workflow, WorkflowRevision workflowRevision) throws QException
   {

   }

   /***************************************************************************
    **
    ***************************************************************************/
   default void postRun(WorkflowExecutionContext context) throws QException
   {

   }


   /***************************************************************************
    **
    ***************************************************************************/
   default void handleException(Exception e, WorkflowExecutionContext values) throws QException
   {

   }

   /***************************************************************************
    *
    ***************************************************************************/
   default void preStep(WorkflowStep step, WorkflowExecutionContext context) throws QException
   {

   }

   /***************************************************************************
    *
    ***************************************************************************/
   default WorkflowStepOutput postStep(WorkflowStep step, WorkflowExecutionContext context, WorkflowStepOutput stepOutput) throws QException
   {
      return stepOutput;
   }

   /***************************************************************************
    **
    ***************************************************************************/
   default QBackendTransaction openTransaction(Workflow workflow, WorkflowRevision workflowRevision) throws QException
   {
      if(StringUtils.hasContent(workflow.getTableName()))
      {
         return (QBackendTransaction.openFor(new InsertInput(workflow.getTableName())));
      }

      return (null);
   }
}
