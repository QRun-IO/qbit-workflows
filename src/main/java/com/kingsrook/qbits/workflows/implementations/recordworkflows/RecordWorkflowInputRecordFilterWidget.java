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
