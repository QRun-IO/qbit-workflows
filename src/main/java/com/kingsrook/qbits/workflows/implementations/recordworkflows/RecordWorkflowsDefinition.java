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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.workflows.definition.WorkflowStepTypeCategory;
import com.kingsrook.qbits.workflows.definition.WorkflowType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;


/*******************************************************************************
 * class to define the RecordWorkflows workflow type and register it in the
 * qInstance
 *******************************************************************************/
public class RecordWorkflowsDefinition
{
   public static final String WORKFLOW_TYPE = "RecordWorkflow";



   /***************************************************************************
    **
    ***************************************************************************/
   public void register(QInstance qInstance) throws QException
   {
      registerStepTypes(qInstance);
      registerWorkflow(qInstance);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void registerStepTypes(QInstance qInstance) throws QException
   {
      ////////////////////////////
      // first, general actions //
      ////////////////////////////
      WorkflowsRegistry.of(qInstance).registerWorkflowStepType(new UpdateInputRecordFieldStep());

      //////////////////////
      // then, conditions //
      //////////////////////
      WorkflowsRegistry.of(qInstance).registerWorkflowStepType(new InputRecordFilterStep());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected void registerWorkflow(QInstance qInstance) throws QException
   {
      WorkflowsRegistry.of(qInstance).registerWorkflowType(makeRecordWorkflow());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   static WorkflowType makeRecordWorkflow()
   {
      List<String> actionStepTypes = new ArrayList<>();
      addActionStepTypeNamesToList(actionStepTypes);

      List<String> conditionStepTypes = new ArrayList<>();
      addConditionStepTypeNamesToList(conditionStepTypes);

      return new WorkflowType()
         .withName(WORKFLOW_TYPE)
         .withLabel("Record Workflow")
         .withExecutor(new QCodeReference(RecordWorkflowTypeExecutor.class))
         .withTester(new QCodeReference(RecordWorkflowTypeTester.class))
         .withDescription("Apply custom logic to any record from any table.  Can be automatically ran via Table Triggers, or manually via the Run Workflow action.")
         .withStepTypeCategories(List.of(
            new WorkflowStepTypeCategory()
               .withName("actions")
               .withLabel("Actions")
               .withWorkflowStepTypes(actionStepTypes),
            new WorkflowStepTypeCategory()
               .withName("conditions")
               .withLabel("Conditions")
               .withWorkflowStepTypes(conditionStepTypes)
         ));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void addConditionStepTypeNamesToList(List<String> conditionStepTypes)
   {
      conditionStepTypes.add(InputRecordFilterStep.NAME);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static void addActionStepTypeNamesToList(List<String> actionStepTypes)
   {
      actionStepTypes.add(UpdateInputRecordFieldStep.NAME);
   }

}
