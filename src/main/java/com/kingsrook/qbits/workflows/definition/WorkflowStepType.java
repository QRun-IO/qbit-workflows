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

package com.kingsrook.qbits.workflows.definition;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qbits.workflows.execution.WorkflowStepExecutorInterface;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.json.JSONObject;


/*******************************************************************************
 ** the definition for a kind of step that can be used within a workflow
 *******************************************************************************/
public class WorkflowStepType implements Serializable
{
   private String         name;
   private String         label;
   private String         iconUrl;
   private String         description;
   private QCodeReference executor;
   private QCodeReference validator;

   private OutboundLinkMode         outboundLinkMode;
   private List<OutboundLinkOption> outboundLinkOptions;

   private Set<String> allowedDescendantStepTypes;

   private ArrayList<QFieldMetaData> inputFields      = new ArrayList<>();
   private ArrayList<String>         inputWidgetNames = new ArrayList<>();



   /***************************************************************************
    **
    ***************************************************************************/
   @Deprecated(since = "replaced by version that takes map instead of JSONObject")
   public String getDynamicStepSummary(Integer workflowId, JSONObject values) throws QException
   {
      Map<String, Serializable> valuesMap = ValueUtils.getValueAsMap(values.toString());
      return (getDynamicStepSummary(workflowId, valuesMap));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public String getDynamicStepSummary(Integer workflowId, Map<String, Serializable> inputValues) throws QException
   {
      return (null);
   }



   /***************************************************************************
    * for a variable-output type step, it must dynamically define its outbound
    * links
    ***************************************************************************/
   public List<OutboundLinkOption> getDynamicOutboundLinkOptions(Integer workflowId, Map<String, Serializable> inputValues) throws QException
   {
      return (null);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void enrich(QInstance qInstance)
   {
      if(!StringUtils.hasContent(label))
      {
         label = QInstanceEnricher.nameToLabel(name);
      }

      QInstanceEnricher enricher = new QInstanceEnricher(qInstance);
      for(QFieldMetaData field : CollectionUtils.nonNullList(inputFields))
      {
         enricher.enrichField(field);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public void validate(QInstanceValidator qInstanceValidator, QInstance qInstance)
   {
      qInstanceValidator.assertCondition(StringUtils.hasContent(name), "WorkflowStepType name is required.");
      if(qInstanceValidator.assertCondition(outboundLinkMode != null, "WorkflowStepType [" + name + "]: outboundLinkMode is required."))
      {
         if(OutboundLinkMode.CONTAINER.equals(outboundLinkMode))
         {
            qInstanceValidator.assertCondition(executor == null, "WorkflowStepType [" + name + "]: executor is not allowed for outboundLinkMode != CONTAINER.");
         }
         else
         {
            qInstanceValidator.assertCondition(executor != null, "WorkflowStepType [" + name + "]: executor is required (for outboundLinkMode != CONTAINER).");
         }
      }

      qInstanceValidator.assertNoException(() -> QCodeLoader.getAdHoc(WorkflowStepExecutorInterface.class, this.executor),
         "WorkflowStepType [" + name + "]: executor could not be loaded as an instance of WorkflowStepExecutorInterface");

      for(QFieldMetaData field : CollectionUtils.nonNullList(inputFields))
      {
         qInstanceValidator.validateFieldSupplementalMetaData(field, qInstance);
      }
   }



   /*******************************************************************************
    ** Getter for name
    *******************************************************************************/
   public String getName()
   {
      return (this.name);
   }



   /*******************************************************************************
    ** Setter for name
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Fluent setter for name
    *******************************************************************************/
   public WorkflowStepType withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for executor
    *******************************************************************************/
   public QCodeReference getExecutor()
   {
      return (this.executor);
   }



   /*******************************************************************************
    ** Setter for executor
    *******************************************************************************/
   public void setExecutor(QCodeReference executor)
   {
      this.executor = executor;
   }



   /*******************************************************************************
    ** Fluent setter for executor
    *******************************************************************************/
   public WorkflowStepType withExecutor(QCodeReference executor)
   {
      this.executor = executor;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    *******************************************************************************/
   public String getLabel()
   {
      return (this.label);
   }



   /*******************************************************************************
    ** Setter for label
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Fluent setter for label
    *******************************************************************************/
   public WorkflowStepType withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for description
    *******************************************************************************/
   public String getDescription()
   {
      return (this.description);
   }



   /*******************************************************************************
    ** Setter for description
    *******************************************************************************/
   public void setDescription(String description)
   {
      this.description = description;
   }



   /*******************************************************************************
    ** Fluent setter for description
    *******************************************************************************/
   public WorkflowStepType withDescription(String description)
   {
      this.description = description;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputFields
    *******************************************************************************/
   public List<QFieldMetaData> getInputFields()
   {
      return (this.inputFields);
   }



   /*******************************************************************************
    ** Setter for inputFields
    *******************************************************************************/
   public void setInputFields(List<QFieldMetaData> inputFields)
   {
      this.inputFields = CollectionUtils.useOrWrap(inputFields, new TypeToken<>() {});
   }



   /*******************************************************************************
    ** Fluent setter for inputFields
    *******************************************************************************/
   public WorkflowStepType withInputFields(List<QFieldMetaData> inputFields)
   {
      setInputFields(inputFields);
      return (this);
   }



   /*******************************************************************************
    ** Getter for outboundLinkMode
    *******************************************************************************/
   public OutboundLinkMode getOutboundLinkMode()
   {
      return (this.outboundLinkMode);
   }



   /*******************************************************************************
    ** Setter for outboundLinkMode
    *******************************************************************************/
   public void setOutboundLinkMode(OutboundLinkMode outboundLinkMode)
   {
      this.outboundLinkMode = outboundLinkMode;
   }



   /*******************************************************************************
    ** Fluent setter for outboundLinkMode
    *******************************************************************************/
   public WorkflowStepType withOutboundLinkMode(OutboundLinkMode outboundLinkMode)
   {
      this.outboundLinkMode = outboundLinkMode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for iconUrl
    *******************************************************************************/
   public String getIconUrl()
   {
      return (this.iconUrl);
   }



   /*******************************************************************************
    ** Setter for iconUrl
    *******************************************************************************/
   public void setIconUrl(String iconUrl)
   {
      this.iconUrl = iconUrl;
   }



   /*******************************************************************************
    ** Fluent setter for iconUrl
    *******************************************************************************/
   public WorkflowStepType withIconUrl(String iconUrl)
   {
      this.iconUrl = iconUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputWidgetNames
    *******************************************************************************/
   public ArrayList<String> getInputWidgetNames()
   {
      return (this.inputWidgetNames);
   }



   /*******************************************************************************
    ** Setter for inputWidgetNames
    *******************************************************************************/
   public void setInputWidgetNames(List<String> inputWidgetNames)
   {
      this.inputWidgetNames = CollectionUtils.useOrWrap(inputWidgetNames, new TypeToken<>() {});
   }



   /*******************************************************************************
    ** Fluent setter for inputWidgetNames
    *******************************************************************************/
   public WorkflowStepType withInputWidgetNames(List<String> inputWidgetNames)
   {
      setInputWidgetNames(inputWidgetNames);
      return (this);
   }



   /*******************************************************************************
    ** Getter for outboundLinkOptions
    *******************************************************************************/
   public List<OutboundLinkOption> getOutboundLinkOptions()
   {
      return (this.outboundLinkOptions);
   }



   /*******************************************************************************
    ** Setter for outboundLinkOptions
    *******************************************************************************/
   public void setOutboundLinkOptions(List<OutboundLinkOption> outboundLinkOptions)
   {
      this.outboundLinkOptions = outboundLinkOptions;
   }



   /*******************************************************************************
    ** Fluent setter for outboundLinkOptions
    *******************************************************************************/
   public WorkflowStepType withOutboundLinkOptions(List<OutboundLinkOption> outboundLinkOptions)
   {
      this.outboundLinkOptions = outboundLinkOptions;
      return (this);
   }



   /*******************************************************************************
    * Getter for validator
    * @see #withValidator(QCodeReference)
    *******************************************************************************/
   public QCodeReference getValidator()
   {
      return (this.validator);
   }



   /*******************************************************************************
    * Setter for validator
    * @see #withValidator(QCodeReference)
    *******************************************************************************/
   public void setValidator(QCodeReference validator)
   {
      this.validator = validator;
   }



   /*******************************************************************************
    * Fluent setter for validator
    *
    * @param validator
    * Optional Code Reference to an implementation of WorkflowStepValidatorInterface,
    * @return this
    *******************************************************************************/
   public WorkflowStepType withValidator(QCodeReference validator)
   {
      this.validator = validator;
      return (this);
   }



   /*******************************************************************************
    * Getter for allowedDescendantStepTypes
    * @see #withAllowedDescendantStepTypes(Set)
    *******************************************************************************/
   public Set<String> getAllowedDescendantStepTypes()
   {
      return (this.allowedDescendantStepTypes);
   }



   /*******************************************************************************
    * Setter for allowedDescendantStepTypes
    * @see #withAllowedDescendantStepTypes(Set)
    *******************************************************************************/
   public void setAllowedDescendantStepTypes(Set<String> allowedDescendantStepTypes)
   {
      this.allowedDescendantStepTypes = allowedDescendantStepTypes;
   }



   /*******************************************************************************
    * Fluent setter for allowedDescendantStepTypes
    *
    * @param allowedDescendantStepTypes
    * to limit what step types can be placed inside this step type within a graph.
    * for example,
    * @return this
    *******************************************************************************/
   public WorkflowStepType withAllowedDescendantStepTypes(Set<String> allowedDescendantStepTypes)
   {
      this.allowedDescendantStepTypes = allowedDescendantStepTypes;
      return (this);
   }


}
