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


import java.util.Collections;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.FilterAndColumnsSetupData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Widget for setting up a filter on the table being used in a record workflow.
 *******************************************************************************/
public class RecordWorkflowInputRecordFilterWidget extends AbstractWidgetRenderer implements MetaDataProducerInterface<QWidgetMetaData>
{
   public static final String NAME = "RecordWorkflowGenericFilterWidget";



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      QWidgetMetaData widget = new QWidgetMetaData()
         .withName(NAME)
         .withLabel("Filter")
         .withIsCard(false)
         .withType(WidgetType.FILTER_AND_COLUMNS_SETUP.getType())
         .withCodeReference(new QCodeReference(getClass()));

      return (widget);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      String tableName         = null;
      String workflowTableName = input.getQueryParams().get("workflow.tableName");
      if(StringUtils.hasContent(workflowTableName))
      {
         tableName = workflowTableName;
      }

      FilterAndColumnsSetupData widgetData = new FilterAndColumnsSetupData(tableName, false, true, Collections.emptyList());
      widgetData.setHidePreview(true);
      widgetData.setHideSortBy(true);
      widgetData.setOverrideIsEditable(true);
      widgetData.setIsApiVersioned(true);

      applyApiNameAndVersionFromInputToWidgetData(input, widgetData);

      return new RenderWidgetOutput(widgetData);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void applyApiNameAndVersionFromInputToWidgetData(RenderWidgetInput input, FilterAndColumnsSetupData widgetData)
   {
      String apiName = input.getQueryParams().get("workflowRevision.apiName");
      if(!StringUtils.hasContent(apiName))
      {
         apiName = input.getQueryParams().get("apiName");
      }

      if(StringUtils.hasContent(apiName))
      {
         widgetData.setApiName(apiName);
         ApiInstanceMetaDataContainer apiInstanceMetaDataContainer = ApiInstanceMetaDataContainer.of(QContext.getQInstance());
         if(apiInstanceMetaDataContainer != null)
         {
            ApiInstanceMetaData apiInstanceMetaData = apiInstanceMetaDataContainer.getApis().get(apiName);
            if(apiInstanceMetaData != null)
            {
               widgetData.setApiPath(apiInstanceMetaData.getPath().replaceFirst("^/+", "").replaceFirst("/+$", ""));
            }
         }
      }

      String apiVersion = input.getQueryParams().get("workflowRevision.apiVersion");
      if(!StringUtils.hasContent(apiVersion))
      {
         apiVersion = input.getQueryParams().get("apiVersion");
      }

      if(StringUtils.hasContent(apiVersion))
      {
         widgetData.setApiVersion(apiVersion);
      }
   }
}
