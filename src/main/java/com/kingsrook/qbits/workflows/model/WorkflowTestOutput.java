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


import java.io.Serializable;
import java.time.Instant;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


/*******************************************************************************
 ** QRecord Entity for WorkflowTestOutput table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowTestOutput.TableMetaDataCustomizer.class
)
public class WorkflowTestOutput extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowTestOutput";



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
            .withIcon(new QIcon().withName("output"))
            .withRecordLabelFormat("%s - %s")
            .withRecordLabelFields("workflowTestAssertionId", "workflowTestRunScenarioId")
            .withSection(SectionFactory.defaultT1("id", "workflowTestRunScenarioId", "workflowTestAssertionId"))
            .withSection(SectionFactory.defaultT2("status", "actualValue", "message").withName("result"))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         table.withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE);

         WorkflowTestStatus.customizeFieldWitChipAndWidth(table.getField("status"));

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(possibleValueSourceName = WorkflowTestRunScenario.TABLE_NAME)
   private Integer workflowTestRunScenarioId;

   @QField(possibleValueSourceName = WorkflowTestAssertion.TABLE_NAME)
   private Integer workflowTestAssertionId;

   @QField(possibleValueSourceName = WorkflowTestStatus.NAME)
   private Integer status;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String actualValue;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String message;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowTestOutput()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowTestOutput(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (id);
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
   public WorkflowTestOutput withId(Integer id)
   {
      setId(id);
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (createDate);
   }



   /*******************************************************************************
    ** Setter for createDate
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate
    *******************************************************************************/
   public WorkflowTestOutput withCreateDate(Instant createDate)
   {
      setCreateDate(createDate);
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (modifyDate);
   }



   /*******************************************************************************
    ** Setter for modifyDate
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate
    *******************************************************************************/
   public WorkflowTestOutput withModifyDate(Instant modifyDate)
   {
      setModifyDate(modifyDate);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowTestRunScenarioId
    *******************************************************************************/
   public Integer getWorkflowTestRunScenarioId()
   {
      return (workflowTestRunScenarioId);
   }



   /*******************************************************************************
    ** Setter for workflowTestRunScenarioId
    *******************************************************************************/
   public void setWorkflowTestRunScenarioId(Integer workflowTestRunScenarioId)
   {
      this.workflowTestRunScenarioId = workflowTestRunScenarioId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTestRunScenarioId
    *******************************************************************************/
   public WorkflowTestOutput withWorkflowTestRunScenarioId(Integer workflowTestRunScenarioId)
   {
      setWorkflowTestRunScenarioId(workflowTestRunScenarioId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowTestAssertionId
    *******************************************************************************/
   public Integer getWorkflowTestAssertionId()
   {
      return (workflowTestAssertionId);
   }



   /*******************************************************************************
    ** Setter for workflowTestAssertionId
    *******************************************************************************/
   public void setWorkflowTestAssertionId(Integer workflowTestAssertionId)
   {
      this.workflowTestAssertionId = workflowTestAssertionId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTestAssertionId
    *******************************************************************************/
   public WorkflowTestOutput withWorkflowTestAssertionId(Integer workflowTestAssertionId)
   {
      setWorkflowTestAssertionId(workflowTestAssertionId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for status
    *******************************************************************************/
   public Integer getStatus()
   {
      return (status);
   }



   /*******************************************************************************
    ** Setter for status
    *******************************************************************************/
   public void setStatus(Integer status)
   {
      this.status = status;
   }



   /*******************************************************************************
    ** Fluent setter for status
    *******************************************************************************/
   public WorkflowTestOutput withStatus(Integer status)
   {
      setStatus(status);
      return (this);
   }



   /*******************************************************************************
    ** Getter for actualValue
    *******************************************************************************/
   public String getActualValue()
   {
      return (actualValue);
   }



   /*******************************************************************************
    ** Setter for actualValue
    *******************************************************************************/
   public void setActualValue(String actualValue)
   {
      this.actualValue = actualValue;
   }



   /*******************************************************************************
    ** Fluent setter for actualValue
    *******************************************************************************/
   public WorkflowTestOutput withActualValue(String actualValue)
   {
      setActualValue(actualValue);
      return (this);
   }



   /*******************************************************************************
    * Getter for message
    *******************************************************************************/
   public String getMessage()
   {
      return (this.message);
   }



   /*******************************************************************************
    * Setter for message
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    * Fluent setter for message
    *******************************************************************************/
   public WorkflowTestOutput withMessage(String message)
   {
      this.message = message;
      return (this);
   }

}