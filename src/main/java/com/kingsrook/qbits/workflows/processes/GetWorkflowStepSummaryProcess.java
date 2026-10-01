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

package com.kingsrook.qbits.workflows.processes;


import java.io.Serializable;
import java.util.Map;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFunctionInputMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** process to get a dynamic summary of a workflow step, e.g., as it's being
 ** edited in a UI.
 *******************************************************************************/
@Deprecated(since = "0.115.0 - November 2025 - Replaced by GetWorkflowStepDynamicValuesProcess")
public class GetWorkflowStepSummaryProcess implements BackendStep, MetaDataProducerInterface<QProcessMetaData>
{
   public static final String NAME = "getWorkflowStepSummary";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QProcessMetaData produce(QInstance qInstance) throws QException
   {
      return new QProcessMetaData()
         .withName(NAME)
         .withPermissionRules(new QPermissionRules().withLevel(PermissionLevel.NOT_PROTECTED))
         .withStep(new QBackendStepMetaData()
            .withName("execute")
            .withCode(new QCodeReference(getClass()))
            .withInputData(new QFunctionInputMetaData()
               .withField(new QFieldMetaData("workflowId", QFieldType.INTEGER))
               .withField(new QFieldMetaData("workflowStepTypeName", QFieldType.STRING))
               .withField(new QFieldMetaData("values", QFieldType.INTEGER)) // JSON
            ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      Integer workflowId = runBackendStepInput.getValueInteger("workflowId");

      String                    workflowStepTypeName = runBackendStepInput.getValueString("workflowStepTypeName");
      String                    values     = runBackendStepInput.getValueString("values");
      Map<String, Serializable> valueAsMap = ValueUtils.getValueAsMap(values);

      WorkflowStepType workflowStepType = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowStepType(workflowStepTypeName);
      runBackendStepOutput.addValue("summary", workflowStepType.getDynamicStepSummary(workflowId, valueAsMap));
   }

}
