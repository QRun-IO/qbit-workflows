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
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


/*******************************************************************************
 ** QRecord Entity for WorkflowRunLogStep table
 *******************************************************************************/
@QMetaDataProducingEntity(
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowRunLogStep.TableMetaDataCustomizer.class
)
public class WorkflowRunLogStep extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowRunLogStep";



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
            .withIcon(new QIcon().withName("reorder"))
            .withRecordLabelFormat("Step - %s (%s)")
            .withRecordLabelFields("seqNo", "workflowRunLogId")
            .withSection(SectionFactory.defaultT1("id", "workflowRunLogId", "seqNo"))
            .withSection(SectionFactory.defaultT2("workflowStepId", "message", "outputData"))
            .withSection(SectionFactory.defaultT3("startTimestamp", "endTimestamp"));

         table.getField("id").withFieldAdornment(AdornmentType.Size.SMALL.toAdornment());
         table.getField("seqNo").withFieldAdornment(AdornmentType.Size.XSMALL.toAdornment());
         table.getField("workflowStepId").withFieldAdornment(AdornmentType.Size.LARGE.toAdornment());
         table.getField("message").withFieldAdornment(AdornmentType.Size.LARGE.toAdornment());

         return (table);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class RevisionChildListWidgetCustomizer implements MetaDataCustomizerInterface<QWidgetMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QWidgetMetaData customizeMetaData(QInstance qInstance, QWidgetMetaData widget) throws QException
      {
         widget.withDefaultValue("orderBy", new ArrayList<>(List.of(new QFilterOrderBy("seqNo", false))));
         return widget;
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(possibleValueSourceName = WorkflowRunLog.TABLE_NAME)
   private Long workflowRunLogId;

   @QField(possibleValueSourceName = WorkflowStep.TABLE_NAME)
   private Integer workflowStepId;

   @QField()
   private Integer seqNo;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String outputData;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String message;

   @QField(isEditable = false)
   private Instant startTimestamp;

   @QField(isEditable = false)
   private Instant endTimestamp;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowRunLogStep()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowRunLogStep(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Long getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Long id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public WorkflowRunLogStep withId(Long id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRunLogId
    *******************************************************************************/
   public Long getWorkflowRunLogId()
   {
      return (this.workflowRunLogId);
   }



   /*******************************************************************************
    ** Setter for workflowRunLogId
    *******************************************************************************/
   public void setWorkflowRunLogId(Long workflowRunLogId)
   {
      this.workflowRunLogId = workflowRunLogId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowRunLogId
    *******************************************************************************/
   public WorkflowRunLogStep withWorkflowRunLogId(Long workflowRunLogId)
   {
      this.workflowRunLogId = workflowRunLogId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowStepId
    *******************************************************************************/
   public Integer getWorkflowStepId()
   {
      return (this.workflowStepId);
   }



   /*******************************************************************************
    ** Setter for workflowStepId
    *******************************************************************************/
   public void setWorkflowStepId(Integer workflowStepId)
   {
      this.workflowStepId = workflowStepId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowStepId
    *******************************************************************************/
   public WorkflowRunLogStep withWorkflowStepId(Integer workflowStepId)
   {
      this.workflowStepId = workflowStepId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for seqNo
    *******************************************************************************/
   public Integer getSeqNo()
   {
      return (this.seqNo);
   }



   /*******************************************************************************
    ** Setter for seqNo
    *******************************************************************************/
   public void setSeqNo(Integer seqNo)
   {
      this.seqNo = seqNo;
   }



   /*******************************************************************************
    ** Fluent setter for seqNo
    *******************************************************************************/
   public WorkflowRunLogStep withSeqNo(Integer seqNo)
   {
      this.seqNo = seqNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputData
    *******************************************************************************/
   public String getOutputData()
   {
      return (this.outputData);
   }



   /*******************************************************************************
    ** Setter for outputData
    *******************************************************************************/
   public void setOutputData(String outputData)
   {
      this.outputData = outputData;
   }



   /*******************************************************************************
    ** Fluent setter for outputData
    *******************************************************************************/
   public WorkflowRunLogStep withOutputData(String outputData)
   {
      this.outputData = outputData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startTimestamp
    *******************************************************************************/
   public Instant getStartTimestamp()
   {
      return (this.startTimestamp);
   }



   /*******************************************************************************
    ** Setter for startTimestamp
    *******************************************************************************/
   public void setStartTimestamp(Instant startTimestamp)
   {
      this.startTimestamp = startTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for startTimestamp
    *******************************************************************************/
   public WorkflowRunLogStep withStartTimestamp(Instant startTimestamp)
   {
      this.startTimestamp = startTimestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for endTimestamp
    *******************************************************************************/
   public Instant getEndTimestamp()
   {
      return (this.endTimestamp);
   }



   /*******************************************************************************
    ** Setter for endTimestamp
    *******************************************************************************/
   public void setEndTimestamp(Instant endTimestamp)
   {
      this.endTimestamp = endTimestamp;
   }



   /*******************************************************************************
    ** Fluent setter for endTimestamp
    *******************************************************************************/
   public WorkflowRunLogStep withEndTimestamp(Instant endTimestamp)
   {
      this.endTimestamp = endTimestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for message
    *******************************************************************************/
   public String getMessage()
   {
      return (this.message);
   }



   /*******************************************************************************
    ** Setter for message
    *******************************************************************************/
   public void setMessage(String message)
   {
      this.message = message;
   }



   /*******************************************************************************
    ** Fluent setter for message
    *******************************************************************************/
   public WorkflowRunLogStep withMessage(String message)
   {
      this.message = message;
      return (this);
   }

}
