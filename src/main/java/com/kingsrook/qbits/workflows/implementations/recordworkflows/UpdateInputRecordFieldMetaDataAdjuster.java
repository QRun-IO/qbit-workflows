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

package com.kingsrook.qbits.workflows.implementations.recordworkflows;


import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qbits.workflows.implementations.WorkflowStepUtils;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qqq.backend.core.actions.values.SearchPossibleValueSourceAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceOutput;
import com.kingsrook.qqq.backend.core.model.metadata.code.InitializableViaCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReferenceWithProperties;
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
import org.json.JSONObject;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/***************************************************************************
 * material-dashboard field-meta-data-adjuster for form with `fieldName` and
 * `value` input fields, so that, when you pick a `fieldName`, then the `value`
 * field will change to work for that fieldName - e.g., its type, its
 * requiredness, its PVS, etc.
 ***************************************************************************/
public class UpdateInputRecordFieldMetaDataAdjuster implements FormAdjusterInterface, InitializableViaCodeReference
{
   private static final QLogger LOG = QLogger.getLogger(UpdateInputRecordFieldMetaDataAdjuster.class);

   public static final String TABLE_NAME_PROPERTY = "tableName";

   private String tableName;



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public void initialize(QCodeReference codeReference)
   {
      if(codeReference instanceof QCodeReferenceWithProperties codeReferenceWithProperties)
      {
         this.tableName = ValueUtils.getValueAsString(codeReferenceWithProperties.getProperties().get(TABLE_NAME_PROPERTY));
      }
   }



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

      //////////////////////////////////////////////
      // start building the updated 'value' field //
      //////////////////////////////////////////////
      QFieldMetaData updatedField = new QFieldMetaData("value", QFieldType.STRING)
         .withLabel("Value")
         .withIsEditable(false);

      String tableName = getTableNameForWorkflow(input);

      ////////////////////////////////////////////////////////////////////////////////////////////////
      // if there is a new value (e.g., a selection has been made for fieldName), look up the field //
      ////////////////////////////////////////////////////////////////////////////////////////////////
      if(StringUtils.hasContent(newValue))
      {
         try
         {
            WorkflowRevision workflowRevision = buildWorkflowRevisionFromInputJSONValues(input.getAllValues().get("workflowRevisionValuesJSON"));
            if(workflowRevision != null && WorkflowStepUtils.useApi(workflowRevision))
            {
               Optional<QFieldMetaData> foundField = UpdateInputRecordFieldStep.getApiField(newValue, tableName, workflowRevision);
               if(foundField.isPresent())
               {
                  updatedField = foundField.get().clone();
                  updatedField.setName("value");
                  updatedField.setLabel("Value (" + foundField.get().getLabel() + ")");
               }
            }
            else
            {
               QTableMetaData    table             = QContext.getQInstance().getTable(tableName);
               FieldAndJoinTable fieldAndJoinTable = FieldAndJoinTable.get(table, newValue);

               if(fieldAndJoinTable != null)
               {
                  updatedField = fieldAndJoinTable.field().clone();
                  updatedField.setName("value");
                  updatedField.setLabel("Value (" + fieldAndJoinTable.field().getLabel() + ")");
               }
            }
         }
         catch(Exception e)
         {
            LOG.info("Error getting field from table", e, logPair("fieldName", newValue));
         }
      }

      adjustPossibleValueSourceFilterCriteriaInputs(updatedField.getPossibleValueSourceFilter());

      ///////////////////////////////////////////////////////////////////////////////////
      // build the output object, starting with the new meta-data of the 'value' field //
      ///////////////////////////////////////////////////////////////////////////////////
      FormAdjusterOutput output = new FormAdjusterOutput();
      output.setUpdatedFieldMetaData(Map.of("value", new QFrontendFieldMetaData(updatedField
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // set attributes on the field that are needed regardless of whether we found the field or not //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         .withGridColumns(12)
      )));

      if(RunFormAdjusterProcess.EVENT_ON_CHANGE.equals(input.getEvent()))
      {
         /////////////////////////////////////////////////////////////////////
         // for an on-change event clear out the value in the 'value' field //
         /////////////////////////////////////////////////////////////////////
         output.setFieldsToClear(Set.of("value"));
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
               output.setUpdatedFieldDisplayValues(Map.of("value", searchPossibleValueSourceOutput.getResults().get(0).getLabel()));
            }
         }
      }

      return output;
   }



   /***************************************************************************
    * if the field has a possible-value-source filter on it, and that filter
    * uses an input field e.g., ${input.someField} - consider that this value
    * *might* be an attribute on the workflow so add an optional
    * ??${input.workflow.someField} after it.
    ***************************************************************************/
   static void adjustPossibleValueSourceFilterCriteriaInputs(QQueryFilter filter)
   {
      if(filter == null)
      {
         return;
      }

      for(QFilterCriteria criteria : CollectionUtils.nonNullList(filter.getCriteria()))
      {
         List<Serializable>         values        = CollectionUtils.nonNullList(criteria.getValues());
         ListIterator<Serializable> valueIterator = values.listIterator();
         while(valueIterator.hasNext())
         {
            Serializable value = valueIterator.next();
            if(value instanceof String stringValue && stringValue.startsWith("${input."))
            {
               String inputFieldName = stringValue.replace("${input.", "").replace("}", "");
               String updatedValue   = stringValue + "??${input.workflow." + inputFieldName + "}";
               valueIterator.set(updatedValue);
            }
         }
      }

      for(QQueryFilter subFilter : CollectionUtils.nonNullList(filter.getSubFilters()))
      {
         adjustPossibleValueSourceFilterCriteriaInputs(subFilter);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private WorkflowRevision buildWorkflowRevisionFromInputJSONValues(Serializable workflowRevisionValuesJSON)
   {
      try
      {
         JSONObject       jsonObject       = new JSONObject(ValueUtils.getValueAsString(workflowRevisionValuesJSON));
         WorkflowRevision workflowRevision = new WorkflowRevision();

         if(jsonObject.has("apiName"))
         {
            workflowRevision.setApiName(jsonObject.getString("apiName"));
         }

         if(jsonObject.has("apiVersion"))
         {
            workflowRevision.setApiVersion(jsonObject.getString("apiVersion"));
         }

         return (workflowRevision);
      }
      catch(Exception e)
      {
         LOG.warn("Error building workflow revision from inputValuesJSON", e);
      }
      return null;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private String getTableNameForWorkflow(FormAdjusterInput input) throws QException
   {
      if(this.tableName != null)
      {
         return (this.tableName);
      }

      String tableName          = null;
      String workflowValuesJSON = ValueUtils.getValueAsString(CollectionUtils.nonNullMap(input.getAllValues()).get("workflowValuesJSON"));
      if(StringUtils.hasContent(workflowValuesJSON))
      {
         JSONObject workflowValuesJSONObject = new JSONObject(workflowValuesJSON);
         if(workflowValuesJSONObject.has("tableName"))
         {
            tableName = workflowValuesJSONObject.getString("tableName");
         }
      }

      if(tableName == null)
      {
         throw (new QException("Could not get table name from input"));
      }
      return tableName;
   }
}
