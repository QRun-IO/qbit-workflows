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
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
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
 ** QRecord Entity for WorkflowTestRun table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowTestRun.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowTestRunScenario.class,
         joinFieldName = "workflowTestRunId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Run Scenarios", enabled = true, maxRows = 50, widgetMetaDataCustomizer = WorkflowTestRun.ScenarioChildListWidgetCustomizer.class))
   }
)
public class WorkflowTestRun extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowTestRun";

   public static final String ASSOCIATION_NAME_SCENARIOS = "scenarios";



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
         String scenarioChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowTestRunScenario.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("play_arrow"))
            .withRecordLabelFormat("%s (Run %s)")
            .withRecordLabelFields("workflowId", "id")
            .withSection(SectionFactory.defaultT1("id", "workflowId", "workflowRevisionId"))
            .withSection(SectionFactory.customT2("status", new QIcon("traffic"), "status").withGridColumns(4))
            .withSection(SectionFactory.customT2("scenarios", new QIcon("science"), "scenarioCount", "scenarioPassCount", "scenarioFailCount").withGridColumns(4))
            .withSection(SectionFactory.customT2("assertions", new QIcon("checklist"), "assertionCount", "assertionPassCount", "assertionFailCount").withGridColumns(4))
            .withSection(SectionFactory.customT2("runScenarios", new QIcon("biotech")).withWidgetName(scenarioChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_SCENARIOS).withAssociatedTableName(WorkflowTestRunScenario.TABLE_NAME).withJoinName(scenarioChildJoinName))
         ;

         table.withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE);

         WorkflowTestStatus.customizeFieldWitChipAndWidth(table.getField("status"));

         return (table);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class ScenarioChildListWidgetCustomizer implements MetaDataCustomizerInterface<QWidgetMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QWidgetMetaData customizeMetaData(QInstance qInstance, QWidgetMetaData widget) throws QException
      {
         widget.withDefaultValue("orderBy", new ArrayList<>(List.of(new QFilterOrderBy("id", false))));
         widget.withDefaultValue("omitFieldNames", new ArrayList<>(List.of("workflowId", "workflowRevisionId")));
         return widget;
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

   @QField(possibleValueSourceName = WorkflowTestStatus.NAME)
   private Integer status;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer scenarioCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer scenarioPassCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer scenarioFailCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer assertionCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer assertionPassCount;

   @QField(displayFormat = DisplayFormat.COMMAS)
   private Integer assertionFailCount;

   @QAssociation(name = ASSOCIATION_NAME_SCENARIOS)
   private List<WorkflowTestRunScenario> scenarios;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowTestRun()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowTestRun(QRecord record)
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
   public WorkflowTestRun withId(Integer id)
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
   public WorkflowTestRun withCreateDate(Instant createDate)
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
   public WorkflowTestRun withModifyDate(Instant modifyDate)
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
   public WorkflowTestRun withWorkflowId(Integer workflowId)
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
   public WorkflowTestRun withWorkflowRevisionId(Integer workflowRevisionId)
   {
      setWorkflowRevisionId(workflowRevisionId);
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
   public WorkflowTestRun withStatus(Integer status)
   {
      setStatus(status);
      return (this);
   }



   /*******************************************************************************
    ** Getter for scenarioCount
    *******************************************************************************/
   public Integer getScenarioCount()
   {
      return (scenarioCount);
   }



   /*******************************************************************************
    ** Setter for scenarioCount
    *******************************************************************************/
   public void setScenarioCount(Integer scenarioCount)
   {
      this.scenarioCount = scenarioCount;
   }



   /*******************************************************************************
    ** Fluent setter for scenarioCount
    *******************************************************************************/
   public WorkflowTestRun withScenarioCount(Integer scenarioCount)
   {
      setScenarioCount(scenarioCount);
      return (this);
   }



   /*******************************************************************************
    ** Getter for scenarioPassCount
    *******************************************************************************/
   public Integer getScenarioPassCount()
   {
      return (scenarioPassCount);
   }



   /*******************************************************************************
    ** Setter for scenarioPassCount
    *******************************************************************************/
   public void setScenarioPassCount(Integer scenarioPassCount)
   {
      this.scenarioPassCount = scenarioPassCount;
   }



   /*******************************************************************************
    ** Fluent setter for scenarioPassCount
    *******************************************************************************/
   public WorkflowTestRun withScenarioPassCount(Integer scenarioPassCount)
   {
      setScenarioPassCount(scenarioPassCount);
      return (this);
   }



   /*******************************************************************************
    ** Getter for scenarioFailCount
    *******************************************************************************/
   public Integer getScenarioFailCount()
   {
      return (scenarioFailCount);
   }



   /*******************************************************************************
    ** Setter for scenarioFailCount
    *******************************************************************************/
   public void setScenarioFailCount(Integer scenarioFailCount)
   {
      this.scenarioFailCount = scenarioFailCount;
   }



   /*******************************************************************************
    ** Fluent setter for scenarioFailCount
    *******************************************************************************/
   public WorkflowTestRun withScenarioFailCount(Integer scenarioFailCount)
   {
      setScenarioFailCount(scenarioFailCount);
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
   public WorkflowTestRun withAssertionCount(Integer assertionCount)
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
   public WorkflowTestRun withAssertionPassCount(Integer assertionPassCount)
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
   public WorkflowTestRun withAssertionFailCount(Integer assertionFailCount)
   {
      setAssertionFailCount(assertionFailCount);
      return (this);
   }



   /*******************************************************************************
    * Getter for scenarios
    *******************************************************************************/
   public List<WorkflowTestRunScenario> getScenarios()
   {
      return (this.scenarios);
   }



   /*******************************************************************************
    * Setter for scenarios
    *******************************************************************************/
   public void setScenarios(List<WorkflowTestRunScenario> scenarios)
   {
      this.scenarios = scenarios;
   }



   /*******************************************************************************
    * Fluent setter for scenarios
    *******************************************************************************/
   public WorkflowTestRun withScenarios(List<WorkflowTestRunScenario> scenarios)
   {
      this.scenarios = scenarios;
      return (this);
   }

}