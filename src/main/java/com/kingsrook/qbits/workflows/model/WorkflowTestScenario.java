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
import com.kingsrook.qbits.workflows.tables.WorkflowTestScenarioTableCustomizer;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataProvider;
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** QRecord Entity for WorkflowTestScenario table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowTestScenario.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowTestAssertion.class,
         joinFieldName = "workflowTestScenarioId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Assertions", enabled = true, maxRows = 50, canAddChildRecords = true, manageAssociationName = WorkflowTestScenario.ASSOCIATION_NAME_ASSERTIONS)),
      @ChildTable(
         childTableEntityClass = WorkflowTestRunScenario.class,
         joinFieldName = "workflowTestScenarioId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Runs", enabled = true, maxRows = 50, widgetMetaDataCustomizer = WorkflowTestScenario.RunChildListWidgetCustomizer.class))
   }
)
public class WorkflowTestScenario extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME                  = "workflowTestScenario";
   public static final String ASSOCIATION_NAME_ASSERTIONS = "assertions";



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
         String assertionChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowTestAssertion.TABLE_NAME);
         String runChildJoinName       = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowTestRunScenario.TABLE_NAME);

         table
            .withIcon(new QIcon().withName("science"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withUniqueKey(new UniqueKey("workflowId", "name"))
            .withSection(SectionFactory.defaultT1("id", "workflowId", "name"))
            .withSection(SectionFactory.defaultT2("sourceRecordId").withName("sourceRecord").withIcon(new QIcon("drive_file_move_outline")))
            .withSection(SectionFactory.defaultT2("apiName", "apiVersion", "apiJson").withName("apiDetails").withIcon(new QIcon("data_object")))
            .withSection(SectionFactory.customT2("assertions", new QIcon("checklist")).withWidgetName(assertionChildJoinName))
            .withSection(SectionFactory.customT2("runs", new QIcon("play_arrow")).withWidgetName(runChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_ASSERTIONS).withAssociatedTableName(WorkflowTestAssertion.TABLE_NAME).withJoinName(assertionChildJoinName))
         ;

         table.withCustomizer(TableCustomizers.PRE_INSERT_RECORD, new QCodeReference(WorkflowTestScenarioTableCustomizer.class));
         table.withCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(WorkflowTestScenarioTableCustomizer.class));

         table.getField("apiJson")
            .withGridColumns(12)
            .withFieldAdornment(new FieldAdornment(AdornmentType.CODE_EDITOR).withValue(AdornmentType.CodeEditorValues.languageMode("json")));

         return (table);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class RunChildListWidgetCustomizer implements MetaDataCustomizerInterface<QWidgetMetaData>
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

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(possibleValueSourceName = Workflow.TABLE_NAME, isRequired = true)
   private Integer workflowId;

   @QField(maxLength = 250, valueTooLongBehavior = ValueTooLongBehavior.ERROR, isRequired = true)
   private String name;

   @QField()
   private Integer sourceRecordId;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = ApiInstanceMetaDataProvider.API_NAME_PVS_NAME)
   private String apiName;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = ApiInstanceMetaDataProvider.API_VERSION_PVS_NAME)
   private String apiVersion;

   @QField()
   private String apiJson;

   @QAssociation(name = ASSOCIATION_NAME_ASSERTIONS)
   private List<WorkflowTestAssertion> assertions;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowTestScenario()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowTestScenario(QRecord record)
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
   public WorkflowTestScenario withId(Integer id)
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
   public WorkflowTestScenario withCreateDate(Instant createDate)
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
   public WorkflowTestScenario withModifyDate(Instant modifyDate)
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
   public WorkflowTestScenario withWorkflowId(Integer workflowId)
   {
      setWorkflowId(workflowId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (name);
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
   public WorkflowTestScenario withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceRecordId
    *******************************************************************************/
   public Integer getSourceRecordId()
   {
      return (sourceRecordId);
   }



   /*******************************************************************************
    ** Setter for sourceRecordId
    *******************************************************************************/
   public void setSourceRecordId(Integer sourceRecordId)
   {
      this.sourceRecordId = sourceRecordId;
   }



   /*******************************************************************************
    ** Fluent setter for sourceRecordId
    *******************************************************************************/
   public WorkflowTestScenario withSourceRecordId(Integer sourceRecordId)
   {
      setSourceRecordId(sourceRecordId);
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiName
    *******************************************************************************/
   public String getApiName()
   {
      return (apiName);
   }



   /*******************************************************************************
    ** Setter for apiName
    *******************************************************************************/
   public void setApiName(String apiName)
   {
      this.apiName = apiName;
   }



   /*******************************************************************************
    ** Fluent setter for apiName
    *******************************************************************************/
   public WorkflowTestScenario withApiName(String apiName)
   {
      setApiName(apiName);
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiVersion
    *******************************************************************************/
   public String getApiVersion()
   {
      return (apiVersion);
   }



   /*******************************************************************************
    ** Setter for apiVersion
    *******************************************************************************/
   public void setApiVersion(String apiVersion)
   {
      this.apiVersion = apiVersion;
   }



   /*******************************************************************************
    ** Fluent setter for apiVersion
    *******************************************************************************/
   public WorkflowTestScenario withApiVersion(String apiVersion)
   {
      setApiVersion(apiVersion);
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiJson
    *******************************************************************************/
   public String getApiJson()
   {
      return (apiJson);
   }



   /*******************************************************************************
    ** Setter for apiJson
    *******************************************************************************/
   public void setApiJson(String apiJson)
   {
      this.apiJson = apiJson;
   }



   /*******************************************************************************
    ** Fluent setter for apiJson
    *******************************************************************************/
   public WorkflowTestScenario withApiJson(String apiJson)
   {
      setApiJson(apiJson);
      return (this);
   }



   /*******************************************************************************
    * Getter for assertions
    *******************************************************************************/
   public List<WorkflowTestAssertion> getAssertions()
   {
      return (this.assertions);
   }



   /*******************************************************************************
    * Setter for assertions
    *******************************************************************************/
   public void setAssertions(List<WorkflowTestAssertion> assertions)
   {
      this.assertions = assertions;
   }



   /*******************************************************************************
    * Fluent setter for assertions
    *******************************************************************************/
   public WorkflowTestScenario withAssertions(List<WorkflowTestAssertion> assertions)
   {
      this.assertions = assertions;
      return (this);
   }

}