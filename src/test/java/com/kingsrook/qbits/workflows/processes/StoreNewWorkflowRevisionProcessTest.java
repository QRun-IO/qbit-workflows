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

package com.kingsrook.qbits.workflows.processes;


import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.TestWorkflowDefinitions;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/*******************************************************************************
 ** Unit test for StoreNewWorkflowRevisionProcess 
 *******************************************************************************/
class StoreNewWorkflowRevisionProcessTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();
      Integer workflowId = WorkflowsTestDataSource.insertTestWorkflow();

      RunBackendStepInput input = new RunBackendStepInput();

      RunBackendStepOutput output = new RunBackendStepOutput();
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("Workflow id is required");

      input.addValue("workflowId", -999);
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("Workflow not found by id: -999");

      input.addValue("workflowId", workflowId);
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("steps input was not provided");

      input.addValue("steps", "1, 2, 3");
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("Error parsing workflow steps");

      input.addValue("steps", JsonUtils.toJson(List.of(
         WorkflowsTestDataSource.newStep(1, "myStepType", Map.of("foo", 1)),
         WorkflowsTestDataSource.newStep(2, "yourStepType", Map.of("bar", true, "baz", false))
      )));
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("links input was not provided");

      input.addValue("links", "not json");
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("Error parsing workflow links");

      input.addValue("links", JsonUtils.toJson(List.of(WorkflowsTestDataSource.newLink(1, 2))));
      assertThatThrownBy(() -> new StoreNewWorkflowRevisionProcess().run(input, output))
         .hasMessageContaining("2 validation errors occurred before the workflow could be saved:")
         .hasMessageContaining("Error processing Step 1: Unknown workflow step type myStepType")
         .hasMessageContaining("Error processing Step 2: Unknown workflow step type yourStepType");

      input.addValue("steps", JsonUtils.toJson(List.of(
         WorkflowsTestDataSource.newStep(1, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, Map.of("foo", 1)),
         WorkflowsTestDataSource.newStep(2, TestWorkflowDefinitions.BOOLEAN_CONDITIONAL, Map.of("bar", true, "baz", false))
      )));
      new StoreNewWorkflowRevisionProcess().run(input, output);
      assertNotNull(output.getValue("workflowRevisionId"));
      assertEquals(2, output.getValue("versionNo"));

      new StoreNewWorkflowRevisionProcess().run(input, output);
      assertEquals(3, output.getValue("versionNo"));
   }

}