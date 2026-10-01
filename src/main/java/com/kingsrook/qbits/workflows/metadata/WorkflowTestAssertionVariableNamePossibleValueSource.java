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


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.execution.WorkflowTypeTesterInterface;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** PVS for the variableName field on WorkflowTestAssertion
 *******************************************************************************/
public class WorkflowTestAssertionVariableNamePossibleValueSource implements QCustomPossibleValueProvider<String>, MetaDataProducerInterface<QPossibleValueSource>
{
   public static final String NAME = " WorkflowTestAssertionVariableName";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValueSource produce(QInstance qInstance) throws QException
   {
      return new QPossibleValueSource()
         .withName(NAME)
         .withIdType(QFieldType.STRING)
         .withCustomCodeReference(new QCodeReference(getClass()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValue<String> getPossibleValue(Serializable idValue)
   {
      if(idValue == null)
      {
         return (null);
      }

      String   idString = ValueUtils.getValueAsString(idValue);
      String[] idParts  = idString.split("\\.");
      if(idParts.length != 2)
      {
         return (null);
      }

      String         tableName = idParts[0];
      String         fieldName = idParts[1];
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);
      QFieldMetaData field     = table.getField(fieldName);
      return (new QPossibleValue<>(idString, field.getLabel()));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QPossibleValue<String>> search(SearchPossibleValueSourceInput input) throws QException
   {
      Integer workflowTestScenarioId = ValueUtils.getValueAsInteger(CollectionUtils.nonNullMap(input.getOtherValues()).get("workflowTestScenarioId"));
      if(workflowTestScenarioId == null)
      {
         return Collections.emptyList();
      }

      QRecord workflowTestScenario = GetAction.execute(WorkflowTestScenario.TABLE_NAME, workflowTestScenarioId);
      if(workflowTestScenario == null)
      {
         return Collections.emptyList();
      }

      QRecord workflow = GetAction.execute(Workflow.TABLE_NAME, workflowTestScenario.getValueInteger("workflowId"));
      if(workflow == null)
      {
         return Collections.emptyList();
      }

      String            workflowTypeName  = workflow.getValueString("workflowTypeName");
      WorkflowsRegistry workflowsRegistry = WorkflowsRegistry.of(QContext.getQInstance());
      WorkflowType      workflowType      = workflowsRegistry.getWorkflowType(workflowTypeName);
      if(workflowType == null)
      {
         return Collections.emptyList();
      }

      WorkflowTypeTesterInterface workflowTypeTester = QCodeLoader.getAdHoc(WorkflowTypeTesterInterface.class, workflowType.getTester());
      return (workflowTypeTester.searchTestAssertionVariableNamePossibleValues(workflow, input));
   }
}
