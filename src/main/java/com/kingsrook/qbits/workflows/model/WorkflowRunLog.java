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
import com.kingsrook.qbits.workflows.metadata.WorkflowRunLogViewerWidget;
import com.kingsrook.qbits.workflows.tables.WorkflowRunLogTableCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.tables.QQQTable;
import static com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType.ChipValues.iconAndColorValues;


/*******************************************************************************
 ** QRecord Entity for WorkflowRunLog table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowRunLog.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowRunLogStep.class,
         joinFieldName = "workflowRunLogId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Steps", enabled = true, maxRows = 250, widgetMetaDataCustomizer = WorkflowRunLog.RevisionChildListWidgetCustomizer.class))
   }
)
public class WorkflowRunLog extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowRunLog";

   public static final String STEPS_ASSOCIATION_NAME = "workflowRunLogSteps";



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
         String childJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowRunLogStep.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("receipt_long"))
            .withRecordLabelFormat("%s (%s)")
            .withRecordLabelFields("id", "workflowRevisionId")
            .withSection(SectionFactory.defaultT1("id", "workflowId", "workflowRevisionId"))
            .withSection(SectionFactory.customT2("workflowRunLogViewerWidget", new QIcon("account_tree")).withLabel("Workflow Steps").withWidgetName(WorkflowRunLogViewerWidget.NAME))
            .withSection(SectionFactory.defaultT2("inputRecordQqqTableId", "inputRecordId", "inputDataJson").withName("input").withGridColumns(6))
            .withSection(SectionFactory.defaultT2("hadError", "errorMessage").withName("output").withGridColumns(6))
            .withSection(SectionFactory.customT2("steps", new QIcon("polyline")).withWidgetName(childJoinName))
            .withSection(SectionFactory.defaultT3("startTimestamp", "endTimestamp")).
            withAssociation(new Association().withName(STEPS_ASSOCIATION_NAME).withJoinName(childJoinName).withAssociatedTableName(WorkflowRunLogStep.TABLE_NAME));

         table.getField("hadError").withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
            .withValues(iconAndColorValues(false, "done", AdornmentType.ChipValues.COLOR_SUCCESS))
            .withValues(iconAndColorValues(true, "error", AdornmentType.ChipValues.COLOR_ERROR)));

         table.withCustomizer(TableCustomizers.POST_QUERY_RECORD, new QCodeReference(WorkflowRunLogTableCustomizer.class));
         table.getField("inputRecordId").withFieldAdornment(new FieldAdornment(AdornmentType.LINK).withValue(AdornmentType.LinkValues.TO_RECORD_FROM_TABLE_DYNAMIC, true));

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
         widget.withDefaultValue("orderBy", new ArrayList<>(List.of(new QFilterOrderBy("seqNo", true))));
         return widget;
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Long id;

   @QField(possibleValueSourceName = Workflow.TABLE_NAME)
   private Integer workflowId;

   @QField(possibleValueSourceName = WorkflowRevision.TABLE_NAME)
   private Integer workflowRevisionId;

   @QField()
   private Boolean hadError;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String inputDataJson;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String errorMessage;

   @QField(label = "Input Record Table", possibleValueSourceName = QQQTable.TABLE_NAME)
   private Integer inputRecordQqqTableId;

   @QField()
   private Integer inputRecordId;

   @QField(isEditable = false)
   private Instant startTimestamp;

   @QField(isEditable = false)
   private Instant endTimestamp;

   @QAssociation(name = STEPS_ASSOCIATION_NAME)
   private List<WorkflowRunLogStep> steps;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowRunLog()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowRunLog(QRecord record)
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
   public WorkflowRunLog withId(Long id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowId
    *******************************************************************************/
   public Integer getWorkflowId()
   {
      return (this.workflowId);
   }



   /*******************************************************************************
    ** Setter for workflowId
    *******************************************************************************/
   public void setWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowId
    *******************************************************************************/
   public WorkflowRunLog withWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
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
   public WorkflowRunLog withWorkflowRevisionId(Integer workflowRevisionId)
   {
      this.workflowRevisionId = workflowRevisionId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputDataJson
    *******************************************************************************/
   public String getInputDataJson()
   {
      return (this.inputDataJson);
   }



   /*******************************************************************************
    ** Setter for inputDataJson
    *******************************************************************************/
   public void setInputDataJson(String inputDataJson)
   {
      this.inputDataJson = inputDataJson;
   }



   /*******************************************************************************
    ** Fluent setter for inputDataJson
    *******************************************************************************/
   public WorkflowRunLog withInputDataJson(String inputDataJson)
   {
      this.inputDataJson = inputDataJson;
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
   public WorkflowRunLog withStartTimestamp(Instant startTimestamp)
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
   public WorkflowRunLog withEndTimestamp(Instant endTimestamp)
   {
      this.endTimestamp = endTimestamp;
      return (this);
   }



   /*******************************************************************************
    ** Getter for steps
    *******************************************************************************/
   public List<WorkflowRunLogStep> getSteps()
   {
      return (this.steps);
   }



   /*******************************************************************************
    ** Setter for steps
    *******************************************************************************/
   public void setSteps(List<WorkflowRunLogStep> steps)
   {
      this.steps = steps;
   }



   /*******************************************************************************
    ** Fluent setter for steps
    *******************************************************************************/
   public WorkflowRunLog withSteps(List<WorkflowRunLogStep> steps)
   {
      this.steps = steps;
      return (this);
   }



   /*******************************************************************************
    ** Getter for hadError
    *******************************************************************************/
   public Boolean getHadError()
   {
      return (this.hadError);
   }



   /*******************************************************************************
    ** Setter for hadError
    *******************************************************************************/
   public void setHadError(Boolean hadError)
   {
      this.hadError = hadError;
   }



   /*******************************************************************************
    ** Fluent setter for hadError
    *******************************************************************************/
   public WorkflowRunLog withHadError(Boolean hadError)
   {
      this.hadError = hadError;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputRecordQqqTableId
    *******************************************************************************/
   public Integer getInputRecordQqqTableId()
   {
      return (this.inputRecordQqqTableId);
   }



   /*******************************************************************************
    ** Setter for inputRecordQqqTableId
    *******************************************************************************/
   public void setInputRecordQqqTableId(Integer inputRecordQqqTableId)
   {
      this.inputRecordQqqTableId = inputRecordQqqTableId;
   }



   /*******************************************************************************
    ** Fluent setter for inputRecordQqqTableId
    *******************************************************************************/
   public WorkflowRunLog withInputRecordQqqTableId(Integer inputRecordQqqTableId)
   {
      this.inputRecordQqqTableId = inputRecordQqqTableId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputRecordId
    *******************************************************************************/
   public Integer getInputRecordId()
   {
      return (this.inputRecordId);
   }



   /*******************************************************************************
    ** Setter for inputRecordId
    *******************************************************************************/
   public void setInputRecordId(Integer inputRecordId)
   {
      this.inputRecordId = inputRecordId;
   }



   /*******************************************************************************
    ** Fluent setter for inputRecordId
    *******************************************************************************/
   public WorkflowRunLog withInputRecordId(Integer inputRecordId)
   {
      this.inputRecordId = inputRecordId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for errorMessage
    *******************************************************************************/
   public String getErrorMessage()
   {
      return (this.errorMessage);
   }



   /*******************************************************************************
    ** Setter for errorMessage
    *******************************************************************************/
   public void setErrorMessage(String errorMessage)
   {
      this.errorMessage = errorMessage;
   }



   /*******************************************************************************
    ** Fluent setter for errorMessage
    *******************************************************************************/
   public WorkflowRunLog withErrorMessage(String errorMessage)
   {
      this.errorMessage = errorMessage;
      return (this);
   }

}
