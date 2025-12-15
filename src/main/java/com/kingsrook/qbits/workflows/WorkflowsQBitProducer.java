/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qbits.workflows;


import java.util.List;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowsDefinition;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.model.WorkflowRunLogStep;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qbits.workflows.model.WorkflowTestOutput;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestRunScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.processes.StoreNewWorkflowRevisionProcess;
import com.kingsrook.qbits.workflows.triggers.TableTriggerCustomizerForWorkflows;
import com.kingsrook.qbits.workflows.triggers.WorkflowCustomTableTriggerRecordAutomationHandler;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.automation.TableTrigger;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;


/*******************************************************************************
 **
 *******************************************************************************/
public class WorkflowsQBitProducer implements QBitMetaDataProducer<WorkflowsQBitConfig>
{
   private WorkflowsQBitConfig workflowsQBitConfig;



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QBitMetaData getQBitMetaData()
   {
      QBitMetaData qBitMetaData = new QBitMetaData()
         .withGroupId("com.kingsrook.qbits")
         .withArtifactId("workflows")
         .withVersion("0.1.11")
         .withNamespace(getNamespace())
         .withConfig(getQBitConfig());

      return qBitMetaData;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void postProduceActions(MetaDataProducerMultiOutput metaDataProducerMultiOutput, QInstance qInstance) throws QException
   {
      ////////////////////////////////////////////////////////////////////////////////////////
      // add the registry to the QInstance now - it'll also be added when the multi-output  //
      // is eventually added to the instance, but we need it to be in the instance earlier, //
      // the RecordWorkflowsDefinition().register() call can access it.                     //
      ////////////////////////////////////////////////////////////////////////////////////////
      WorkflowsRegistry workflowsRegistry = metaDataProducerMultiOutput.get(WorkflowsRegistry.class, WorkflowsRegistry.NAME);
      qInstance.add(workflowsRegistry);

      if(workflowsQBitConfig.getIncludeRecordWorkflows())
      {
         new RecordWorkflowsDefinition().register(qInstance);
      }

      if(!workflowsQBitConfig.getIncludeApiVersions())
      {
         QTableMetaData workflowRevisionTable = metaDataProducerMultiOutput.get(QTableMetaData.class, WorkflowRevision.TABLE_NAME);
         workflowRevisionTable.getFields().remove("apiVersion");
         workflowRevisionTable.getFields().remove("apiName");
         workflowRevisionTable.getSections().removeIf(s -> "api".equals(s.getName()));
         workflowRevisionTable.getSections().stream()
            .filter(s -> SectionFactory.getDefaultT2name().equals(s.getName()))
            .forEach(s -> s.setGridColumns(12));

         QProcessMetaData storeRevisionProcess = metaDataProducerMultiOutput.get(QProcessMetaData.class, StoreNewWorkflowRevisionProcess.NAME);
         List<QFieldMetaData> storeProcessFieldList = storeRevisionProcess.getBackendStep("execute")
            .getInputMetaData()
            .getFieldList();
         storeProcessFieldList.removeIf(f -> f.getName().equals("apiVersion") || f.getName().equals("apiName"));
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static QAppSection getAppSection(QInstance qInstance)
   {
      return (new QAppSection().withName("workflows")
         .withTable(Workflow.TABLE_NAME)
         .withTable(WorkflowRevision.TABLE_NAME)
         .withTable(WorkflowStep.TABLE_NAME)
         .withTable(WorkflowLink.TABLE_NAME)
         .withTable(WorkflowRunLog.TABLE_NAME)
         .withTable(WorkflowRunLogStep.TABLE_NAME)

         .withTable(WorkflowTestScenario.TABLE_NAME)
         .withTable(WorkflowTestAssertion.TABLE_NAME)
         .withTable(WorkflowTestRun.TABLE_NAME)
         .withTable(WorkflowTestRunScenario.TABLE_NAME)
         .withTable(WorkflowTestOutput.TABLE_NAME)

      );
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void addWorkflowsToTableTriggers(QInstance qInstance) throws QException
   {
      QTableMetaData tableTriggerTable = qInstance.getTable(TableTrigger.TABLE_NAME);
      if(tableTriggerTable == null)
      {
         LOG.warn("Attempted to add workflows to the TableTriggers table, but that table is not defined in the QInstance (at least not at this point in time.");
         return;
      }

      ////////////////////////////////////////////////////////////////////////////////////
      // have the workflows custom-table-trigger handler register itself with the class //
      // that'll call it to run when an automation tries to run a workflow trigger      //
      ////////////////////////////////////////////////////////////////////////////////////
      WorkflowCustomTableTriggerRecordAutomationHandler.register();

      ////////////////////////////////////////////////
      // add workflowId field to tableTrigger table //
      ////////////////////////////////////////////////
      tableTriggerTable.withField(new QFieldMetaData("workflowId", QFieldType.INTEGER)
         .withBackendName("workflow_id")
         .withPossibleValueSourceName(Workflow.TABLE_NAME)
         .withPossibleValueSourceFilter(new QQueryFilter()
            .withCriteria(new QFilterCriteria("workflowTypeName", QCriteriaOperator.EQUALS, RecordWorkflowsDefinition.WORKFLOW_TYPE))
            .withCriteria(new QFilterCriteria("tableName", QCriteriaOperator.EQUALS, "${input.tableName}"))));

      //////////////////////////////////////////////////////////////////////////////
      // place workflowId field after scriptId in whatever section scriptId is in //
      //////////////////////////////////////////////////////////////////////////////
      for(QFieldSection section : tableTriggerTable.getSections())
      {
         int scriptIdIndex = section.getFieldNames().indexOf("scriptId");
         if(scriptIdIndex > -1)
         {
            section.getFieldNames().add(scriptIdIndex + 1, "workflowId");
         }
      }

      //////////////////////////////////////////////////////////////////////////
      // if you have workflows, then you don't necessarily need a script - so //
      // change this field to not be required                                 //
      //////////////////////////////////////////////////////////////////////////
      tableTriggerTable.getField("scriptId").setIsRequired(false);

      ////////////////////////////////////////////////////////////////////////////////////////////
      // but - do add a pre insert/updated validator, to make sure one or the other is selected //
      ////////////////////////////////////////////////////////////////////////////////////////////
      tableTriggerTable.withCustomizer(TableCustomizers.PRE_INSERT_RECORD, new QCodeReference(TableTriggerCustomizerForWorkflows.class));
      tableTriggerTable.withCustomizer(TableCustomizers.PRE_UPDATE_RECORD, new QCodeReference(TableTriggerCustomizerForWorkflows.class));
   }



   /*******************************************************************************
    ** Getter for qBitConfig
    *******************************************************************************/
   @Override
   public WorkflowsQBitConfig getQBitConfig()
   {
      return (this.workflowsQBitConfig);
   }



   /*******************************************************************************
    ** Setter for qBitConfig
    *******************************************************************************/
   public void setQBitConfig(WorkflowsQBitConfig workflowsQBitConfig)
   {
      this.workflowsQBitConfig = workflowsQBitConfig;
   }



   /*******************************************************************************
    ** Fluent setter for qBitConfig
    *******************************************************************************/
   public WorkflowsQBitProducer withQBitConfig(WorkflowsQBitConfig workflowsQBitConfig)
   {
      this.workflowsQBitConfig = workflowsQBitConfig;
      return (this);
   }

}
