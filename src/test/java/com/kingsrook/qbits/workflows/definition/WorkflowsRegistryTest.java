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

package com.kingsrook.qbits.workflows.definition;


import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for WorkflowsRegistry 
 *******************************************************************************/
class WorkflowsRegistryTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      String            stepTypeName      = "myStepType";
      WorkflowsRegistry workflowsRegistry = new WorkflowsRegistry();

      workflowsRegistry.registerWorkflowStepType(new WorkflowStepType()
         .withName(stepTypeName)
         .withDescription("originalDescription")
         .withLabel("originalLabel")
         .withInputFields(List.of(new QFieldMetaData("myField", QFieldType.STRING))));

      /////////////////////
      // set description //
      /////////////////////
      workflowsRegistry.acceptHelpContent(QContext.getQInstance(), new QHelpContent("newDescription"), Map.of("workflowStepType", stepTypeName, "slot", "description"));
      assertEquals("newDescription", workflowsRegistry.getWorkflowStepType(stepTypeName).getDescription());

      ///////////////////////
      // clear description //
      ///////////////////////
      workflowsRegistry.acceptHelpContent(QContext.getQInstance(), null, Map.of("workflowStepType", stepTypeName, "slot", "description"));
      assertNull(workflowsRegistry.getWorkflowStepType(stepTypeName).getDescription());

      ///////////////
      // set label //
      ///////////////
      workflowsRegistry.acceptHelpContent(QContext.getQInstance(), new QHelpContent("newLabel"), Map.of("workflowStepType", stepTypeName, "slot", "label"));
      assertEquals("newLabel", workflowsRegistry.getWorkflowStepType(stepTypeName).getLabel());

      ////////////////////////////////////////////////////////
      // clear label, though we can't, so it says as it was //
      ////////////////////////////////////////////////////////
      workflowsRegistry.acceptHelpContent(QContext.getQInstance(), null, Map.of("workflowStepType", stepTypeName, "slot", "label"));
      assertEquals("newLabel", workflowsRegistry.getWorkflowStepType(stepTypeName).getLabel());

      ////////////////////
      // set field help //
      ////////////////////
      workflowsRegistry.acceptHelpContent(QContext.getQInstance(), new QHelpContent("fieldHelp"), Map.of("workflowStepType", stepTypeName, "field", "myField"));
      assertEquals("fieldHelp", workflowsRegistry.getWorkflowStepType(stepTypeName).getInputFields().get(0).getHelpContents().get(0).getContent());

      //////////////////////
      // clear field help //
      //////////////////////
      workflowsRegistry.acceptHelpContent(QContext.getQInstance(), null, Map.of("workflowStepType", stepTypeName, "field", "myField"));
      assertNull(workflowsRegistry.getWorkflowStepType(stepTypeName).getInputFields().get(0).getHelpContents());
   }

}