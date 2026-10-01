
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


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsOutput;
import com.kingsrook.qqq.backend.core.actions.values.QCustomPossibleValueProvider;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.values.SearchPossibleValueSourceInput;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;


/*******************************************************************************
 ** possible value source (meta-data producer and custom PVS value provider)
 ** that provides field-name PVS to select a field from a table.
 *******************************************************************************/
public class RecordWorkflowFieldNamePossibleValueSource implements QCustomPossibleValueProvider<String>, MetaDataProducerInterface<QPossibleValueSource>
{
   private static final QLogger LOG = QLogger.getLogger(RecordWorkflowFieldNamePossibleValueSource.class);

   public static final String NAME = "RecordWorkflowFieldName";



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QPossibleValueSource produce(QInstance qInstance) throws QException
   {
      return new QPossibleValueSource()
         .withName(NAME)
         .withType(QPossibleValueSourceType.CUSTOM)
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
      List<QPossibleValue<String>> rs = new ArrayList<>();

      String tableName = getTableName(input);

      if(!StringUtils.hasContent(tableName))
      {
         LOG.info("No tableName given - returning empty list");
         return Collections.emptyList();
      }

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // todo - how do we know in case there's more than once instance of the qbit, which one this PVS is for? //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      Optional<QBitMetaData> optionalQBitConfig = QContext.getQInstance().getQBits().values().stream().filter(qb -> qb.getConfig() instanceof WorkflowsQBitConfig).findFirst();
      if(WorkflowsQBitConfig.getApiMiddlewareModuleAvailable() && optionalQBitConfig.isPresent() && ((WorkflowsQBitConfig) (optionalQBitConfig.get().getConfig())).getIncludeApiVersions())
      {
         /////////////////////////////////////////////////////////////////////////////////////////////////
         // if we have the api middleware module, and we're configured to use api versions, then, do so //
         /////////////////////////////////////////////////////////////////////////////////////////////////
         String apiName    = null;
         String apiVersion = null;

         String workflowRevisionValuesJSON = ValueUtils.getValueAsString(CollectionUtils.nonNullMap(input.getOtherValues()).get("workflowRevisionValuesJSON"));
         if(StringUtils.hasContent(workflowRevisionValuesJSON))
         {
            JSONObject workflowRevisionValuesJSONObject = new JSONObject(workflowRevisionValuesJSON);
            if(workflowRevisionValuesJSONObject.has("apiName"))
            {
               apiName = workflowRevisionValuesJSONObject.getString("apiName");
            }
            if(workflowRevisionValuesJSONObject.has("apiVersion"))
            {
               apiVersion = workflowRevisionValuesJSONObject.getString("apiVersion");
            }
         }

         if(!StringUtils.hasContent(apiName) || !StringUtils.hasContent(apiVersion))
         {
            LOG.info("No apiName or apiVersion given - returning empty list");
            return Collections.emptyList();
         }

         GetTableApiFieldsOutput tableApiFieldsOutput = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withApiName(apiName).withVersion(apiVersion).withTableName(tableName));
         for(QFieldMetaData field : tableApiFieldsOutput.getFields())
         {
            rs.add(new QPossibleValue<>(tableName + "." + field.getName(), field.getLabel()));
         }
      }
      else
      {
         ////////////////////////////////////
         // else, non-api-versioned fields //
         ////////////////////////////////////
         for(QFieldMetaData field : QContext.getQInstance().getTable(tableName).getFields().values())
         {
            rs.add(new QPossibleValue<>(tableName + "." + field.getName(), field.getLabel()));
         }
      }

      return completeCustomPVSSearch(input, rs);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected String getTableName(SearchPossibleValueSourceInput input)
   {
      String tableName          = null;
      String workflowValuesJSON = ValueUtils.getValueAsString(CollectionUtils.nonNullMap(input.getOtherValues()).get("workflowValuesJSON"));
      if(StringUtils.hasContent(workflowValuesJSON))
      {
         JSONObject workflowValuesJSONObject = new JSONObject(workflowValuesJSON);
         if(workflowValuesJSONObject.has("tableName"))
         {
            tableName = workflowValuesJSONObject.getString("tableName");
         }
      }
      return tableName;
   }

}
