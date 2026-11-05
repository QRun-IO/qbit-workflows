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

package com.kingsrook.qbits.workflows.metadata;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowsDefinition;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qqq.backend.core.actions.processes.RunProcessAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.RunFormAdjusterProcess;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Runs the workflows form adjusters through the current Material Dashboard
 ** form-adjuster process, the same way the UI API's table form-adjuster route
 ** does (path params + multipart-style form params) - not by calling them directly.
 *******************************************************************************/
class WorkflowTestAssertionFormAdjusterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testVariableNameAdjusterThroughMaterialDashboardProcess() throws Exception
   {

      Workflow workflow = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinition.WORKFLOW_TYPE, TABLE_NAME_PERSON);
      Integer scenarioId = new InsertAction().execute(new InsertInput(WorkflowTestScenario.TABLE_NAME).withRecordEntity(
         new WorkflowTestScenario().withWorkflowId(workflow.getId()).withName("s").withApiName(API_NAME).withApiVersion(V3).withApiJson("{}"))).getRecords().get(0).getValueInteger("id");

      Map<String, Object> output = runAdjuster("WorkflowTestAssertion.variableName", RunFormAdjusterProcess.EVENT_ON_CHANGE,
         TABLE_NAME_PERSON + ".firstName", Map.of("workflowTestScenarioId", scenarioId));

      @SuppressWarnings("unchecked")
      Map<String, Object> expectedValueField = (Map<String, Object>) ((Map<String, Object>) output.get("updatedFieldMetaData")).get("expectedValue");
      assertNotNull(expectedValueField);
      assertTrue(String.valueOf(expectedValueField.get("label")).startsWith("Expected Value ("), "label: " + expectedValueField.get("label"));
      assertEquals(List.of("expectedValue"), output.get("fieldsToClear"));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private Map<String, Object> runAdjuster(String identifier, String event, String newValue, Map<String, Object> allValues) throws Exception
   {
      Map<String, List<String>> formParams = new HashMap<>();
      formParams.put("event", List.of(event));
      formParams.put("fieldName", List.of("variableName"));
      formParams.put("newValue", List.of(newValue));
      formParams.put("allValues", List.of(JsonUtils.toJson(allValues)));

      RunProcessInput input = new RunProcessInput();
      input.setProcessName(RunFormAdjusterProcess.NAME);
      input.setFrontendStepBehavior(RunProcessInput.FrontendStepBehavior.SKIP);
      input.addValue("pathParams", new HashMap<>(Map.of("identifier", identifier, "event", event)));
      input.addValue("formParams", (Serializable) formParams);

      RunProcessOutput processOutput = new RunProcessAction().execute(input);
      if(processOutput.getException().isPresent())
      {
         throw (processOutput.getException().get());
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> result = JsonUtils.toObject(processOutput.getValueString("responseString"), Map.class);
      return (result);
   }

}
