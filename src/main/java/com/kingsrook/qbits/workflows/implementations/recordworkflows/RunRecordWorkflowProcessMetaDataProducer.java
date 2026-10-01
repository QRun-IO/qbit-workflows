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

package com.kingsrook.qbits.workflows.implementations.recordworkflows;


import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.tracing.WorkflowTracerInterface;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducer;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Meta Data Producer for RunRecordWorkflow process - the generic process
 ** that can appear on all tables, to run workflows against records.
 *******************************************************************************/
public class RunRecordWorkflowProcessMetaDataProducer extends QBitComponentMetaDataProducer<QProcessMetaData, WorkflowsQBitConfig>
{
   public static final String NAME = "RunRecordWorkflow";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      QProcessMetaData processMetaData = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(NAME)
         .withLabel("Run Workflow")
         .withIcon(new QIcon().withName("account_tree"))
         .withSupportsFullValidation(false)
         .withExtractStepClass(RunRecordWorkflowExtractStep.class)
         .withTransformStepClass(RunRecordWorkflowTransformStep.class)
         .withLoadStepClass(RunRecordWorkflowLoadStep.class)
         .getProcessMetaData();

      processMetaData.withStep(0, new QFrontendStepMetaData()
         .withName("input")
         .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))
         .withFormField(new QFieldMetaData("workflowId", QFieldType.INTEGER).withPossibleValueSourceName(Workflow.TABLE_NAME)
            .withPossibleValueSourceFilter(new QQueryFilter(
               new QFilterCriteria("workflowTypeName", QCriteriaOperator.EQUALS, "RecordWorkflow"),
               new QFilterCriteria("tableName", QCriteriaOperator.EQUALS, "${input.tableName}")
            ))));

      QBackendStepMetaData executeStep = processMetaData.getBackendStep(StreamedETLWithFrontendProcess.STEP_NAME_EXECUTE);
      executeStep.getInputMetaData()
         .withField(new QFieldMetaData("workflowTracerCodeReference", QFieldType.STRING)
            .withDefaultValue(getQBitConfig().getWorkflowTracerCodeReference()))
         .withField(new QFieldMetaData("workflowTracerCodeReference_expectedType", QFieldType.STRING)
            .withDefaultValue(WorkflowTracerInterface.class.getName()));

      return (processMetaData);
   }

}
