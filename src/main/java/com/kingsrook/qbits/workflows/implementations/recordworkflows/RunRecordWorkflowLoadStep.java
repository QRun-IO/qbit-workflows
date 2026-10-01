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
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.execution.WorkflowExecutor;
import com.kingsrook.qbits.workflows.execution.WorkflowInput;
import com.kingsrook.qbits.workflows.execution.WorkflowOutput;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowRunLog;
import com.kingsrook.qbits.workflows.tracing.WorkflowTracerInterface;
import com.kingsrook.qqq.backend.core.actions.audits.AuditAction;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.audits.AuditInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryFilterLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.audits.AuditsMetaDataProvider;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.tables.QQQTableTableManager;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Load step for the runRecordWorkflow process - runs the workflow on a page of records
 *******************************************************************************/
public class RunRecordWorkflowLoadStep extends AbstractLoadStep implements ProcessSummaryProviderInterface
{
   private static final QLogger LOG = QLogger.getLogger(RunRecordWorkflowLoadStep.class);

   private ProcessSummaryLine okLine = new ProcessSummaryLine(Status.OK)
      .withSingularPastMessage("had the workflow ran against it.")
      .withPluralPastMessage("had the workflow ran against them.");

   private ProcessSummaryLine unloggedExceptionLine = new ProcessSummaryLine(Status.ERROR, null, "had an error that was not logged.");

   private List<Serializable> okWorkflowLogIds    = new ArrayList<>();
   private List<Serializable> errorWorkflowLogIds = new ArrayList<>();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getOverrideRecordPipeCapacity(RunBackendStepInput runBackendStepInput)
   {
      //////////////////////////////
      // todo think about this... //
      //////////////////////////////
      return (1);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void runOnePage(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      runBackendStepInput.getAsyncJobCallback().updateStatus("Running workflow");

      Integer workflowId = runBackendStepInput.getValueInteger("workflowId");
      QRecord workflow   = GetAction.execute(Workflow.TABLE_NAME, workflowId);
      if(workflow == null)
      {
         throw (new QException("Could not find workflow by id: " + workflowId));
      }

      String         tableName = workflow.getValueString("tableName");
      QTableMetaData table     = QContext.getQInstance().getTable(tableName);

      for(QRecord record : runBackendStepInput.getRecords())
      {
         okLine.incrementCountAndAddPrimaryKey(record.getValueInteger(table.getPrimaryKeyField()));
         runBackendStepInput.getAsyncJobCallback().incrementCurrent();

         WorkflowInput workflowInput = new WorkflowInput();
         workflowInput.setWorkflowId(workflowId);
         workflowInput.setValues(Map.of("record", record));
         workflowInput.setWorkflowExecutionContext(new RecordWorkflowContext());

         WorkflowOutput workflowOutput = new WorkflowOutput();

         WorkflowRunLog workflowRunLog = new WorkflowRunLog()
            .withInputRecordQqqTableId(QQQTableTableManager.getQQQTableId(QContext.getQInstance(), tableName))
            .withInputRecordId(record.getValueInteger(table.getPrimaryKeyField()))
            .withInputDataJson("""
               {"tableName":"%s","id", %s}""".formatted(record.getTableName(), record.getValue("id")));

         WorkflowTracerInterface workflowTracer = getWorkflowTracer(runBackendStepInput);

         new WorkflowExecutor()
            .withInputWorkflowRunLog(workflowRunLog)
            .withWorkflowTracer(workflowTracer)
            .execute(workflowInput, workflowOutput);

         workflowRunLog = workflowOutput.getWorkflowRunLog();

         String auditMessage = "Workflow \"" + workflow.getValueString("name") + "\" (id: " + workflowId + ") was executed against this record";

         //////////////////////////////////////////////////////////
         // add the record to the appropriate processSummaryLine //
         //////////////////////////////////////////////////////////
         if(workflowRunLog.getId() != null)
         {
            Long id = workflowRunLog.getId();
            if(id != null)
            {
               auditMessage += ", creating Workflow Run Log: " + id;
               boolean hadError = BooleanUtils.isTrue(workflowRunLog.getHadError());
               (hadError ? errorWorkflowLogIds : okWorkflowLogIds).add(id);
            }
         }
         else if(workflowOutput.getException() != null)
         {
            unloggedExceptionLine.incrementCount(runBackendStepInput.getRecords().size());
         }

         //////////////////////////////////////////////////////////////
         // audit that the workflow was executed against the records //
         //////////////////////////////////////////////////////////////
         audit(runBackendStepInput, tableName, auditMessage);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static WorkflowTracerInterface getWorkflowTracer(RunBackendStepInput runBackendStepInput)
   {
      Serializable            workflowTracerCodeReference = runBackendStepInput.getValue("workflowTracerCodeReference");
      WorkflowTracerInterface workflowTracer              = null;
      if(workflowTracerCodeReference instanceof QCodeReference codeReference)
      {
         workflowTracer = QCodeLoader.getAdHoc(WorkflowTracerInterface.class, codeReference);
      }
      return workflowTracer;
   }



   /*******************************************************************************
    ** for each input record, add an audit stating that the workflow was executed.
    *******************************************************************************/
   private static void audit(RunBackendStepInput runBackendStepInput, String tableName, String auditMessage)
   {
      try
      {
         if(QContext.getQInstance().getTable(AuditsMetaDataProvider.TABLE_NAME_AUDIT) == null)
         {
            return;
         }

         QTableMetaData table      = QContext.getQInstance().getTable(tableName);
         AuditInput     auditInput = new AuditInput();
         for(QRecord record : runBackendStepInput.getRecords())
         {
            AuditAction.appendToInput(auditInput, table, record, auditMessage);
         }
         new AuditAction().execute(auditInput);
      }
      catch(Exception e)
      {
         LOG.warn("Error recording audits after running record workflow", e, logPair("tableName", tableName), logPair("auditMessage", auditMessage));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput runBackendStepOutput, boolean isForResultScreen)
   {
      ArrayList<ProcessSummaryLineInterface> summary = new ArrayList<>();
      summary.add(okLine);

      if(CollectionUtils.nullSafeHasContents(okWorkflowLogIds))
      {
         summary.add(new ProcessSummaryFilterLink(Status.OK, WorkflowRunLog.TABLE_NAME, new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, okWorkflowLogIds)))
            .withLinkText("Created " + String.format("%,d", okWorkflowLogIds.size()) + " Successful Workflow Run Log" + StringUtils.plural(okWorkflowLogIds)));
      }

      if(CollectionUtils.nullSafeHasContents(errorWorkflowLogIds))
      {
         summary.add(new ProcessSummaryFilterLink(Status.ERROR, WorkflowRunLog.TABLE_NAME, new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.IN, errorWorkflowLogIds)))
            .withLinkText("Created " + String.format("%,d", errorWorkflowLogIds.size()) + " Workflow Run Log" + StringUtils.plural(errorWorkflowLogIds) + " with Errors"));
      }

      unloggedExceptionLine.addSelfToListIfAnyCount(summary);

      return (summary);
   }
}
