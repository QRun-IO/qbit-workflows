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
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowTesterInput extends AbstractActionInput implements Serializable
{
   private QRecord       workflow;
   private List<QRecord> workflowTestScenarioList;

   private QRecord overrideWorkflowRevision;



   /*******************************************************************************
    * Getter for workflow
    * @see #withWorkflow(QRecord)
    *******************************************************************************/
   public QRecord getWorkflow()
   {
      return workflow;
   }



   /*******************************************************************************
    * Setter for workflow
    * @see #withWorkflow(QRecord)
    *******************************************************************************/
   public void setWorkflow(QRecord workflow)
   {
      this.workflow = workflow;
   }



   /*******************************************************************************
    * Fluent setter for workflow
    *
    * @param workflow The workflow to test
    * @return this
    *******************************************************************************/
   public WorkflowTesterInput withWorkflow(QRecord workflow)
   {
      this.workflow = workflow;
      return (this);
   }



   /*******************************************************************************
    * Getter for workflowTestScenarioList
    * @see #withWorkflowTestScenarioList(List)
    *******************************************************************************/
   public List<QRecord> getWorkflowTestScenarioList()
   {
      return (this.workflowTestScenarioList);
   }



   /*******************************************************************************
    * Setter for workflowTestScenarioList
    * @see #withWorkflowTestScenarioList(List)
    *******************************************************************************/
   public void setWorkflowTestScenarioList(List<QRecord> workflowTestScenarioList)
   {
      this.workflowTestScenarioList = workflowTestScenarioList;
   }



   /*******************************************************************************
    * Fluent setter for workflowTestScenarioList
    *
    * @param workflowTestScenarioList list of scenarios to test against.
    * @return this
    *******************************************************************************/
   public WorkflowTesterInput withWorkflowTestScenarioList(List<QRecord> workflowTestScenarioList)
   {
      this.workflowTestScenarioList = workflowTestScenarioList;
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
   public WorkflowTesterInput withOverrideWorkflowRevision(QRecord overrideWorkflowRevision)
   {
      this.overrideWorkflowRevision = overrideWorkflowRevision;
      return (this);
   }

}
