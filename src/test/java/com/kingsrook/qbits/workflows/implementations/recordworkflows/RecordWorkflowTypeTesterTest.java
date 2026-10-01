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


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.execution.WorkflowTester;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterInput;
import com.kingsrook.qbits.workflows.execution.WorkflowTesterOutput;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertion;
import com.kingsrook.qbits.workflows.model.WorkflowTestRun;
import com.kingsrook.qbits.workflows.model.WorkflowTestScenario;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.backend.core.actions.tables.GetAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.get.GetInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for RecordWorkflowTypeTester 
 *******************************************************************************/
class RecordWorkflowTypeTesterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws Exception
   {
      Workflow workflow   = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinition.WORKFLOW_TYPE, TABLE_NAME_PERSON);
      Integer  workflowId = workflow.getId();

      //////////////////////////
      // if firstName = Darin //
      // - set salary 3.50    //
      // else                 //
      // - set noOfShoes 2    //
      //////////////////////////
      WorkflowsTestDataSource.insertSteps(workflow, List.of(
         WorkflowsTestDataSource.newStep(1, InputRecordFilterStep.NAME, Map.of("queryFilterJson", new QQueryFilter(new QFilterCriteria("firstName", QCriteriaOperator.STARTS_WITH, "Darin")))),
         WorkflowsTestDataSource.newStep(2, UpdateInputRecordFieldStep.NAME, Map.of("fieldName", "salary", "value", new BigDecimal("3.50"))),
         WorkflowsTestDataSource.newStep(3, UpdateInputRecordFieldStep.NAME, Map.of("fieldName", "noOfShoes", "value", 2))
      ));
      WorkflowsTestDataSource.insertLinks(workflow, List.of(
         WorkflowsTestDataSource.newLink(1, 2, true),
         WorkflowsTestDataSource.newLink(1, 3, false)
      ));

      new InsertAction().execute(new InsertInput(WorkflowTestScenario.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowTestScenario()
            .withWorkflowId(workflowId).withName("Darin")
            .withApiName(API_NAME).withApiVersion(V3).withApiJson("""
               {"firstName": "Darin", "salary": 1, "noOfShoes": null, "lastName": ""}""")
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("first name should be Darin").withVariableName(TABLE_NAME_PERSON + ".firstName").withExpectedValue("Darin"),
               new WorkflowTestAssertion().withName("last name should be blank").withVariableName(TABLE_NAME_PERSON + ".lastName").withExpectedValue(null),
               new WorkflowTestAssertion().withName("noOfShoes should be null/empty").withVariableName(TABLE_NAME_PERSON + ".noOfShoes").withExpectedValue(""),
               new WorkflowTestAssertion().withName("salary should be about three fitty").withVariableName(TABLE_NAME_PERSON + ".salary").withExpectedValue("3.50"))),
         new WorkflowTestScenario()
            .withWorkflowId(workflowId).withName("Tim")
            .withApiName(API_NAME).withApiVersion(V3).withApiJson("""
               {"firstName": "Tim", "noOfShoes": null}""")
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("first name should be Tim").withVariableName(TABLE_NAME_PERSON + ".firstName").withExpectedValue("Tim"),
               new WorkflowTestAssertion().withName("salary should be null/empty").withVariableName(TABLE_NAME_PERSON + ".salary").withExpectedValue(""),
               new WorkflowTestAssertion().withName("no of shoes should (of course) be 2").withVariableName(TABLE_NAME_PERSON + ".noOfShoes").withExpectedValue("2")))
      )));

      WorkflowTesterInput input = new WorkflowTesterInput()
         .withWorkflow(new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId)))
         .withWorkflowTestScenarioList(QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))));
      WorkflowTesterOutput output = new WorkflowTesterOutput();
      new WorkflowTester().execute(input, output);

      WorkflowTestRun workflowTestRun = output.getWorkflowTestRun();
      assertEquals(WorkflowTestStatus.PASS.getId(), workflowTestRun.getStatus());
      assertEquals(2, workflowTestRun.getScenarioCount());
      assertEquals(2, workflowTestRun.getScenarioPassCount());
      assertEquals(0, workflowTestRun.getScenarioFailCount());
      assertEquals(7, workflowTestRun.getAssertionCount());
      assertEquals(7, workflowTestRun.getAssertionPassCount());
      assertEquals(0, workflowTestRun.getAssertionFailCount());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFilterUsingExposedJoin() throws QException
   {
      Workflow workflow   = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinition.WORKFLOW_TYPE, TABLE_NAME_PERSON);
      Integer  workflowId = workflow.getId();

      /////////////////////////////////////
      // if favorite shape name = Square //
      // - set salary 4                  //
      // else                            //
      // - set favorite shape = Circle   //
      /////////////////////////////////////
      WorkflowsTestDataSource.insertSteps(workflow, List.of(
         WorkflowsTestDataSource.newStep(1, InputRecordFilterStep.NAME, Map.of("queryFilterJson", new QQueryFilter(new QFilterCriteria(TABLE_NAME_SHAPE + ".name", QCriteriaOperator.EQUALS, "Square")))),
         WorkflowsTestDataSource.newStep(2, UpdateInputRecordFieldStep.NAME, Map.of("fieldName", "salary", "value", new BigDecimal("4.00"))),
         WorkflowsTestDataSource.newStep(3, UpdateInputRecordFieldStep.NAME, Map.of("fieldName", "favoriteShapeId", "value", 2))
      ));
      WorkflowsTestDataSource.insertLinks(workflow, List.of(
         WorkflowsTestDataSource.newLink(1, 2, true),
         WorkflowsTestDataSource.newLink(1, 3, false)
      ));

      new InsertAction().execute(new InsertInput(TABLE_NAME_SHAPE).withRecords(List.of(
         new QRecord().withValue("id", 1).withValue("name", "Square"),
         new QRecord().withValue("id", 2).withValue("name", "Circle")
      )));


      new InsertAction().execute(new InsertInput(WorkflowTestScenario.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowTestScenario()
            .withWorkflowId(workflowId).withName("Darin")
            .withApiName(API_NAME).withApiVersion(V3).withApiJson("""
               {"firstName": "Darin", "favoriteShapeId": 1}""")
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("salary should be 4").withVariableName(TABLE_NAME_PERSON + ".salary").withExpectedValue("4.00"))),
         new WorkflowTestScenario()
            .withWorkflowId(workflowId).withName("Tim")
            .withApiName(API_NAME).withApiVersion(V3).withApiJson("""
               {"firstName": "Tim", "noOfShoes": null}""")
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("favorite shape name should start with c").withQueryFilterJson(JsonUtils.toJson(new QQueryFilter(new QFilterCriteria(TABLE_NAME_SHAPE + ".name", QCriteriaOperator.STARTS_WITH, "C"))))))
      )));

      WorkflowTesterInput input = new WorkflowTesterInput()
         .withWorkflow(new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId)))
         .withWorkflowTestScenarioList(QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))));
      WorkflowTesterOutput output = new WorkflowTesterOutput();
      new WorkflowTester().execute(input, output);

      WorkflowTestRun workflowTestRun = output.getWorkflowTestRun();
      assertEquals(WorkflowTestStatus.PASS.getId(), workflowTestRun.getStatus());
      assertEquals(2, workflowTestRun.getScenarioCount());
      assertEquals(2, workflowTestRun.getScenarioPassCount());
      assertEquals(0, workflowTestRun.getScenarioFailCount());
      assertEquals(2, workflowTestRun.getAssertionCount());
      assertEquals(2, workflowTestRun.getAssertionPassCount());
      assertEquals(0, workflowTestRun.getAssertionFailCount());
   }




   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFilterUsingAssociatedJoinFromInputApiRecord() throws QException
   {
      Workflow workflow   = WorkflowsTestDataSource.insertWorkflowAndInitialRevision(RecordWorkflowsDefinition.WORKFLOW_TYPE, TABLE_NAME_PERSON);
      Integer  workflowId = workflow.getId();

      //////////////////////
      // if has a pet dog //
      // - set salary 4   //
      //////////////////////
      WorkflowsTestDataSource.insertSteps(workflow, List.of(
         WorkflowsTestDataSource.newStep(1, InputRecordFilterStep.NAME, Map.of("queryFilterJson", new QQueryFilter(new QFilterCriteria(TABLE_NAME_PET + ".species", QCriteriaOperator.EQUALS, "dog")))),
         WorkflowsTestDataSource.newStep(2, UpdateInputRecordFieldStep.NAME, Map.of("fieldName", "salary", "value", new BigDecimal("4.00")))
      ));
      WorkflowsTestDataSource.insertLinks(workflow, List.of(
         WorkflowsTestDataSource.newLink(1, 2, true)
      ));

      new InsertAction().execute(new InsertInput(WorkflowTestScenario.TABLE_NAME).withRecordEntities(List.of(
         new WorkflowTestScenario()
            .withWorkflowId(workflowId).withName("Darin")
            .withApiName(API_NAME).withApiVersion(V3).withApiJson("""
               {"firstName": "Darin", "pets": [{"name": "Chester", "species": "dog"}]}""")
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("salary should be 4").withVariableName(TABLE_NAME_PERSON + ".salary").withExpectedValue("4.00"))),
         new WorkflowTestScenario()
            .withWorkflowId(workflowId).withName("Tim")
            .withApiName(API_NAME).withApiVersion(V3).withApiJson("""
               {"firstName": "Tim", "pets": [{"name": "Mae", "species": "cat"}]}""")
            .withAssertions(List.of(
               new WorkflowTestAssertion().withName("should have null salary").withVariableName(TABLE_NAME_PERSON + ".salary").withExpectedValue(null)))
      )));

      WorkflowTesterInput input = new WorkflowTesterInput()
         .withWorkflow(new GetAction().executeForRecord(new GetInput(Workflow.TABLE_NAME).withPrimaryKey(workflowId)))
         .withWorkflowTestScenarioList(QueryAction.execute(WorkflowTestScenario.TABLE_NAME, new QQueryFilter(new QFilterCriteria("workflowId", QCriteriaOperator.EQUALS, workflowId))));
      WorkflowTesterOutput output = new WorkflowTesterOutput();
      new WorkflowTester().execute(input, output);

      WorkflowTestRun workflowTestRun = output.getWorkflowTestRun();
      assertEquals(WorkflowTestStatus.PASS.getId(), workflowTestRun.getStatus());
      assertEquals(2, workflowTestRun.getScenarioCount());
      assertEquals(2, workflowTestRun.getScenarioPassCount());
      assertEquals(0, workflowTestRun.getScenarioFailCount());
      assertEquals(2, workflowTestRun.getAssertionCount());
      assertEquals(2, workflowTestRun.getAssertionPassCount());
      assertEquals(0, workflowTestRun.getAssertionFailCount());
   }

}