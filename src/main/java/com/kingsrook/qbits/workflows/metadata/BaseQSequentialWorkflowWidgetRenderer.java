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
import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qbits.workflows.WorkflowsQBitConfig;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.AbstractWidgetRenderer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetOutput;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.QWidgetData;
import com.kingsrook.qqq.backend.core.model.dashboard.widgets.WidgetType;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.QWidgetMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitComponentMetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** base class for widgets (both renderers and meta-data-producers) that host
 ** the QSequentialWorkflow dynamic component.
 *******************************************************************************/
public abstract class BaseQSequentialWorkflowWidgetRenderer extends AbstractWidgetRenderer
   implements MetaDataProducerInterface<QWidgetMetaData>, QBitComponentMetaDataProducerInterface<QWidgetMetaData, WorkflowsQBitConfig>
{
   private static final QLogger LOG = QLogger.getLogger(BaseQSequentialWorkflowWidgetRenderer.class);

   private WorkflowsQBitConfig qBitConfig = null;



   /***************************************************************************
    **
    ***************************************************************************/
   protected enum ReadonlyState
   {
      INITIALLY,
      ALWAYS
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract String getWidgetName();


   /***************************************************************************
    **
    ***************************************************************************/
   protected abstract ReadonlyState getReadonlyState();


   /***************************************************************************
    *
    ***************************************************************************/
   protected abstract OutputData getOutputData(RenderWidgetInput input) throws QException;



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public QWidgetMetaData produce(QInstance qInstance) throws QException
   {
      /////////////////////////////////////////////////////////////
      // allow a custom URL for the component to be given in env //
      // useful for dev against a local build                    //
      /////////////////////////////////////////////////////////////
      String componentSourceUrl = new QMetaDataVariableInterpreter().interpret("${env.Q_SEQUENTIAL_WORKFLOW_COMPONENT_SOURCE_URL}");
      if(StringUtils.hasContent(componentSourceUrl))
      {
         LOG.debug("Using component source url from environment", logPair("url", componentSourceUrl));
      }
      else
      {
         componentSourceUrl = "/dynamic-qfmd-components/q-sequential-workflow.bundle.js";
      }

      Map<String, Serializable> defaultValues = MapBuilder.of(
         "componentName", "QSequentialWorkflow",
         "componentSourceUrl", componentSourceUrl,
         "readonlyState", getReadonlyState().name().toLowerCase(),
         "sx", new HashMap<>(Map.of("height", "calc(90vh)", "minHeight", "600px"))
      );

      if(qBitConfig != null)
      {
         if(StringUtils.hasContent(qBitConfig.getCustomStoreNewWorkflowRevisionProcessName()))
         {
            defaultValues.put("customStoreNewWorkflowRevisionProcessName", qBitConfig.getCustomStoreNewWorkflowRevisionProcessName());
         }
         if(StringUtils.hasContent(qBitConfig.getComponentSourceUrl()))
         {
            defaultValues.put("componentSourceUrl", qBitConfig.getComponentSourceUrl());
         }
      }

      QWidgetMetaData workflowWidget = new QWidgetMetaData()
         .withName(getWidgetName())
         .withType(WidgetType.CUSTOM_COMPONENT.getType())
         .withCodeReference(new QCodeReference(getClass()))
         .withGridColumns(12)
         .withIsCard(true)
         .withLabel("Workflow Steps")
         .withDefaultValues(defaultValues);
      return workflowWidget;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public RenderWidgetOutput render(RenderWidgetInput input) throws QException
   {
      return new RenderWidgetOutput(getOutputData(input));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static class OutputData extends QWidgetData
   {
      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public String getType()
      {
         return WidgetType.CUSTOM_COMPONENT.getType();
      }
   }



   /*******************************************************************************
    ** Getter for qBitConfig
    *******************************************************************************/
   @Override
   public WorkflowsQBitConfig getQBitConfig()
   {
      return (this.qBitConfig);
   }



   /*******************************************************************************
    ** Setter for qBitConfig
    *******************************************************************************/
   @Override
   public void setQBitConfig(WorkflowsQBitConfig qBitConfig)
   {
      this.qBitConfig = qBitConfig;
   }

}
