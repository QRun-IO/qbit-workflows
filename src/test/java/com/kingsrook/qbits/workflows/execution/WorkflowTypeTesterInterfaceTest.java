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

package com.kingsrook.qbits.workflows.execution;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.implementations.recordworkflows.RecordWorkflowContext;
import com.kingsrook.qbits.workflows.model.WorkflowTestAssertionType;
import com.kingsrook.qbits.workflows.model.WorkflowTestOutput;
import com.kingsrook.qbits.workflows.model.WorkflowTestStatus;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for WorkflowTypeTesterInterface 
 *******************************************************************************/
class WorkflowTypeTesterInterfaceTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testScalarValues()
   {
      ////////////////////////////////
      // positive asserts that pass //
      ////////////////////////////////
      assertOutputs(WorkflowTestStatus.PASS, "x: was [foo] as expected.", "foo", "foo", WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was [1] as expected.", "1", 1, WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was [3.50] as expected.", "3.50", new BigDecimal("3.50"), WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was [true] as expected.", "true", true, WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was blank as expected.", "", "", WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was blank as expected.", "", null, WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was blank as expected.", null, "", WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was blank as expected.", null, null, WorkflowTestAssertionType.POSITIVE);

      ////////////////////////////////
      // positive asserts that fail //
      ////////////////////////////////
      assertOutputs(WorkflowTestStatus.FAIL, "x: was [bar] but expected [foo].", "foo", "bar", WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.FAIL, "x: was [bar] but expected [].", "", "bar", WorkflowTestAssertionType.POSITIVE);
      assertOutputs(WorkflowTestStatus.FAIL, "x: was [bar] but expected [null].", null, "bar", WorkflowTestAssertionType.POSITIVE);

      ////////////////////////////////
      // negative asserts that pass //
      ////////////////////////////////
      assertOutputs(WorkflowTestStatus.PASS, "x: was [bar] not [foo] as expected.", "foo", "bar", WorkflowTestAssertionType.NEGATIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was [foo] not [] as expected.", "", "foo", WorkflowTestAssertionType.NEGATIVE);
      assertOutputs(WorkflowTestStatus.PASS, "x: was [] not [foo] as expected.", "foo", "", WorkflowTestAssertionType.NEGATIVE);

      ////////////////////////////////
      // negative asserts that fail //
      ////////////////////////////////
      assertOutputs(WorkflowTestStatus.FAIL, "x: was [foo] but was not expected to be.", "foo", "foo", WorkflowTestAssertionType.NEGATIVE);
      assertOutputs(WorkflowTestStatus.FAIL, "x: was blank but was not expected to be.", "", "", WorkflowTestAssertionType.NEGATIVE);
      assertOutputs(WorkflowTestStatus.FAIL, "x: was blank but was not expected to be.", null, "", WorkflowTestAssertionType.NEGATIVE);
      assertOutputs(WorkflowTestStatus.FAIL, "x: was blank but was not expected to be.", null, null, WorkflowTestAssertionType.NEGATIVE);

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testListValues()
   {
      ////////////////////////////////
      // positive asserts that pass //
      ////////////////////////////////
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [foo] as expected.", "foo", List.of("foo"), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [1] as expected.", "1", List.of(1), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [3.50] as expected.", "3.50", List.of(new BigDecimal("3.50")), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [true] as expected.", "true", List.of(true), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [] as expected.", "", List.of(""), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [] as expected.", "", ListBuilder.of(null), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [null] as expected.", null, List.of(""), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: found [null] as expected.", null, ListBuilder.of(null), WorkflowTestAssertionType.POSITIVE);

      ////////////////////////////////
      // positive asserts that fail //
      ////////////////////////////////
      assertListOutputs(WorkflowTestStatus.FAIL, "x: did not find expected value [foo].", "foo", List.of("bar"), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.FAIL, "x: did not find expected value [].", "", List.of("bar"), WorkflowTestAssertionType.POSITIVE);
      assertListOutputs(WorkflowTestStatus.FAIL, "x: did not find expected value [null].", null, List.of("bar"), WorkflowTestAssertionType.POSITIVE);

      ////////////////////////////////
      // negative asserts that pass //
      ////////////////////////////////
      assertListOutputs(WorkflowTestStatus.PASS, "x: as expected, did not find [foo].", "foo", List.of("bar"), WorkflowTestAssertionType.NEGATIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: as expected, did not find [].", "", List.of("foo"), WorkflowTestAssertionType.NEGATIVE);
      assertListOutputs(WorkflowTestStatus.PASS, "x: as expected, did not find [foo].", "foo", List.of(""), WorkflowTestAssertionType.NEGATIVE);

      ////////////////////////////////
      // negative asserts that fail //
      ////////////////////////////////
      assertListOutputs(WorkflowTestStatus.FAIL, "x: found [foo] but was not expected to.", "foo", List.of("foo"), WorkflowTestAssertionType.NEGATIVE);
      assertListOutputs(WorkflowTestStatus.FAIL, "x: found [] but was not expected to.", "", List.of(""), WorkflowTestAssertionType.NEGATIVE);
      assertListOutputs(WorkflowTestStatus.FAIL, "x: found [null] but was not expected to.", null, List.of(""), WorkflowTestAssertionType.NEGATIVE);
      assertListOutputs(WorkflowTestStatus.FAIL, "x: found [null] but was not expected to.", null, ListBuilder.of(null), WorkflowTestAssertionType.NEGATIVE);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testEvaluateFilter() throws Exception
   {
      QQueryFilter idEquals1 = new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.EQUALS, 1));
      QRecord      id1       = new QRecord().withValue("id", 1);
      QRecord      id2       = new QRecord().withValue("id", 2);

      assertFilterOutputs(WorkflowTestStatus.PASS, "Filter matched.", idEquals1, id1, WorkflowTestAssertionType.POSITIVE);
      assertFilterOutputs(WorkflowTestStatus.FAIL, "Filter did not match, but was expected to.", idEquals1, id2, WorkflowTestAssertionType.POSITIVE);

      assertFilterOutputs(WorkflowTestStatus.FAIL, "Filter matched, but was expected not to.", idEquals1, id1, WorkflowTestAssertionType.NEGATIVE);
      assertFilterOutputs(WorkflowTestStatus.PASS, "As expected, filter did not match.", idEquals1, id2, WorkflowTestAssertionType.NEGATIVE);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void assertFilterOutputs(WorkflowTestStatus expectedStatus, String expectedMessage, QQueryFilter filter, QRecord record, WorkflowTestAssertionType assertionType) throws Exception
   {
      WorkflowTypeTesterInterface tester = new WorkflowTypeTesterInterface() {};

      WorkflowOutput workflowOutput = new WorkflowOutput()
         .withContext(new RecordWorkflowContext()
            .withValues(Map.of("record", record)));

      WorkflowTestOutput output = new WorkflowTestOutput();
      tester.evaluateFilter(output, workflowOutput, new QRecord(), JsonUtils.toJson(filter), assertionType);
      assertEquals(expectedStatus.getId(), output.getStatus(), "Expected status: " + expectedStatus);
      assertEquals(expectedMessage, output.getMessage());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void assertOutputs(WorkflowTestStatus expectedStatus, String expectedMessage, String expectedValue, Serializable actualValue, WorkflowTestAssertionType assertionType)
   {
      WorkflowTypeTesterInterface tester = new WorkflowTypeTesterInterface() {};
      WorkflowTestOutput          output = new WorkflowTestOutput();
      tester.evaluateExpectedValue(output, "x", actualValue, expectedValue, assertionType);
      assertEquals(expectedStatus.getId(), output.getStatus(), "Expected status: " + expectedStatus);
      assertEquals(expectedMessage, output.getMessage());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void assertListOutputs(WorkflowTestStatus expectedStatus, String expectedMessage, String expectedValue, List<?> actualValue, WorkflowTestAssertionType assertionType)
   {
      assertOutputs(expectedStatus, expectedMessage, expectedValue, new ArrayList<>(actualValue), assertionType);
   }
}