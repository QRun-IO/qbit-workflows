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
import com.kingsrook.qbits.workflows.metadata.WorkflowEditorWidget;
import com.kingsrook.qbits.workflows.metadata.WorkflowTypePossibleValueSource;
import com.kingsrook.qbits.workflows.tables.WorkflowTableCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildJoin;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildRecordListWidget;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.ChildTable;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.TablesPossibleValueSourceMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** QRecord Entity for Workflow table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = Workflow.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowRevision.class,
         joinFieldName = "workflowId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Revisions", enabled = true, maxRows = 50, widgetMetaDataCustomizer = Workflow.OrderByIdDescChildListWidgetCustomizer.class)),
      @ChildTable(
         childTableEntityClass = WorkflowRunLog.class,
         joinFieldName = "workflowId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Run Logs", enabled = true, maxRows = 50, widgetMetaDataCustomizer = Workflow.OrderByIdDescChildListWidgetCustomizer.class)),
      @ChildTable(
         childTableEntityClass = WorkflowTestScenario.class,
         joinFieldName = "workflowId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Test Scenarios", enabled = true, maxRows = 50, canAddChildRecords = true)),
      @ChildTable(
         childTableEntityClass = WorkflowTestRun.class,
         joinFieldName = "workflowId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Test Runs", enabled = true, maxRows = 50, widgetMetaDataCustomizer = Workflow.OrderByIdDescChildListWidgetCustomizer.class))
   }
)
public class Workflow extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflow";



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
         String revisionsChildJoinName     = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowRevision.TABLE_NAME);
         String runLogsChildJoinName       = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowRunLog.TABLE_NAME);
         String testScenariosChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowTestScenario.TABLE_NAME);
         String testRunsChildJoinName      = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowTestRun.TABLE_NAME);

         table
            .withUniqueKey(new UniqueKey("name"))
            .withIcon(new QIcon().withName("account_tree"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withSection(SectionFactory.defaultT1("id", "name"))
            .withSection(SectionFactory.customT2("workflowEditorWidget", new QIcon("account_tree")).withLabel("Workflow Steps").withWidgetName(WorkflowEditorWidget.NAME))
            .withSection(SectionFactory.defaultT2("workflowTypeName", "tableName", "currentWorkflowRevisionId"))
            .withSection(SectionFactory.customT2("revisions", new QIcon("schema")).withWidgetName(revisionsChildJoinName))
            .withSection(SectionFactory.customT2("runLogs", new QIcon("receipt_long")).withWidgetName(runLogsChildJoinName))
            .withSection(SectionFactory.customT2("testScenarios", new QIcon("science")).withWidgetName(testScenariosChildJoinName))
            .withSection(SectionFactory.customT2("testRuns", new QIcon("play_arrow")).withWidgetName(testRunsChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         table.withCustomizer(TableCustomizers.PRE_INSERT_RECORD, new QCodeReference(WorkflowTableCustomizer.class));

         return (table);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class OrderByIdDescChildListWidgetCustomizer implements MetaDataCustomizerInterface<QWidgetMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QWidgetMetaData customizeMetaData(QInstance qInstance, QWidgetMetaData widget) throws QException
      {
         widget.withDefaultValue("orderBy", new ArrayList<>(List.of(new QFilterOrderBy("id", false))));
         return widget;
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, isRequired = true)
   private String name;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = WorkflowTypePossibleValueSource.NAME, label = "Workflow Type", isRequired = true)
   private String workflowTypeName;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = TablesPossibleValueSourceMetaDataProvider.NAME, label = "Table")
   private String tableName;

   @QField(isEditable = false, possibleValueSourceName = WorkflowRevision.TABLE_NAME)
   private Integer currentWorkflowRevisionId;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public Workflow()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public Workflow(QRecord record)
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
   public Workflow withId(Integer id)
   {
      this.id = id;
      return (this);
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
   public Workflow withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for currentWorkflowRevisionId
    *******************************************************************************/
   public Integer getCurrentWorkflowRevisionId()
   {
      return (this.currentWorkflowRevisionId);
   }



   /*******************************************************************************
    ** Setter for currentWorkflowRevisionId
    *******************************************************************************/
   public void setCurrentWorkflowRevisionId(Integer currentWorkflowRevisionId)
   {
      this.currentWorkflowRevisionId = currentWorkflowRevisionId;
   }



   /*******************************************************************************
    ** Fluent setter for currentWorkflowRevisionId
    *******************************************************************************/
   public Workflow withCurrentWorkflowRevisionId(Integer currentWorkflowRevisionId)
   {
      this.currentWorkflowRevisionId = currentWorkflowRevisionId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (this.createDate);
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
   public Workflow withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (this.modifyDate);
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
   public Workflow withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowTypeName
    *******************************************************************************/
   public String getWorkflowTypeName()
   {
      return (this.workflowTypeName);
   }



   /*******************************************************************************
    ** Setter for workflowTypeName
    *******************************************************************************/
   public void setWorkflowTypeName(String workflowTypeName)
   {
      this.workflowTypeName = workflowTypeName;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTypeName
    *******************************************************************************/
   public Workflow withWorkflowTypeName(String workflowTypeName)
   {
      this.workflowTypeName = workflowTypeName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for tableName
    *******************************************************************************/
   public String getTableName()
   {
      return (this.tableName);
   }



   /*******************************************************************************
    ** Setter for tableName
    *******************************************************************************/
   public void setTableName(String tableName)
   {
      this.tableName = tableName;
   }



   /*******************************************************************************
    ** Fluent setter for tableName
    *******************************************************************************/
   public Workflow withTableName(String tableName)
   {
      this.tableName = tableName;
      return (this);
   }

}
