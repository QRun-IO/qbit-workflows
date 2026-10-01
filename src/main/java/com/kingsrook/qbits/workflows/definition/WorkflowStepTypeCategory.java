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

package com.kingsrook.qbits.workflows.definition;


import java.io.Serializable;
import java.util.List;


/*******************************************************************************
 ** collection of step-types within a workflow type.  just organizational.
 *******************************************************************************/
public class WorkflowStepTypeCategory implements Serializable
{
   private String name;
   private String label;
   private List<String> workflowStepTypes;



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public WorkflowStepTypeCategory withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public WorkflowStepTypeCategory withLabel(String label)
   {
      this.label = label;
      return (this);
   }


   /*******************************************************************************
    ** Getter for workflowStepTypes
    *******************************************************************************/
   public List<String> getWorkflowStepTypes()
   {
      return (this.workflowStepTypes);
   }



   /*******************************************************************************
    ** Setter for workflowStepTypes
    *******************************************************************************/
   public void setWorkflowStepTypes(List<String> workflowStepTypes)
   {
      this.workflowStepTypes = workflowStepTypes;
   }



   /*******************************************************************************
    ** Fluent setter for workflowStepTypes
    *******************************************************************************/
   public WorkflowStepTypeCategory withWorkflowStepTypes(List<String> workflowStepTypes)
   {
      this.workflowStepTypes = workflowStepTypes;
      return (this);
   }


}
