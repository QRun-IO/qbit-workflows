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

package com.kingsrook.qbits.workflows.definition;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeExecutorInterface;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeTesterInterface;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** a type of workflow available to the application.  contains WorkflowStepTypeCategory
 ** objects in a list, which contain the WorkflowStepType objects allowed for the
 ** workflow.
 *******************************************************************************/
public class WorkflowType implements Serializable
{
   private String         name;
   private String         label;
   private String         description;
   private QCodeReference executor;
   private QCodeReference tester;

   protected Map<String, List<QHelpContent>>     helpContent;
   private   ArrayList<WorkflowStepTypeCategory> stepTypeCategories;



   /***************************************************************************
    **
    ***************************************************************************/
   public void enrich(QInstance qInstance)
   {
      if(!StringUtils.hasContent(label))
      {
         label = QInstanceEnricher.nameToLabel(name);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void validate(QInstanceValidator qInstanceValidator, QInstance qInstance)
   {
      qInstanceValidator.assertCondition(StringUtils.hasContent(name), "WorkflowType name is required.");

      if(qInstanceValidator.assertCondition(executor != null, "WorkflowType [" + name + "]: executor is required."))
      {
         qInstanceValidator.validateSimpleCodeReference("WorkflowType [" + name + "]: executor ", executor, WorkflowTypeExecutorInterface.class);
      }

      if(tester != null)
      {
         qInstanceValidator.validateSimpleCodeReference("WorkflowType [" + name + "]: tester ", tester, WorkflowTypeTesterInterface.class);
      }
   }



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
   public WorkflowType withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for executor
    *******************************************************************************/
   public QCodeReference getExecutor()
   {
      return (this.executor);
   }



   /*******************************************************************************
    ** Setter for executor
    *******************************************************************************/
   public void setExecutor(QCodeReference executor)
   {
      this.executor = executor;
   }



   /*******************************************************************************
    ** Fluent setter for executor
    *
    * @param executor reference to implementation of
    * {@link com.kingsrook.qbits.workflows.execution.WorkflowTypeExecutorInterface}
    *******************************************************************************/
   public WorkflowType withExecutor(QCodeReference executor)
   {
      this.executor = executor;
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
   public WorkflowType withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public WorkflowType withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stepTypeCategories
    *******************************************************************************/
   public List<WorkflowStepTypeCategory> getStepTypeCategories()
   {
      return (this.stepTypeCategories);
   }



   /*******************************************************************************
    ** Setter for stepTypeCategories
    *******************************************************************************/
   public void setStepTypeCategories(List<WorkflowStepTypeCategory> stepTypeCategories)
   {
      this.stepTypeCategories = CollectionUtils.useOrWrap(stepTypeCategories, new TypeToken<>() {});
   }



   /*******************************************************************************
    ** Fluent setter for stepTypeCategories
    *******************************************************************************/
   public WorkflowType withStepTypeCategories(List<WorkflowStepTypeCategory> stepTypeCategories)
   {
      setStepTypeCategories(stepTypeCategories);
      return (this);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public WorkflowType customizeBasedOnWorkflow(QRecord workflowRecord)
   {
      return (this);
   }



   /***************************************************************************
    ** a method you can override as a way to send additional values to the frontend
    ** in the GetWorkflowTypeDefinitionProcess.  e.g., default apiName and apiVersion.
    ***************************************************************************/
   public Map<String, Serializable> getAdditionalDefinitionValues(QRecord workflowRecord)
   {
      return Collections.emptyMap();
   }



   /*******************************************************************************
    * Getter for tester
    * @see #withTester(QCodeReference)
    *******************************************************************************/
   public QCodeReference getTester()
   {
      return (this.tester);
   }



   /*******************************************************************************
    * Setter for tester
    * @see #withTester(QCodeReference)
    *******************************************************************************/
   public void setTester(QCodeReference tester)
   {
      this.tester = tester;
   }



   /*******************************************************************************
    * Fluent setter for tester
    *
    * @param tester reference to implementation of
    * {@link com.kingsrook.qbits.workflows.execution.WorkflowTypeTesterInterface}
    *
    * @return this
    *******************************************************************************/
   public WorkflowType withTester(QCodeReference tester)
   {
      this.tester = tester;
      return (this);
   }



   /*******************************************************************************
    ** Getter for helpContent
    *******************************************************************************/
   public Map<String, List<QHelpContent>> getHelpContent()
   {
      return (this.helpContent);
   }



   /*******************************************************************************
    ** Setter for helpContent
    *******************************************************************************/
   public void setHelpContent(Map<String, List<QHelpContent>> helpContent)
   {
      this.helpContent = helpContent;
   }



   /*******************************************************************************
    ** Fluent setter for helpContent
    *******************************************************************************/
   public WorkflowType withHelpContent(Map<String, List<QHelpContent>> helpContent)
   {
      this.helpContent = helpContent;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for helpContent
    *******************************************************************************/
   public WorkflowType withHelpContent(String key, QHelpContent helpContent)
   {
      if(this.helpContent == null)
      {
         this.helpContent = new HashMap<>();
      }
      if(!this.helpContent.containsKey(key))
      {
         this.helpContent.put(key, new ArrayList<>());
      }

      this.helpContent.get(key).add(helpContent);
      return (this);
   }

}
