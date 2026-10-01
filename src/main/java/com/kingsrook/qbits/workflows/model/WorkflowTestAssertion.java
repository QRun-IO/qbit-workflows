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
import java.util.Set;
import com.kingsrook.qbits.workflows.metadata.WorkflowTestAssertionFilterWidget;
import com.kingsrook.qbits.workflows.metadata.WorkflowTestAssertionScenarioIdFieldMetaDataAdjuster;
import com.kingsrook.qbits.workflows.metadata.WorkflowTestAssertionVariableNameFieldMetaDataAdjuster;
import com.kingsrook.qbits.workflows.metadata.WorkflowTestAssertionVariableNamePossibleValueSource;
import com.kingsrook.qbits.workflows.tables.WorkflowTestAssertionTableCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FilterJsonFieldDisplayValueFormatter;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardFieldMetaData;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.MaterialDashboardTableMetaData;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.fieldrules.FieldRule;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.fieldrules.FieldRuleAction;
import com.kingsrook.qqq.frontend.materialdashboard.model.metadata.fieldrules.FieldRuleTrigger;


/*******************************************************************************
 ** QRecord Entity for WorkflowTestAssertion table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowTestAssertion.TableMetaDataCustomizer.class
)
public class WorkflowTestAssertion extends QRecordEntity implements Serializable
{
   public static final String TABLE_NAME = "workflowTestAssertion";



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
            .withIcon(new QIcon().withName("checklist"))
            .withRecordLabelFormat("%s")
            .withRecordLabelFields("name")
            .withUniqueKey(new UniqueKey("workflowTestScenarioId", "name"))
            .withSection(SectionFactory.defaultT1("id", "workflowTestScenarioId", "name"))
            .withSection(SectionFactory.defaultT2("assertionType").withName("behavior").withIcon(new QIcon().withName("gavel")))
            .withSection(SectionFactory.defaultT2("variableName", "expectedValue").withName("assertion").withIcon(new QIcon().withName("checklist")))
            .withSection(SectionFactory.defaultT2().withName("filter").withWidgetName(WorkflowTestAssertionFilterWidget.NAME).withIcon(new QIcon("filter_alt")))
            .withSection(SectionFactory.defaultT2("queryFilterJson").withName("hidden").withIsHidden(true))
            .withSection(SectionFactory.defaultT3("createDate", "modifyDate"));

         WorkflowTestAssertionType.customizeFieldWitChipAndWidth(table.getField("assertionType"));

         table.withCustomizer(TableCustomizers.PRE_INSERT_RECORD, new QCodeReference(WorkflowTestAssertionTableCustomizer.class));
         table.withCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(WorkflowTestAssertionTableCustomizer.class));

         ///////////////////////////////////////////////////////////////////////////////
         // when user changes the test scenario, we need to re-load the filter widget //
         // as the scenario drives what table the filter could apply to               //
         ///////////////////////////////////////////////////////////////////////////////
         MaterialDashboardTableMetaData materialDashboardTableMetaData = MaterialDashboardTableMetaData.ofOrWithNew(table);
         materialDashboardTableMetaData.withFieldRule(new FieldRule()
            .withSourceField("workflowTestScenarioId")
            .withTrigger(FieldRuleTrigger.ON_CHANGE)
            .withAction(FieldRuleAction.RELOAD_WIDGET)
            .withTargetWidget(WorkflowTestAssertionFilterWidget.NAME));

         /////////////////////////////////////////////////////////////////////////////////////////
         // also if scenario changes, need to clear out variable name and expected value fields //
         /////////////////////////////////////////////////////////////////////////////////////////
         QCodeReference scenarioIdFormAdjuster = new QCodeReference(WorkflowTestAssertionScenarioIdFieldMetaDataAdjuster.class);
         table.getField("workflowTestScenarioId")
            .withSupplementalMetaData(new MaterialDashboardFieldMetaData()
               .withFormAdjusterIdentifier("WorkflowTestAssertion.workflowTestScenarioId")
               .withFieldsToDisableWhileRunningAdjusters(Set.of("variableName", "expectedValue"))
               .withOnChangeFormAdjuster(scenarioIdFormAdjuster));

         ////////////////////////////////////////////////////////////////////
         // when variable name changes, need to reset expected value field //
         ////////////////////////////////////////////////////////////////////
         QCodeReference variableNameFormAdjuster = new QCodeReference(WorkflowTestAssertionVariableNameFieldMetaDataAdjuster.class);
         table.getField("variableName")
            .withSupplementalMetaData(new MaterialDashboardFieldMetaData()
               .withFormAdjusterIdentifier("WorkflowTestAssertion.variableName")
               .withFieldsToDisableWhileRunningAdjusters(Set.of("expectedValue"))
               .withOnChangeFormAdjuster(variableNameFormAdjuster)
               .withOnLoadFormAdjuster(variableNameFormAdjuster));

         table.getField("queryFilterJson").withBehavior(new FilterJsonFieldDisplayValueFormatter());

         return (table);
      }
   }



   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;

   @QField(possibleValueSourceName = WorkflowTestScenario.TABLE_NAME, isRequired = true)
   private Integer workflowTestScenarioId;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, isRequired = true)
   private String name;

   @QField(isRequired = true, defaultValue = WorkflowTestAssertionType.DEFAULT_VALUE_STRING, possibleValueSourceName = WorkflowTestAssertionType.NAME)
   private Integer assertionType;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR, possibleValueSourceName = WorkflowTestAssertionVariableNamePossibleValueSource.NAME, label = "Field")
   private String variableName;

   @QField(maxLength = 100, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String expectedValue;

   @QField(label = "Filter")
   private String queryFilterJson;



   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowTestAssertion()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowTestAssertion(QRecord record)
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
   public WorkflowTestAssertion withId(Integer id)
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
   public WorkflowTestAssertion withCreateDate(Instant createDate)
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
   public WorkflowTestAssertion withModifyDate(Instant modifyDate)
   {
      setModifyDate(modifyDate);
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
   public WorkflowTestAssertion withWorkflowTestScenarioId(Integer workflowTestScenarioId)
   {
      setWorkflowTestScenarioId(workflowTestScenarioId);
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
   public WorkflowTestAssertion withName(String name)
   {
      setName(name);
      return (this);
   }



   /*******************************************************************************
    ** Getter for variableName
    *******************************************************************************/
   public String getVariableName()
   {
      return (variableName);
   }



   /*******************************************************************************
    ** Setter for variableName
    *******************************************************************************/
   public void setVariableName(String variableName)
   {
      this.variableName = variableName;
   }



   /*******************************************************************************
    ** Fluent setter for variableName
    *******************************************************************************/
   public WorkflowTestAssertion withVariableName(String variableName)
   {
      setVariableName(variableName);
      return (this);
   }



   /*******************************************************************************
    ** Getter for expectedValue
    *******************************************************************************/
   public String getExpectedValue()
   {
      return (expectedValue);
   }



   /*******************************************************************************
    ** Setter for expectedValue
    *******************************************************************************/
   public void setExpectedValue(String expectedValue)
   {
      this.expectedValue = expectedValue;
   }



   /*******************************************************************************
    ** Fluent setter for expectedValue
    *******************************************************************************/
   public WorkflowTestAssertion withExpectedValue(String expectedValue)
   {
      setExpectedValue(expectedValue);
      return (this);
   }



   /*******************************************************************************
    ** Getter for queryFilterJson
    *******************************************************************************/
   public String getQueryFilterJson()
   {
      return (queryFilterJson);
   }



   /*******************************************************************************
    ** Setter for queryFilterJson
    *******************************************************************************/
   public void setQueryFilterJson(String queryFilterJson)
   {
      this.queryFilterJson = queryFilterJson;
   }



   /*******************************************************************************
    ** Fluent setter for queryFilterJson
    *******************************************************************************/
   public WorkflowTestAssertion withQueryFilterJson(String queryFilterJson)
   {
      setQueryFilterJson(queryFilterJson);
      return (this);
   }



   /*******************************************************************************
    * Getter for assertionType
    *******************************************************************************/
   public Integer getAssertionType()
   {
      return (this.assertionType);
   }



   /*******************************************************************************
    * Setter for assertionType
    *******************************************************************************/
   public void setAssertionType(Integer assertionType)
   {
      this.assertionType = assertionType;
   }



   /*******************************************************************************
    * Fluent setter for assertionType
    *******************************************************************************/
   public WorkflowTestAssertion withAssertionType(Integer assertionType)
   {
      this.assertionType = assertionType;
      return (this);
   }

}