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

package com.kingsrook.qbits.workflows.execution;


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertionType;
import com.kingsrook.qbits.workflows.model.WorkflowTestOutput;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.api.actions.QRecordApiAdapter;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.utils.BackendQueryFilterUtils;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.ObjectUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;


/*******************************************************************************
 ** interface for the code that tests a workflow type
 *******************************************************************************/
public interface WorkflowTypeTesterInterface
{
   QLogger LOG = QLogger.getLogger(WorkflowTypeTesterInterface.class);

   /***************************************************************************
    *
    ***************************************************************************/
   default WorkflowInput setupWorkflowInputForTestScenario(QRecord workflow, QRecord scenario) throws QException
   {
      WorkflowInput workflowInput = new WorkflowInput();
      workflowInput.setWorkflowId(workflow.getValueInteger("id"));
      workflowInput.setWorkflowExecutionContext(new WorkflowExecutionContext());
      workflowInput.setValues(new LinkedHashMap<>());

      return (workflowInput);
   }


   /***************************************************************************
    * would be protected, but interface...
    * Expected to be called within setupWorkflowInputForTestScenario
    ***************************************************************************/
   default QRecord makeRecordFromTestScenario(QRecord scenario, String tableName) throws QException
   {
      Integer sourceRecordId = scenario.getValueInteger("sourceRecordId");
      String  apiJson        = scenario.getValueString("apiJson");
      String  apiName        = scenario.getValueString("apiName");
      String  apiVersion     = scenario.getValueString("apiVersion");

      QRecord inputRecord = null;
      if(sourceRecordId != null)
      {
         inputRecord = new GetAction().executeForRecord(new GetInput(tableName).withPrimaryKey(sourceRecordId).withIncludeAssociations(true));
         if(inputRecord == null)
         {
            String tableLabel = ObjectUtils.tryElse(() -> QContext.getQInstance().getTable(tableName).getLabel(), tableName);
            throw (new QException("Could not find " + tableLabel + " with primary key " + sourceRecordId));
         }
      }
      else if(StringUtils.hasContent(apiJson) && StringUtils.hasContent(apiName) && StringUtils.hasContent(apiVersion))
      {
         try
         {
            inputRecord = QRecordApiAdapter.apiJsonObjectToQRecord(new JSONObject(apiJson), tableName, apiName, apiVersion, true);

            //////////////////////////////////////////////////////////////////////////////////
            // maybe if we find that a null primary key causes issues, set it to some value //
            //////////////////////////////////////////////////////////////////////////////////
            // String primaryKeyField = QContext.getQInstance().getTable(tableName).getPrimaryKeyField();
            // if(inputRecord.getValue(primaryKeyField) == null)
            // {
            //    inputRecord.setValue(primaryKeyField, 0);
            // }
         }
         catch(Exception e)
         {
            throw (new QException("Error building input record from API JSON: " + e.getMessage()));
         }
      }
      else
      {
         throw (new QException("Test scenario did not have sufficient fields set for creating an input record"));
      }
      return inputRecord;
   }


   /***************************************************************************
    * would be protected, but interface...
    ***************************************************************************/
   default WorkflowTestOutput evaluateTestAssertion(QRecord assertion, WorkflowOutput workflowOutput)
   {
      WorkflowTestOutput        workflowTestOutput = new WorkflowTestOutput();
      WorkflowTestAssertionType assertionType      = WorkflowTestAssertionType.getByIdOrDefault(assertion.getValueInteger("assertionType"));

      try
      {
         String variableName    = assertion.getValueString("variableName");
         String expectedValue   = assertion.getValueString("expectedValue");
         String queryFilterJson = assertion.getValueString("queryFilterJson");

         if(StringUtils.hasContent(variableName))
         {
            Serializable actualValueSerializable = getValueForTestAssertion(workflowOutput, assertion);
            evaluateExpectedValue(workflowTestOutput, variableName, actualValueSerializable, expectedValue, assertionType);
         }
         else if(StringUtils.hasContent(queryFilterJson))
         {
            evaluateFilter(workflowTestOutput, workflowOutput, assertion, queryFilterJson, assertionType);
         }
         else
         {
            LOG.warn("Assertion not properly configured - considering this a fail.");
            workflowTestOutput.setMessage("Assertion was not property configured (missing variable and filter)");
            workflowTestOutput.setStatus(WorkflowTestStatus.FAIL.getId());
         }
      }
      catch(Exception e)
      {
         workflowTestOutput.setStatus(WorkflowTestStatus.ERROR.getId());
         workflowTestOutput.setMessage("Error: " + e.getMessage());
      }

      return (workflowTestOutput);
   }

   /***************************************************************************
    *
    ***************************************************************************/
   default void evaluateFilter(WorkflowTestOutput workflowTestOutput, WorkflowOutput workflowOutput, QRecord assertion, String queryFilterJson, WorkflowTestAssertionType assertionType) throws Exception
   {
      boolean doesMatch = doesFilterMatch(workflowTestOutput, workflowOutput, assertion, queryFilterJson);
      workflowTestOutput.setActualValue(String.valueOf(doesMatch));

      if(doesMatch)
      {
         switch(assertionType)
         {
            case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, "Filter matched.");
            case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, "Filter matched, but was expected not to.");
         }
      }
      else
      {
         switch(assertionType)
         {
            case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, "Filter did not match, but was expected to.");
            case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, "As expected, filter did not match.");
         }
      }
   }


   /***************************************************************************
    *
    ***************************************************************************/
   default boolean doesFilterMatch(WorkflowTestOutput workflowTestOutput, WorkflowOutput workflowOutput, QRecord assertion, String queryFilterJson) throws QException, IOException
   {
      QRecord record = getRecordForTestAssertionFilter(workflowOutput, assertion);
      if(record == null)
      {
         setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.ERROR, "Could not find record to apply filter assertion");
      }

      QQueryFilter filter    = JsonUtils.toObject(queryFilterJson, QQueryFilter.class);
      boolean      doesMatch = BackendQueryFilterUtils.doesRecordMatch(filter, record);
      return doesMatch;
   }


   /***************************************************************************
    *
    ***************************************************************************/
   default void evaluateExpectedValue(WorkflowTestOutput workflowTestOutput, String variableName, Serializable actualValueSerializable, String expectedValue, WorkflowTestAssertionType assertionType)
   {
      if(actualValueSerializable instanceof List<?> list)
      {
         workflowTestOutput.setActualValue("[" + StringUtils.join(",", list) + "]");
         boolean foundMatch = false;
         for(Object actual : list)
         {
            String actualString = ValueUtils.getValueAsString(actual);
            if(Objects.equals(expectedValue, actualString) || areBothBlank(expectedValue, actualString))
            {
               foundMatch = true;
               break;
            }
         }

         if(foundMatch)
         {
            switch(assertionType)
            {
               case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, variableName + ": found [" + expectedValue + "] as expected.");
               case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, variableName + ": found [" + expectedValue + "] but was not expected to.");
            }
         }
         else
         {
            switch(assertionType)
            {
               case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, variableName + ": did not find expected value [" + expectedValue + "].");
               case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, variableName + ": as expected, did not find [" + expectedValue + "].");
            }
         }
      }
      else
      {
         String actualValue = ValueUtils.getValueAsString(actualValueSerializable);
         workflowTestOutput.setActualValue(actualValue);

         if(areBothBlank(expectedValue, actualValue))
         {
            switch(assertionType)
            {
               case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, variableName + ": was blank as expected.");
               case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, variableName + ": was blank but was not expected to be.");
            }
         }
         else if(Objects.equals(expectedValue, actualValue))
         {
            switch(assertionType)
            {
               case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, variableName + ": was [" + expectedValue + "] as expected.");
               case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, variableName + ": was [" + expectedValue + "] but was not expected to be.");
            }
         }
         else
         {
            switch(assertionType)
            {
               case POSITIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.FAIL, variableName + ": was [" + actualValue + "] but expected [" + expectedValue + "].");
               case NEGATIVE -> setStatusAndMessage(workflowTestOutput, WorkflowTestStatus.PASS, variableName + ": was [" + actualValue + "] not [" + expectedValue + "] as expected.");
            }
         }
      }
   }


   /***************************************************************************
    *
    ***************************************************************************/
   default void setStatusAndMessage(WorkflowTestOutput workflowTestOutput, WorkflowTestStatus status, String message)
   {
      workflowTestOutput.setStatus(status.getId());
      workflowTestOutput.setMessage(message);
   }

   /***************************************************************************
    *
    ***************************************************************************/
   private static boolean areBothBlank(String expectedValue, String actualValue)
   {
      return (!StringUtils.hasContent(expectedValue) && !StringUtils.hasContent(actualValue));
   }


   /***************************************************************************
    *
    ***************************************************************************/
   default Serializable getValueForTestAssertion(WorkflowOutput workflowOutput, QRecord assertion) throws QException
   {
      String variableName = assertion.getValueString("variableName");
      String actualValue  = ValueUtils.getValueAsString(workflowOutput.getContext().getValues().get(variableName));
      return (actualValue);
   }

   /***************************************************************************
    *
    ***************************************************************************/
   default QRecord getRecordForTestAssertionFilter(WorkflowOutput workflowOutput, QRecord assertion) throws QException
   {
      QRecord record = (QRecord) workflowOutput.getContext().getValues().get("record");
      return (record);
   }


   /***************************************************************************
    *
    ***************************************************************************/
   default String buildTestRunScenarioOutputData(QRecord workflowRecord, WorkflowOutput workflowOutput) throws QException
   {
      return JsonUtils.toJson(workflowOutput.getContext().getValues());
   }


   /***************************************************************************
    *
    ***************************************************************************/
   default List<QPossibleValue<String>> searchTestAssertionVariableNamePossibleValues(QRecord workflow, SearchPossibleValueSourceInput input)
   {
      String         tableName = workflow.getValueString("tableName");
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);
      if(table == null)
      {
         return Collections.emptyList();
      }

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // todo - should this be a listing of api fields?  if so, what, based on the workflow's current revision? //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////

      List<QPossibleValue<String>> rs = new ArrayList<>();
      Comparator<QFieldMetaData> fieldMetaDataComparator = Comparator.comparing(QFieldMetaData::getLabel).thenComparing(QFieldMetaData::getName);

      for(QFieldMetaData field : table.getFields().values().stream().sorted(fieldMetaDataComparator).toList())
      {
         rs.add(new QPossibleValue<>(tableName + "." + field.getName(), field.getLabel()));
      }

      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         QTableMetaData associatedTable = QContext.getQInstance().getTable(association.getAssociatedTableName());
         for(QFieldMetaData field : associatedTable.getFields().values().stream().sorted(fieldMetaDataComparator).toList())
         {
            rs.add(new QPossibleValue<>(association.getAssociatedTableName() + "." + field.getName(), associatedTable.getLabel() + ": " + field.getLabel()));
         }
      }

      return (QCustomPossibleValueProvider.filterPossibleValuesForSearch(input, rs));
   }

}
