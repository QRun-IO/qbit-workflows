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
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryRecordLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetOutput;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.NoopTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Transform step run-record-workflow process.  extends no-op, but main purpose is
 ** to set FIELD_VALIDATION_SUMMARY in the output, with the name of the workflow
 ** you selected - this is because the process is set to not do full-validation,
 ** so the process summary isn't otherwise ever ran.
 *******************************************************************************/
public class RunRecordWorkflowTransformStep extends NoopTransformStep
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> processSummary = new ArrayList<>();

      try
      {
         Serializable workflowId = runBackendStepOutput.getValue("workflowId");
         GetInput     getInput   = new GetInput();
         getInput.setTableName(Workflow.TABLE_NAME);
         getInput.setPrimaryKey(workflowId);
         GetOutput getOutput = new GetAction().execute(getInput);
         if(getOutput.getRecord() != null)
         {
            processSummary.add(new ProcessSummaryRecordLink(Status.OK, Workflow.TABLE_NAME, workflowId, getOutput.getRecord().getValueString("name"))
               .withLinkPreText(StringUtils.plural(runBackendStepOutput.getRecords(), "It", "They") + " will have the workflow ")
               .withLinkPostText(" ran against " + StringUtils.plural(runBackendStepOutput.getRecords(), "it.", "them.")));
         }
         else
         {
            processSummary.add(new ProcessSummaryLine(Status.ERROR, null, "The selected workflow could not be found."));
         }
      }
      catch(Exception e)
      {
         processSummary.add(new ProcessSummaryLine(Status.ERROR, null, "Error getting the workflow: " + e.getMessage()));
      }

      return (processSummary);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      super.runOnePage(runBackendStepInput, runBackendStepOutput);

      runBackendStepOutput.addValue(StreamedETLWithFrontendProcess.FIELD_VALIDATION_SUMMARY, doGetProcessSummary(runBackendStepOutput, false));
   }

}
