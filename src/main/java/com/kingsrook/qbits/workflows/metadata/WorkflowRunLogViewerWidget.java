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


import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.model.WorkflowRunLogStep;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.widgets.RenderWidgetInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** widget for viewing a workflow run log on the workflow revision table.
 *******************************************************************************/
public class WorkflowRunLogViewerWidget extends BaseQSequentialWorkflowWidgetRenderer
{
   public static final String NAME = "WorkflowRunLogViewerWidget";



   /***************************************************************************
    **
    ***************************************************************************/
   protected String getWidgetName()
   {
      return (NAME);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected ReadonlyState getReadonlyState()
   {
      return ReadonlyState.ALWAYS;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   protected OutputData getOutputData(RenderWidgetInput input) throws QException
   {
      Integer              workflowRunLogId    = ValueUtils.getValueAsInteger(input.getQueryParams().get("id"));
      QRecord              workflowRunLog      = GetAction.execute(WorkflowRunLog.TABLE_NAME, workflowRunLogId);
      List<QRecord>        workflowRunLogSteps = QueryAction.execute(WorkflowRunLogStep.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowRunLogId", QCriteriaOperator.EQUALS, workflowRunLogId)));
      List<Integer>        executedStepIds     = workflowRunLogSteps.stream().map(wrls -> wrls.getValueInteger("workflowStepId")).toList();
      Map<Integer, String> stepOutputMap       = CollectionUtils.listToMap(workflowRunLogSteps, e -> e.getValueInteger("workflowStepId"), e -> e.getValueString("outputData"));
      Map<Integer, String> stepLogMessageMap   = CollectionUtils.listToMap(workflowRunLogSteps, e -> e.getValueInteger("workflowStepId"), e -> e.getValueString("message"));

      return new OutputData()
      {

         /***************************************************************************
          **
          ***************************************************************************/
         public List<Integer> getExecutedStepIds()
         {
            return (executedStepIds);
         }



         /***************************************************************************
          **
          ***************************************************************************/
         public Map<Integer, String> getStepOutputMap()
         {
            return (stepOutputMap);
         }



         /***************************************************************************
          **
          ***************************************************************************/
         public Map<Integer, String> getStepLogMessageMap()
         {
            return (stepLogMessageMap);
         }



         /***************************************************************************
          **
          ***************************************************************************/
         public Integer getWorkflowRevisionId()
         {
            return (workflowRunLog.getValueInteger("workflowRevisionId"));
         }

      };
   }
}
