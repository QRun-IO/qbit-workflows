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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAndJoinTable;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInterface;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterOutput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.RunFormAdjusterProcess;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/***************************************************************************
 * material-dashboard field-meta-data-adjuster for form with `variableName` and
 * `expectedValue` fields, so that, when you pick a `variableName`, then the
 * `expectedValue` field will change to work for that variableName - e.g., its
 * type, its requiredness, its PVS, etc.
 ***************************************************************************/
public class WorkflowTestAssertionVariableNameFieldMetaDataAdjuster implements FormAdjusterInterface
{
   private static final QLogger LOG = QLogger.getLogger(WorkflowTestAssertionVariableNameFieldMetaDataAdjuster.class);



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public FormAdjusterOutput execute(FormAdjusterInput input) throws QException
   {
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // get the new value for the 'fieldName' field - which will tell us what meta-data we need to set for the 'value' field //
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      String newValue = ValueUtils.getValueAsString(input.getNewValue());

      //////////////////////////////////////////////////////
      // start building the updated 'expectedValue' field //
      //////////////////////////////////////////////////////
      QFieldMetaData updatedField = new QFieldMetaData("expectedValue", QFieldType.STRING)
         .withLabel("Expected Value")
         .withIsEditable(false);

      String tableName = getTableNameForWorkflow(input);

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // if there is a new value (e.g., a selection has been made for fieldName), look up the field //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      if(StringUtils.hasContent(newValue))
      {
         try
         {
            /////////////////////////////////////////////////////////////////////////////////////////////////////
            // todo - should this be using api fields?  if so, what, based on the workflow's current revision? //
            /////////////////////////////////////////////////////////////////////////////////////////////////////
            /*
            WorkflowRevision workflowRevision = ... todo
            if(workflowRevision != null && WorkflowStepUtils.useApi(workflowRevision))
            {
               Optional<QFieldMetaData> foundField = UpdateInputRecordFieldStep.getApiField(newValue, tableName, workflowRevision);
               if(foundField.isPresent())
               {
                  updatedField = foundField.get().clone();
                  updatedField.setName("expectedValue");
                  updatedField.setLabel("Expected Value (" + foundField.get().getLabel() + ")");
                  updatedField.setIsEditable(true);
               }
            }
            else
            */

            {
               QTableMetaData    table             = QContext.getQInstance().getTable(tableName);
               FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, newValue);

               if(fieldAndJoinTable != null)
               {
                  updatedField = fieldAndJoinTable.field().clone();
                  updatedField.setName("expectedValue");
                  updatedField.setLabel("Expected Value (" + fieldAndJoinTable.field().getLabel() + ")");
                  updatedField.setIsEditable(true);
               }
            }
         }
         catch(Exception e)
         {
            LOG.info("Error getting field from table", e, logPair("fieldName", newValue));
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////
      // build the output object, starting with the new meta-data of the 'value' field //
      ///////////////////////////////////////////////////////////////////////////////////
      FormAdjusterOutput output = new FormAdjusterOutput();
      output.setUpdatedFieldMetaData(Map.of("expectedValue", new QFrontendFieldMetaData(updatedField)));

      if(RunFormAdjusterProcess.EVENT_ON_CHANGE.equals(input.getEvent()))
      {
         /////////////////////////////////////////////////////////////////////
         // for an on-change event clear out the value in the 'value' field //
         /////////////////////////////////////////////////////////////////////
         output.setFieldsToClear(Set.of("expectedValue"));
      }
      else if(RunFormAdjusterProcess.EVENT_ON_LOAD.equals(input.getEvent()))
      {
         /////////////////////////////////////////////////////////////////////////
         // for an on-load event, for PVS fields, look up the value for display //
         /////////////////////////////////////////////////////////////////////////
         Object oldValueObject = Objects.requireNonNullElse(input.getAllValues(), Collections.emptyMap()).get("value");
         if(StringUtils.hasContent(updatedField.getPossibleValueSourceName()) && oldValueObject instanceof Serializable oldValue)
         {
            SearchPossibleValueSourceOutput searchPossibleValueSourceOutput = new SearchPossibleValueSourceAction().execute(new SearchPossibleValueSourceInput()
               .withIdList(List.of(oldValue))
               .withPossibleValueSourceName(updatedField.getPossibleValueSourceName())
            );

            if(CollectionUtils.nullSafeHasContents(searchPossibleValueSourceOutput.getResults()))
            {
               output.setUpdatedFieldDisplayValues(Map.of("expectedValue", searchPossibleValueSourceOutput.getResults().get(0).getLabel()));
            }
         }
      }

      return output;
   }


   /***************************************************************************
    **
    ***************************************************************************/
   private String getTableNameForWorkflow(FormAdjusterInput input) throws QException
   {
      Integer workflowTestScenarioId = ValueUtils.getValueAsInteger(CollectionUtils.nonNullMap(input.getAllValues()).get("workflowTestScenarioId"));
      if(workflowTestScenarioId == null)
      {
         return null;
      }

      QRecord workflowTestScenario = GetAction.execute(WorkflowTestScenario.TABLE_NAME, workflowTestScenarioId);
      if(workflowTestScenario == null)
      {
         return null;
      }

      QRecord workflow = GetAction.execute(Workflow.TABLE_NAME, workflowTestScenario.getValueInteger("workflowId"));
      if(workflow == null)
      {
         return null;
      }

      return workflow.getValueString("tableName");
   }
}
