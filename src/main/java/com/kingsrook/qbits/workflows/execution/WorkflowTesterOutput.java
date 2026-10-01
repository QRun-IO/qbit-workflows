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
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTesterOutput extends AbstractActionOutput implements Serializable
{
   private WorkflowTestRun workflowTestRun;



   /*******************************************************************************
    * Getter for workflowTestRun
    * @see #withWorkflowTestRun(WorkflowTestRun)
    *******************************************************************************/
   public WorkflowTestRun getWorkflowTestRun()
   {
      return (this.workflowTestRun);
   }



   /*******************************************************************************
    * Setter for workflowTestRun
    * @see #withWorkflowTestRun(WorkflowTestRun)
    *******************************************************************************/
   public void setWorkflowTestRun(WorkflowTestRun workflowTestRun)
   {
      this.workflowTestRun = workflowTestRun;
   }



   /*******************************************************************************
    * Fluent setter for workflowTestRun
    *
    * @param workflowTestRun
    * The run log object - with populated children:  WorkflowTestRunScenario and
    * WorkflowTestOutput.
    * @return this
    *******************************************************************************/
   public WorkflowTesterOutput withWorkflowTestRun(WorkflowTestRun workflowTestRun)
   {
      this.workflowTestRun = workflowTestRun;
      return (this);
   }

}
