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

package com.kingsrook.qbits.workflows;


import java.util.List;
import java.util.Optional;
import com.kingsrook.qbits.workflows.tracing.WorkflowRunLogTracer;
import com.kingsrook.qbits.workflows.tracing.WorkflowTracerInterface;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.ClassPathUtils;


/*******************************************************************************
 ** Configuration data for this qbit.
 **
 *******************************************************************************/
public class WorkflowsQBitConfig implements QBitConfig
{
   private MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer;

   private static boolean apiMiddlewareModuleAvailable;

   private boolean        includeApiVersions          = false;
   private String         customStoreNewWorkflowRevisionProcessName;
   private String         componentSourceUrl;
   private QCodeReference workflowTracerCodeReference = new QCodeReference(WorkflowRunLogTracer.class);

   private boolean includeRecordWorkflows = true;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public WorkflowsQBitConfig()
   {
      apiMiddlewareModuleAvailable = ClassPathUtils.isClassAvailable(ApiInstanceMetaData.class.getName());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static boolean isApiModuleAvailableAndDoesQBitIncludeApiVersions()
   {
      Optional<QBitMetaData> optionalQBitConfig = QContext.getQInstance().getQBits().values().stream().filter(qb -> qb.getConfig() instanceof WorkflowsQBitConfig).findFirst();
      return (WorkflowsQBitConfig.getApiMiddlewareModuleAvailable() && optionalQBitConfig.isPresent() && ((WorkflowsQBitConfig) (optionalQBitConfig.get().getConfig())).getIncludeApiVersions());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      if(includeApiVersions && !apiMiddlewareModuleAvailable)
      {
         errors.add("Workflows QBit is configured to includeApiVersions, but the qqq-middleware-api module is available.");
      }

      if(workflowTracerCodeReference != null)
      {
         QInstanceValidator qInstanceValidator = new QInstanceValidator();
         qInstanceValidator.validateSimpleCodeReference("Workflows qbit config workflowTracerCodeReference", workflowTracerCodeReference, WorkflowTracerInterface.class);
         errors.addAll(qInstanceValidator.getErrors());
      }
   }



   /*******************************************************************************
    ** Getter for tableMetaDataCustomizer
    *******************************************************************************/
   public MetaDataCustomizerInterface<QTableMetaData> getTableMetaDataCustomizer()
   {
      return (this.tableMetaDataCustomizer);
   }



   /*******************************************************************************
    ** Setter for tableMetaDataCustomizer
    *******************************************************************************/
   public void setTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer)
   {
      this.tableMetaDataCustomizer = tableMetaDataCustomizer;
   }



   /*******************************************************************************
    ** Fluent setter for tableMetaDataCustomizer
    *******************************************************************************/
   public WorkflowsQBitConfig withTableMetaDataCustomizer(MetaDataCustomizerInterface<QTableMetaData> tableMetaDataCustomizer)
   {
      this.tableMetaDataCustomizer = tableMetaDataCustomizer;
      return (this);
   }



   /*******************************************************************************
    ** Getter for includeApiVersions
    *******************************************************************************/
   public boolean getIncludeApiVersions()
   {
      return (this.includeApiVersions);
   }



   /*******************************************************************************
    ** Setter for includeApiVersions
    *******************************************************************************/
   public void setIncludeApiVersions(boolean includeApiVersions)
   {
      this.includeApiVersions = includeApiVersions;
   }



   /*******************************************************************************
    ** Fluent setter for includeApiVersions
    *******************************************************************************/
   public WorkflowsQBitConfig withIncludeApiVersions(boolean includeApiVersions)
   {
      this.includeApiVersions = includeApiVersions;
      return (this);
   }



   /*******************************************************************************
    ** Getter for apiMiddlewareModuleAvailable
    **
    *******************************************************************************/
   public static boolean getApiMiddlewareModuleAvailable()
   {
      return apiMiddlewareModuleAvailable;
   }



   /*******************************************************************************
    ** Getter for customStoreNewWorkflowRevisionProcessName
    *******************************************************************************/
   public String getCustomStoreNewWorkflowRevisionProcessName()
   {
      return (this.customStoreNewWorkflowRevisionProcessName);
   }



   /*******************************************************************************
    ** Setter for customStoreNewWorkflowRevisionProcessName
    *******************************************************************************/
   public void setCustomStoreNewWorkflowRevisionProcessName(String customStoreNewWorkflowRevisionProcessName)
   {
      this.customStoreNewWorkflowRevisionProcessName = customStoreNewWorkflowRevisionProcessName;
   }



   /*******************************************************************************
    ** Fluent setter for customStoreNewWorkflowRevisionProcessName
    *******************************************************************************/
   public WorkflowsQBitConfig withCustomStoreNewWorkflowRevisionProcessName(String customStoreNewWorkflowRevisionProcessName)
   {
      this.customStoreNewWorkflowRevisionProcessName = customStoreNewWorkflowRevisionProcessName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for componentSourceUrl
    *******************************************************************************/
   public String getComponentSourceUrl()
   {
      return (this.componentSourceUrl);
   }



   /*******************************************************************************
    ** Setter for componentSourceUrl
    *******************************************************************************/
   public void setComponentSourceUrl(String componentSourceUrl)
   {
      this.componentSourceUrl = componentSourceUrl;
   }



   /*******************************************************************************
    ** Fluent setter for componentSourceUrl
    *******************************************************************************/
   public WorkflowsQBitConfig withComponentSourceUrl(String componentSourceUrl)
   {
      this.componentSourceUrl = componentSourceUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowTracerCodeReference
    *******************************************************************************/
   public QCodeReference getWorkflowTracerCodeReference()
   {
      return (this.workflowTracerCodeReference);
   }



   /*******************************************************************************
    ** Setter for workflowTracerCodeReference
    *******************************************************************************/
   public void setWorkflowTracerCodeReference(QCodeReference workflowTracerCodeReference)
   {
      this.workflowTracerCodeReference = workflowTracerCodeReference;
   }



   /*******************************************************************************
    ** Fluent setter for workflowTracerCodeReference
    *******************************************************************************/
   public WorkflowsQBitConfig withWorkflowTracerCodeReference(QCodeReference workflowTracerCodeReference)
   {
      this.workflowTracerCodeReference = workflowTracerCodeReference;
      return (this);
   }



   /*******************************************************************************
    * Getter for includeRecordWorkflows
    * @see #withIncludeRecordWorkflows(boolean)
    *******************************************************************************/
   public boolean getIncludeRecordWorkflows()
   {
      return (this.includeRecordWorkflows);
   }



   /*******************************************************************************
    * Setter for includeRecordWorkflows
    * @see #withIncludeRecordWorkflows(boolean)
    *******************************************************************************/
   public void setIncludeRecordWorkflows(boolean includeRecordWorkflows)
   {
      this.includeRecordWorkflows = includeRecordWorkflows;
   }



   /*******************************************************************************
    * Fluent setter for includeRecordWorkflows
    *
    * @param includeRecordWorkflows
    * Control if loading this QBit also loads the RecordWorkflow workflow type,
    * as defined in RecordWorkflowsDefinition.  Default value is true.  Applications
    * may want to set this to false, if they either don't want this workflow at all,
    * or if they want to override RecordWorkflowsDefinition, e.g., to customize
    * it, such as adding additional step types.
    *
    * @return this
    *******************************************************************************/
   public WorkflowsQBitConfig withIncludeRecordWorkflows(boolean includeRecordWorkflows)
   {
      this.includeRecordWorkflows = includeRecordWorkflows;
      return (this);
   }

}
