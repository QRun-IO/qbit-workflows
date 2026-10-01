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
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


/*******************************************************************************
 ** QRecord Entity for WorkflowTestRunScenario table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowTestRunScenario.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowTestOutput.class,
         joinFieldName = "workflowTestRunScenarioId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Outputs", enabled = true, maxRows = 250))
   }
)
public class WorkflowTestRunScenario extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME               = "workflowTestRunScenario";
   public static final String ASSOCIATION_NAME_OUTPUTS = "outputs";



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
         String outputChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowTestOutput.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("biotech"))
            .withRecordLabelFormat("%s (%s)")
            .withRecordLabelFields("workflowTestScenarioId", "workflowTestRunId")
            .withSection(SectionFactory.defaultT1("id", "workflowTestScenarioId", "workflowTestRunId", "workflowId", "workflowRevisionId"))
            .withSection(SectionFactory.customT2("status", new QIcon("traffic"), "status", "message", "workflowRunLogId").withGridColumns(6))
            .withSection(SectionFactory.customT2("assertions", new QIcon("checklist"), "assertionCount", "assertionPassCount", "assertionFailCount").withGridColumns(6))
            .withSection(SectionFactory.customT2("outputs", new QIcon("output")).withWidgetName(outputChildJoinName))
            .withSection(SectionFactory.customT2("outputData", new QIcon("data_object"), "outputData"))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_OUTPUTS).withAssociatedTableName(WorkflowTestOutput.TABLE_NAME).withJoinName(outputChildJoinName))
         ;

         table.withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE);

         table.getField("outputData")
            .withGridColumns(12)
            .withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

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

   @QField(possibleValueSourceName = Workflow.TABLE_NAME)
   private Integer workflowId;

   @QField(possibleValueSourceName = WorkflowRevision.TABLE_NAME)
   private Integer workflowRevisionId;

   @QField(possibleValueSourceName = WorkflowRunLog.TABLE_NAME)
   private Long workflowRunLogId;

   @QField(possibleValueSourceName = WorkflowTestScenario.TABLE_NAME)
   private Integer workflowTestScenarioId;

   @QField(possibleValueSourceName = WorkflowTestRun.TABLE_NAME)
   private Integer workflowTestRunId;

   @QField(possibleValueSourceName = WorkflowTestStatus.NAME)
   private Integer status;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String message;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer assertionCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer assertionPassCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer assertionFailCount;

   @QField()
   private String outputData;

   @QAssociation(name = ASSOCIATION_NAME_OUTPUTS)
   private List<WorkflowTestOutput> outputs;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowTestRunScenario()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowTestRunScenario(QRecord record)
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
   public WorkflowTestRunScenario withId(Integer id)
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
   public WorkflowTestRunScenario withCreateDate(Instant createDate)
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
   public WorkflowTestRunScenario withModifyDate(Instant modifyDate)
   {
      setModifyDate(modifyDate);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowId
    *******************************************************************************/
   public Integer getWorkflowId()
   {
      return (workflowId);
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
   public WorkflowTestRunScenario withWorkflowId(Integer workflowId)
   {
      setWorkflowId(workflowId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRevisionId
    *******************************************************************************/
   public Integer getWorkflowRevisionId()
   {
      return (workflowRevisionId);
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
   public WorkflowTestRunScenario withWorkflowRevisionId(Integer workflowRevisionId)
   {
      setWorkflowRevisionId(workflowRevisionId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRunLogId
    *******************************************************************************/
   public Long getWorkflowRunLogId()
   {
      return (workflowRunLogId);
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
   public WorkflowTestRunScenario withWorkflowRunLogId(Long workflowRunLogId)
   {
      setWorkflowRunLogId(workflowRunLogId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowTestScenarioId
    *******************************************************************************/
   public Integer getWorkflowTestScenarioId()
   {
      return (workflowTestScenarioId);
   }



   /*******************************************************************************
    ** Setter for workflowTestScenarioId
    *******************************************************************************/
   public void setWorkflowTestScenarioId(Integer workflowTestScenarioId)
   {
      this.workflowTestScenarioId = workflowTestScenarioId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTestScenarioId
    *******************************************************************************/
   public WorkflowTestRunScenario withWorkflowTestScenarioId(Integer workflowTestScenarioId)
   {
      setWorkflowTestScenarioId(workflowTestScenarioId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowTestRunId
    *******************************************************************************/
   public Integer getWorkflowTestRunId()
   {
      return (workflowTestRunId);
   }



   /*******************************************************************************
    ** Setter for workflowTestRunId
    *******************************************************************************/
   public void setWorkflowTestRunId(Integer workflowTestRunId)
   {
      this.workflowTestRunId = workflowTestRunId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTestRunId
    *******************************************************************************/
   public WorkflowTestRunScenario withWorkflowTestRunId(Integer workflowTestRunId)
   {
      setWorkflowTestRunId(workflowTestRunId);
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
   public WorkflowTestRunScenario withStatus(Integer status)
   {
      setStatus(status);
      return (this);
   }



   /*******************************************************************************
    ** Getter for assertionCount
    *******************************************************************************/
   public Integer getAssertionCount()
   {
      return (assertionCount);
   }



   /*******************************************************************************
    ** Setter for assertionCount
    *******************************************************************************/
   public void setAssertionCount(Integer assertionCount)
   {
      this.assertionCount = assertionCount;
   }



   /*******************************************************************************
    ** Fluent setter for assertionCount
    *******************************************************************************/
   public WorkflowTestRunScenario withAssertionCount(Integer assertionCount)
   {
      setAssertionCount(assertionCount);
      return (this);
   }



   /*******************************************************************************
    ** Getter for assertionPassCount
    *******************************************************************************/
   public Integer getAssertionPassCount()
   {
      return (assertionPassCount);
   }



   /*******************************************************************************
    ** Setter for assertionPassCount
    *******************************************************************************/
   public void setAssertionPassCount(Integer assertionPassCount)
   {
      this.assertionPassCount = assertionPassCount;
   }



   /*******************************************************************************
    ** Fluent setter for assertionPassCount
    *******************************************************************************/
   public WorkflowTestRunScenario withAssertionPassCount(Integer assertionPassCount)
   {
      setAssertionPassCount(assertionPassCount);
      return (this);
   }



   /*******************************************************************************
    ** Getter for assertionFailCount
    *******************************************************************************/
   public Integer getAssertionFailCount()
   {
      return (assertionFailCount);
   }



   /*******************************************************************************
    ** Setter for assertionFailCount
    *******************************************************************************/
   public void setAssertionFailCount(Integer assertionFailCount)
   {
      this.assertionFailCount = assertionFailCount;
   }



   /*******************************************************************************
    ** Fluent setter for assertionFailCount
    *******************************************************************************/
   public WorkflowTestRunScenario withAssertionFailCount(Integer assertionFailCount)
   {
      setAssertionFailCount(assertionFailCount);
      return (this);
   }



   /*******************************************************************************
    * Getter for outputs
    *******************************************************************************/
   public List<WorkflowTestOutput> getOutputs()
   {
      return (this.outputs);
   }



   /*******************************************************************************
    * Setter for outputs
    *******************************************************************************/
   public void setOutputs(List<WorkflowTestOutput> outputs)
   {
      this.outputs = outputs;
   }



   /*******************************************************************************
    * Fluent setter for outputs
    *******************************************************************************/
   public WorkflowTestRunScenario withOutputs(List<WorkflowTestOutput> outputs)
   {
      this.outputs = outputs;
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
   public WorkflowTestRunScenario withMessage(String message)
   {
      this.message = message;
      return (this);
   }



   /*******************************************************************************
    * Getter for outputData
    *******************************************************************************/
   public String getOutputData()
   {
      return (this.outputData);
   }



   /*******************************************************************************
    * Setter for outputData
    *******************************************************************************/
   public void setOutputData(String outputData)
   {
      this.outputData = outputData;
   }



   /*******************************************************************************
    * Fluent setter for outputData
    *******************************************************************************/
   public WorkflowTestRunScenario withOutputData(String outputData)
   {
      this.outputData = outputData;
      return (this);
   }

}