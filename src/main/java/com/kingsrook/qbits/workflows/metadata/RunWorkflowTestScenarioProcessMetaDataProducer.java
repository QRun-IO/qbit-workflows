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

package com.kingsrook.qbits.workflows.metadata;


import java.util.List;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.processes.RunWorkflowTestScenarioLoadStep;
import com.kingsrook.qbits.workflows.processes.RunWorkflowTestScenarioTransformStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducer;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 * Meta Data Producer for RunWorkflowTestScenarioProcess
 * - that is - for 1+ scenarios, run them.
 *******************************************************************************/
public class RunWorkflowTestScenarioProcessMetaDataProducer extends MetaDataProducer<QProcessMetaData>
{
   public static final String NAME = "RunWorkflowTestScenario";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(NAME)
         .withLabel("Run Test Scenarios")
         .withIcon(new QIcon().withName("play_arrow"))
         .withTableName(WorkflowTestScenario.TABLE_NAME)
         .withSourceTable(WorkflowTestScenario.TABLE_NAME)
         .withDestinationTable(WorkflowTestRun.TABLE_NAME)
         .withExtractStepClass(ExtractViaQueryStep.class)
         .withTransformStepClass(RunWorkflowTestScenarioTransformStep.class)
         .withLoadStepClass(RunWorkflowTestScenarioLoadStep.class)

         /////////////////////////////////////////////////////////////////////////////
         // this process requires full validation, because we need all rows to go   //
         // through the transform step, to see the full set of workflows that'll be //
         // processed - as we want to make 1 WorkflowTestRun per workflow.          //
         /////////////////////////////////////////////////////////////////////////////
         .withDoFullValidation(true)

         .withReviewStepRecordFields(List.of(
            new QFieldMetaData("id", QFieldType.INTEGER),
            new QFieldMetaData("name", QFieldType.STRING)
         ))
         .withPreviewMessage(StreamedETLWithFrontendProcess.DEFAULT_PREVIEW_MESSAGE_PREFIX + " tested")
         .withTransactionLevelPage()
         .getProcessMetaData();
   }

}
