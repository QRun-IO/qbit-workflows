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
import com.kingsrook.qbits.workflows.metadata.WorkflowRevisionViewerWidget;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QAssociation;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
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
 ** QRecord Entity for WorkflowRevision table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowRevision.TableMetaDataCustomizer.class,
   childTables = {
      @ChildTable(
         childTableEntityClass = WorkflowStep.class,
         joinFieldName = "workflowRevisionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Steps", enabled = true, maxRows = 250)),
      @ChildTable(
         childTableEntityClass = WorkflowLink.class,
         joinFieldName = "workflowRevisionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Links", enabled = true, maxRows = 250)),
      @ChildTable(
         childTableEntityClass = WorkflowRunLog.class,
         joinFieldName = "workflowRevisionId",
         childJoin = @ChildJoin(enabled = true),
         childRecordListWidget = @ChildRecordListWidget(label = "Run Logs", enabled = true, maxRows = 50, widgetMetaDataCustomizer = WorkflowRevision.RunLogChildListWidgetCustomizer.class))
   }
)
public class WorkflowRevision extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowRevision";

   public static final String ASSOCIATION_NAME_WORKFLOW_STEP = "steps";
   public static final String ASSOCIATION_NAME_WORKFLOW_LINK = "links";



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
         String stepChildJoinName    = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowStep.TABLE_NAME);
         String linkChildJoinName    = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowLink.TABLE_NAME);
         String runLogsChildJoinName = QJoinMetaData.makeInferredJoinName(TABLE_NAME, WorkflowRunLog.TABLE_NAME);

         table
            .withUniqueKey(new UniqueKey("workflowId", "versionNo"))
            .withIcon(new QIcon().withName("schema"))
            .withRecordLabelFormat("%s v%s")
            .withRecordLabelFields("workflowId", "versionNo")
            .withSection(SectionFactory.defaultT1("id", "workflowId", "versionNo"))
            .withSection(SectionFactory.customT2("workflowViewerWidget", new QIcon("account_tree")).withLabel("Workflow Steps").withWidgetName(WorkflowRevisionViewerWidget.NAME))
            .withSection(SectionFactory.defaultT2("apiName", "apiVersion").withName("api").withGridColumns(6))
            .withSection(SectionFactory.defaultT2("commitMessage", "author", "startStepNo").withGridColumns(6))
            .withSection(SectionFactory.customT2("runLogs", new QIcon("receipt_long")).withWidgetName(runLogsChildJoinName))
            .withSection(SectionFactory.customT2("steps", new QIcon("polyline")).withWidgetName(stepChildJoinName))
            .withSection(SectionFactory.customT2("links", new QIcon("link")).withWidgetName(linkChildJoinName))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_WORKFLOW_STEP).withAssociatedTableName(WorkflowStep.TABLE_NAME).withJoinName(stepChildJoinName))
            .withAssociation(new Association().withName(ASSOCIATION_NAME_WORKFLOW_LINK).withAssociatedTableName(WorkflowLink.TABLE_NAME).withJoinName(linkChildJoinName));

         return (table);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class RunLogChildListWidgetCustomizer implements MetaDataCustomizerInterface<QWidgetMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QWidgetMetaData customizeMetaData(QInstance qInstance, QWidgetMetaData widget) throws QException
      {
         widget.withDefaultValue("orderBy", new ArrayList<>(List.of(new QFilterOrderBy("id", false))));
         widget.withDefaultValue("omitFieldNames", new ArrayList<>(List.of("workflowId")));
         return widget;
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(possibleValueSourceName = Workflow.TABLE_NAME)
   private Integer workflowId;

   @QField()
   private Integer versionNo;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = "apiName")
   private String apiName;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = "apiVersion")
   private String apiVersion;

   @QField()
   private Integer startStepNo;

   @QField(maxLength = 500, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String commitMessage;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.TRUNCATE_ELLIPSIS)
   private String author;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QAssociation(name = ASSOCIATION_NAME_WORKFLOW_STEP)
   private List<WorkflowStep> steps;

   @QAssociation(name = ASSOCIATION_NAME_WORKFLOW_LINK)
   private List<WorkflowLink> links;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowRevision()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowRevision(QRecord record)
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
   public WorkflowRevision withId(Integer id)
   {
      this.id = id;
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
   public WorkflowRevision withCreateDate(Instant createDate)
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
   public WorkflowRevision withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for versionNo
    *******************************************************************************/
   public Integer getVersionNo()
   {
      return (this.versionNo);
   }



   /*******************************************************************************
    ** Setter for versionNo
    *******************************************************************************/
   public void setVersionNo(Integer versionNo)
   {
      this.versionNo = versionNo;
   }



   /*******************************************************************************
    ** Fluent setter for versionNo
    *******************************************************************************/
   public WorkflowRevision withVersionNo(Integer versionNo)
   {
      this.versionNo = versionNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for startStepNo
    *******************************************************************************/
   public Integer getStartStepNo()
   {
      return (this.startStepNo);
   }



   /*******************************************************************************
    ** Setter for startStepNo
    *******************************************************************************/
   public void setStartStepNo(Integer startStepNo)
   {
      this.startStepNo = startStepNo;
   }



   /*******************************************************************************
    ** Fluent setter for startStepNo
    *******************************************************************************/
   public WorkflowRevision withStartStepNo(Integer startStepNo)
   {
      this.startStepNo = startStepNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for commitMessage
    *******************************************************************************/
   public String getCommitMessage()
   {
      return (this.commitMessage);
   }



   /*******************************************************************************
    ** Setter for commitMessage
    *******************************************************************************/
   public void setCommitMessage(String commitMessage)
   {
      this.commitMessage = commitMessage;
   }



   /*******************************************************************************
    ** Fluent setter for commitMessage
    *******************************************************************************/
   public WorkflowRevision withCommitMessage(String commitMessage)
   {
      this.commitMessage = commitMessage;
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
   public WorkflowRevision withWorkflowId(Integer workflowId)
   {
      this.workflowId = workflowId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for steps
    *******************************************************************************/
   public List<WorkflowStep> getSteps()
   {
      return (this.steps);
   }



   /*******************************************************************************
    ** Setter for steps
    *******************************************************************************/
   public void setSteps(List<WorkflowStep> steps)
   {
      this.steps = steps;
   }



   /*******************************************************************************
    ** Fluent setter for steps
    *******************************************************************************/
   public WorkflowRevision withSteps(List<WorkflowStep> steps)
   {
      this.steps = steps;
      return (this);
   }



   /*******************************************************************************
    ** Getter for links
    *******************************************************************************/
   public List<WorkflowLink> getLinks()
   {
      return (this.links);
   }



   /*******************************************************************************
    ** Setter for links
    *******************************************************************************/
   public void setLinks(List<WorkflowLink> links)
   {
      this.links = links;
   }



   /*******************************************************************************
    ** Fluent setter for links
    *******************************************************************************/
   public WorkflowRevision withLinks(List<WorkflowLink> links)
   {
      this.links = links;
      return (this);
   }



   /*******************************************************************************
    ** Getter for author
    *******************************************************************************/
   public String getAuthor()
   {
      return (this.author);
   }



   /*******************************************************************************
    ** Setter for author
    *******************************************************************************/
   public void setAuthor(String author)
   {
      this.author = author;
   }



   /*******************************************************************************
    ** Fluent setter for author
    *******************************************************************************/
   public WorkflowRevision withAuthor(String author)
   {
      this.author = author;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiName
    *******************************************************************************/
   public String getApiName()
   {
      return (this.apiName);
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
   public WorkflowRevision withApiName(String apiName)
   {
      this.apiName = apiName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiVersion
    *******************************************************************************/
   public String getApiVersion()
   {
      return (this.apiVersion);
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
   public WorkflowRevision withApiVersion(String apiVersion)
   {
      this.apiVersion = apiVersion;
      return (this);
   }

}
