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


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QHelpContentPlugin;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QSupplementalInstanceMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 * Class that stores the available workflow types and their step types.
 * Implemented as QSupplementalInstanceMetaData, meaning, it should exist
 * as effectively a singleton within a QInstance.
 *******************************************************************************/
public class WorkflowsRegistry implements QSupplementalInstanceMetaData, QHelpContentPlugin
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowsRegistry.class);

   public static String NAME = WorkflowsRegistry.class.getName();

   private Map<String, WorkflowType>     workflowTypes;
   private Map<String, WorkflowStepType> workflowStepTypes;



   /*******************************************************************************
    *
    *******************************************************************************/
   public WorkflowsRegistry()
   {
      workflowTypes = new LinkedHashMap<>();
      workflowStepTypes = new LinkedHashMap<>();
   }



   /*******************************************************************************
    * get the instance of this object that is a member *of* the input QInstance
    *
    * @param qInstance the object containing the registry you are looking for
    * @return the workflows registry in that qInstance, or null if there isn't one.
    *******************************************************************************/
   public static WorkflowsRegistry of(QInstance qInstance)
   {
      return QSupplementalInstanceMetaData.of(qInstance, NAME);
   }



   /*******************************************************************************
    * get the instance of this object that is a member *of* the input QInstance -
    * OR - if there isn't one, construct a new one and attach it to the instance.
    *
    * @param qInstance the object containing the registry you are looking for
    * @return the workflows registry in that qInstance - OR - if there wasn't one,
    * a new one, which has been put in the instance.
    *******************************************************************************/
   public static WorkflowsRegistry ofOrWithNew(QInstance qInstance)
   {
      return QSupplementalInstanceMetaData.ofOrWithNew(qInstance, NAME, WorkflowsRegistry::new);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public WorkflowType getWorkflowType(String name)
   {
      return (workflowTypes.get(name));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public List<WorkflowType> getAllWorkflowTypes()
   {
      return (new ArrayList<>(workflowTypes.values()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public WorkflowStepType getWorkflowStepType(String name)
   {
      return (workflowStepTypes.get(name));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public List<WorkflowStepType> getAllWorkflowStepTypes()
   {
      return (new ArrayList<>(workflowStepTypes.values()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void registerWorkflowType(WorkflowType workflowType) throws QException
   {
      if(workflowTypes.containsKey(workflowType.getName()))
      {
         LOG.info("Replacing existing workflow type", logPair("name", workflowType.getName()));
      }

      workflowTypes.put(workflowType.getName(), workflowType);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void registerWorkflowStepType(WorkflowStepType workflowStepType) throws QException
   {
      if(workflowStepTypes.containsKey(workflowStepType.getName()))
      {
         LOG.warn("Replacing existing workflow step type", logPair("name", workflowStepType.getName()));
      }

      workflowStepTypes.put(workflowStepType.getName(), workflowStepType);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void enrich(QInstance qInstance)
   {
      workflowTypes.values().forEach(wt -> wt.enrich(qInstance));
      workflowStepTypes.values().forEach(wst -> wst.enrich(qInstance));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void validate(QInstance qInstance, QInstanceValidator qInstanceValidator)
   {
      workflowTypes.values().forEach(wt -> wt.validate(qInstanceValidator, qInstance));
      workflowStepTypes.values().forEach(workflowStepType -> workflowStepType.validate(qInstanceValidator, qInstance));
   }



   /***************************************************************************
    * as required by {@link QSupplementalInstanceMetaData}
    * @return the unique name for this object within its QInstance
    ***************************************************************************/
   @Override
   public String getName()
   {
      return (NAME);
   }



   /***************************************************************************
    * plug in to the QQQ Help Content system, to allow help content to set
    * descriptions and labels for workflow steps.
    *
    * Note:  helpContent can come in here as null, if it has been, e.g., deleted!
    ***************************************************************************/
   @Override
   public void acceptHelpContent(QInstance qInstance, QHelpContent helpContent, Map<String, String> nameValuePairs)
   {
      if(nameValuePairs.containsKey("workflowStepType"))
      {
         String           workflowStepTypeName = nameValuePairs.get("workflowStepType");
         WorkflowStepType workflowStepType     = workflowStepTypes.get(workflowStepTypeName);
         if(workflowStepType != null)
         {
            if("description".equals(nameValuePairs.get("slot")))
            {
               workflowStepType.setDescription(helpContent == null ? null : helpContent.getContentAsHtml());
            }
            else if("label".equals(nameValuePairs.get("slot")))
            {
               if(helpContent != null)
               {
                  workflowStepType.setLabel(helpContent.getContent());
               }
               else
               {
                  LOG.info("Not removing workflow step type label after deleted help content (original label is not known...", logPair("name", workflowStepTypeName));
               }
            }
            else if(nameValuePairs.containsKey("field"))
            {
               if("qswdDescription".equals(nameValuePairs.get("field")))
               {
                  QFieldMetaData newField = new QFieldMetaData()
                     .withType(QFieldType.STRING)
                     .withLabel("Step Description")
                     .withName("qswdDescription")
                     .withGridColumns(12);

                  newField.setHelpContents(helpContent == null ? null : ListBuilder.of(helpContent));

                  List<QFieldMetaData> existingFields = workflowStepType.getInputFields();
                  existingFields.add(newField);
                  workflowStepType.setInputFields(existingFields);
               }
               else
               {
                  for(QFieldMetaData fieldMetaData : workflowStepType.getInputFields())
                  {
                     if(nameValuePairs.get("field").equals(fieldMetaData.getName()))
                     {
                        fieldMetaData.setHelpContents(helpContent == null ? null : ListBuilder.of(helpContent));
                     }
                  }
               }
            }
         }
      }
   }

}
