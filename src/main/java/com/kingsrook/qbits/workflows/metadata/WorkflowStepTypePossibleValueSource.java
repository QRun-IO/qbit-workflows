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
import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** PVS for workflow step types - custom PVS implementation, that uses Workflow
 ** Registry as backend.
 *******************************************************************************/
public class WorkflowStepTypePossibleValueSource implements QCustomPossibleValueProvider<String>, MetaDataProducerInterface<QPossibleValueSource>
{
   public static final String NAME = "WorkflowStepType";



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
   public QPossibleValue<String> getPossibleValue(Serializable id)
   {
      WorkflowStepType workflowStepType = WorkflowsRegistry.of(QContext.getQInstance()).getWorkflowStepType(ValueUtils.getValueAsString(id));
      if(workflowStepType == null)
      {
         return (null);
      }

      return getPossibleValue(workflowStepType);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static QPossibleValue<String> getPossibleValue(WorkflowStepType workflowStepType)
   {
      return new QPossibleValue<>(workflowStepType.getName(), workflowStepType.getLabel());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<QPossibleValue<String>> search(SearchPossibleValueSourceInput input) throws QException
   {
      List<QPossibleValue<String>> allPossibleValues = new ArrayList<>();
      for(WorkflowStepType workflowStepType : WorkflowsRegistry.of(QContext.getQInstance()).getAllWorkflowStepTypes())
      {
         allPossibleValues.add(getPossibleValue(workflowStepType));
      }

      return completeCustomPVSSearch(input, allPossibleValues);
   }
}
