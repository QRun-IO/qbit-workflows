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

package com.kingsrook.qbits.workflows.model;


import com.kingsrook.qbits.workflows.metadata.WorkflowStepTypePossibleValueSource;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** QRecord Entity for WorkflowStep table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowStep.TableMetaDataCustomizer.class
)
public class WorkflowStep extends QRecordEntity
{
   public static final String TABLE_NAME = "workflowStep";



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TableMetaDataCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QTableMetaData customizeMetaData(QInstance qInstance, QTableMetaData table) throws QException
      {
         table
            .withUniqueKey(new UniqueKey("workflowRevisionId", "stepNo"))
            .withIcon(new QIcon().withName("polyline"))
            .withRecordLabelFormat("%s: %s")
            .withRecordLabelFields("workflowStepTypeName", "summary")
            .withSection(SectionFactory.defaultT1("id", "workflowRevisionId", "stepNo"))
            .withSection(SectionFactory.defaultT2("workflowStepTypeName", "summary", "description", "inputValuesJson"));

         return (table);
      }
   }


   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WorkflowRevision.TABLE_NAME)
   private Integer workflowRevisionId;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, isRequired = true, possibleValueSourceName = WorkflowStepTypePossibleValueSource.NAME, label = "Step Type")
   private String workflowStepTypeName;

   @QField(isRequired = true)
   private Integer stepNo;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String summary;

   @QField(maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String description;

   //////////////////////////////////////////////
   // mediumtext type, so no max-length needed //
   //////////////////////////////////////////////
   @QField(label = "Input Values")
   private String inputValuesJson;


   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowStep()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowStep(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public WorkflowStep withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRevisionId
    *******************************************************************************/
   public Integer getWorkflowRevisionId()
   {
      return (this.workflowRevisionId);
   }



   /*******************************************************************************
    ** Setter for workflowRevisionId
    *******************************************************************************/
   public void setWorkflowRevisionId(Integer workflowRevisionId)
   {
      this.workflowRevisionId = workflowRevisionId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowRevisionId
    *******************************************************************************/
   public WorkflowStep withWorkflowRevisionId(Integer workflowRevisionId)
   {
      this.workflowRevisionId = workflowRevisionId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowStepTypeName
    *******************************************************************************/
   public String getWorkflowStepTypeName()
   {
      return (this.workflowStepTypeName);
   }



   /*******************************************************************************
    ** Setter for workflowStepTypeName
    *******************************************************************************/
   public void setWorkflowStepTypeName(String workflowStepTypeName)
   {
      this.workflowStepTypeName = workflowStepTypeName;
   }



   /*******************************************************************************
    ** Fluent setter for workflowStepTypeName
    *******************************************************************************/
   public WorkflowStep withWorkflowStepTypeName(String workflowStepTypeName)
   {
      this.workflowStepTypeName = workflowStepTypeName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for stepNo
    *******************************************************************************/
   public Integer getStepNo()
   {
      return (this.stepNo);
   }



   /*******************************************************************************
    ** Setter for stepNo
    *******************************************************************************/
   public void setStepNo(Integer stepNo)
   {
      this.stepNo = stepNo;
   }



   /*******************************************************************************
    ** Fluent setter for stepNo
    *******************************************************************************/
   public WorkflowStep withStepNo(Integer stepNo)
   {
      this.stepNo = stepNo;
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
   public WorkflowStep withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputValuesJson
    *******************************************************************************/
   public String getInputValuesJson()
   {
      return (this.inputValuesJson);
   }



   /*******************************************************************************
    ** Setter for inputValuesJson
    *******************************************************************************/
   public void setInputValuesJson(String inputValuesJson)
   {
      this.inputValuesJson = inputValuesJson;
   }



   /*******************************************************************************
    ** Fluent setter for inputValuesJson
    *******************************************************************************/
   public WorkflowStep withInputValuesJson(String inputValuesJson)
   {
      this.inputValuesJson = inputValuesJson;
      return (this);
   }



   /*******************************************************************************
    ** Getter for summary
    *******************************************************************************/
   public String getSummary()
   {
      return (this.summary);
   }



   /*******************************************************************************
    ** Setter for summary
    *******************************************************************************/
   public void setSummary(String summary)
   {
      this.summary = summary;
   }



   /*******************************************************************************
    ** Fluent setter for summary
    *******************************************************************************/
   public WorkflowStep withSummary(String summary)
   {
      this.summary = summary;
      return (this);
   }


}
