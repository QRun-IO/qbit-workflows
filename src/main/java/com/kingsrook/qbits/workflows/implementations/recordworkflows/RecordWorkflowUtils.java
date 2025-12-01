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


import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutionContext;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qqq.api.actions.GetTableApiFieldsAction;
import com.kingsrook.qqq.api.utils.ApiQueryFilterUtils;
import com.kingsrook.qqq.backend.core.actions.tables.CountAction;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.QQueryFilterFormatter;


/*******************************************************************************
 ** utility methods for record workflows.
 *******************************************************************************/
public class RecordWorkflowUtils
{
   public static final Integer DELETE_VIA_FILTER_THRESHOLD = 10_000;



   /***************************************************************************
    **
    ***************************************************************************/
   public static QRecord getRecordFromContext(WorkflowExecutionContext context) throws QException
   {
      QRecord record = (QRecord) context.getValues().get("record");
      if(record == null)
      {
         throw (new QException("Missing record in workflow context."));
      }

      return (record);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void updateRecordInContext(WorkflowExecutionContext context, QRecord record)
   {
      context.getValues().put("record", record);
   }



   /***************************************************************************
    * expects name in inputValues to be "queryFilterJson"
    ***************************************************************************/
   public static QQueryFilter getFilterFromInput(Map<String, ?> inputValues) throws QException
   {
      Object object = inputValues.get("queryFilterJson");
      if(object == null)
      {
         throw (new QException("Could not find filter in input values"));
      }

      return getFilterFromJsonOrObject(object);
   }



   /***************************************************************************
    * supported input types are:  QQueryFilter, String (json), Map.
    ***************************************************************************/
   public static QQueryFilter getFilterFromJsonOrObject(Object object) throws QException
   {
      try
      {
         if(object instanceof QQueryFilter qQueryFilter)
         {
            return (qQueryFilter);
         }
         else if(object instanceof String string && string.startsWith("{"))
         {
            return (JsonUtils.toObject(string, QQueryFilter.class));
         }
         else if(object instanceof Map map)
         {
            return (JsonUtils.toObject(JsonUtils.toJson(map), QQueryFilter.class));
         }
         else
         {
            throw new QException("Filter in input values was not a recognized object (" + object.getClass() + ")");
         }
      }
      catch(IOException e)
      {
         throw new QException("Error getting filter from Object", e);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static String getDynamicStepSummaryForFilter(String tableName, Map<String, Serializable> inputValues) throws QException
   {
      try
      {
         QQueryFilter filter = RecordWorkflowUtils.getFilterFromInput(inputValues);
         // todo - if api aware... this method we're calling, it assumes current-version, so, yeah...
         return QQueryFilterFormatter.formatQueryFilter(tableName, filter);
      }
      catch(Exception e)
      {
         return "No filter";
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static Integer deleteChildRecordsViaFilter(String parentFieldName, Integer parentId, QQueryFilter inputFilter, String tableName, WorkflowExecutionContext context) throws QException
   {
      QQueryFilter actualFilter = new QQueryFilter()
         .withCriteria(new QFilterCriteria(parentFieldName, QCriteriaOperator.EQUALS, parentId))
         .withSubFilter(inputFilter);

      Integer count = new CountAction().execute(new CountInput(tableName)
         .withTransaction(context.getTransaction())
         .withFilter(actualFilter)).getCount();

      if(count > DELETE_VIA_FILTER_THRESHOLD)
      {
         throw (new QException("Filter for delete matched more than the allowed number of records (" + DELETE_VIA_FILTER_THRESHOLD + ")"));
      }
      else if(count == 0)
      {
         return (0);
      }
      else
      {
         DeleteOutput deleteOutput = new DeleteAction().execute(new DeleteInput(tableName)
            .withTransaction(context.getTransaction())
            .withQueryFilter(actualFilter));

         return (deleteOutput.getDeletedRecordCount());
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static void updateFilterForApi(WorkflowExecutionContext context, QQueryFilter inputFilter) throws QException
   {
      WorkflowRevision            workflowRevision   = context.getWorkflowRevision();
      Workflow                    workflow           = context.getWorkflow();
      Map<String, QFieldMetaData> tableApiFields     = GetTableApiFieldsAction.getTableApiFieldMap(new GetTableApiFieldsAction.ApiNameVersionAndTableName(workflowRevision.getApiName(), workflowRevision.getApiVersion(), workflow.getTableName()));
      List<String>                badRequestMessages = new ArrayList<>();

      CountInput countInput = new CountInput(context.getWorkflow().getTableName())
         .withTransaction(context.getTransaction());

      ApiQueryFilterUtils.manageCriteriaFields(inputFilter, tableApiFields, badRequestMessages, workflowRevision.getApiName(), workflowRevision.getApiVersion(), countInput);
   }

}
