/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
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


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.execution.WorkflowStepOutput;
import com.kingsrook.qbits.workflows.model.Workflow;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.workflows.CompanyHierarchyTablesAndJoinsMetaDataProducer.COMPANY_TABLE;
import static com.kingsrook.qbits.workflows.CompanyHierarchyTablesAndJoinsMetaDataProducer.DEPARTMENT_TABLE;
import static com.kingsrook.qbits.workflows.CompanyHierarchyTablesAndJoinsMetaDataProducer.EMPLOYEE_DETAIL_TABLE;
import static com.kingsrook.qbits.workflows.CompanyHierarchyTablesAndJoinsMetaDataProducer.EMPLOYEE_TABLE;
import static com.kingsrook.qbits.workflows.CompanyHierarchyTablesAndJoinsMetaDataProducer.JOB_HISTORY_TABLE;
import static com.kingsrook.qbits.workflows.CompanyHierarchyTablesAndJoinsMetaDataProducer.JOB_TITLE_TABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;


/*******************************************************************************
 ** Unit test for InputRecordFilterStep 
 *******************************************************************************/
class InputRecordFilterStepTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testWithoutJoins() throws QException
   {
      //////////////////////////////////////////////////
      // empty filter matches any record (even empty) //
      //////////////////////////////////////////////////
      QQueryFilter emptyFilter = new QQueryFilter();
      assertOutput(true, EMPLOYEE_TABLE, emptyFilter, new QRecord());
      assertOutput(true, EMPLOYEE_TABLE, new QQueryFilter(), new QRecord().withValue("id", 1));

      /////////////////////////////////////////////////
      // simple single criteria - matches or doesn't //
      /////////////////////////////////////////////////
      QQueryFilter idEquals1 = new QQueryFilter().withCriteria("id", QCriteriaOperator.EQUALS, 1);
      assertOutput(false, EMPLOYEE_TABLE, idEquals1, new QRecord());
      assertOutput(false, EMPLOYEE_TABLE, idEquals1, Map.of("id", 2));
      assertOutput(true, EMPLOYEE_TABLE, idEquals1, Map.of("id", 1));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testOneToOneJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(EMPLOYEE_DETAIL_TABLE).withRecord(new QRecord().withValue("employeeId", 1).withValue("phone", "555-1234")));

      QQueryFilter phoneContains555 = new QQueryFilter().withCriteria(EMPLOYEE_DETAIL_TABLE + ".phone", QCriteriaOperator.CONTAINS, "555");
      QQueryFilter phoneContains911 = new QQueryFilter().withCriteria(EMPLOYEE_DETAIL_TABLE + ".phone", QCriteriaOperator.CONTAINS, "911");

      ///////////////////////////////////////////////////////////////////////
      // employee with detail record with matching phone number is a match //
      ///////////////////////////////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_TABLE, phoneContains555, Map.of("id", 1));

      ///////////////////////////////////////////////////////////////////////////////
      // employee with detail record with non-matching phone number is NOT a match //
      ///////////////////////////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, phoneContains911, Map.of("id", 1));

      ///////////////////////////////////////////////////
      // employee without detail record is NOT a match //
      ///////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, phoneContains555, Map.of("id", 2));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testManyToOneJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(DEPARTMENT_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 1).withValue("name", "Engineering"),
         new QRecord().withValue("id", 2).withValue("name", "Sales"))));

      QQueryFilter departmentIsEngineering = new QQueryFilter().withCriteria(DEPARTMENT_TABLE + ".name", QCriteriaOperator.EQUALS, "Engineering");

      //////////////////////////////////////////////////
      // employee with matching department is a match //
      //////////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_TABLE, departmentIsEngineering, Map.of("departmentId", 1));

      //////////////////////////////////////////////////////////
      // employee with non-matching department is NOT a match //
      //////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, departmentIsEngineering, Map.of("departmentId", 2));

      //////////////////////////////////////////////////
      // employee without a department is not a match //
      //////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, departmentIsEngineering, Map.of());
      assertOutput(false, EMPLOYEE_TABLE, departmentIsEngineering, MapBuilder.of("departmentId", null));
      assertOutput(false, EMPLOYEE_TABLE, departmentIsEngineering, Map.of("departmentId", 3));
   }



   /*******************************************************************************
    * employee N:1 department N:1 company
    *******************************************************************************/
   @Test
   void testManyToOneThenManyToOneJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(COMPANY_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 1).withValue("name", "ACME"),
         new QRecord().withValue("id", 2).withValue("name", "EvilCorp"))));

      new InsertAction().execute(new InsertInput(DEPARTMENT_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 11).withValue("companyId", 1).withValue("name", "Engineering"),
         new QRecord().withValue("id", 12).withValue("companyId", 1).withValue("name", "Sales"),
         new QRecord().withValue("id", 21).withValue("companyId", 2).withValue("name", "Evil"),
         new QRecord().withValue("id", 31).withValue("companyId", null).withValue("name", "Company-less"))));

      QQueryFilter companyIsACME = new QQueryFilter().withCriteria(COMPANY_TABLE + ".name", QCriteriaOperator.EQUALS, "ACME");

      ///////////////////////////////////////////////
      // employee with matching company is a match //
      ///////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_TABLE, companyIsACME, Map.of("departmentId", 11));

      //////////////////////////////////////////////////////////
      // employee with non-matching department is NOT a match //
      //////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, companyIsACME, Map.of("departmentId", 21));

      ////////////////////////////////////////////////////////////////////
      // employee without a department (thus no company) is not a match //
      ////////////////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, companyIsACME, Map.of());
      assertOutput(false, EMPLOYEE_TABLE, companyIsACME, MapBuilder.of("departmentId", null));
      assertOutput(false, EMPLOYEE_TABLE, companyIsACME, Map.of("departmentId", 3));

      //////////////////////////////////////////////////////////////////////
      // employee without a department that has no company is not a match //
      //////////////////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, companyIsACME, Map.of("departmentId", 31));
   }



   /*******************************************************************************
    ** department 1:N employee 1:1 employeeDetail
    *******************************************************************************/
   @Test
   void testOneToManyThenOneToOneJoin() throws Exception
   {
      //////////////////////////////////////////////////////////////////////////////////////
      // this test queries for Departments, but on fields from EmployeeDetail             //
      // The relationship is Department, one-to-many Employee, one-to-one EmployeeDetail. //
      //////////////////////////////////////////////////////////////////////////////////////
      new InsertAction().execute(new InsertInput(EMPLOYEE_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 1).withValue("departmentId", 11),
         new QRecord().withValue("id", 2).withValue("departmentId", 21),
         new QRecord().withValue("id", 3).withValue("departmentId", 31))));

      new InsertAction().execute(new InsertInput(EMPLOYEE_DETAIL_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 101).withValue("employeeId", 1).withValue("salary", new BigDecimal("105000")),
         new QRecord().withValue("id", 102).withValue("employeeId", 2).withValue("salary", new BigDecimal("95000")))));

      QQueryFilter hasEmployeeOver100K = new QQueryFilter().withCriteria(EMPLOYEE_DETAIL_TABLE + ".salary", QCriteriaOperator.GREATER_THAN, new BigDecimal("100000"));

      ///////////////////////////////////////////////////////////
      // department with a matching employee detail is a match //
      ///////////////////////////////////////////////////////////
      assertOutput(true, DEPARTMENT_TABLE, hasEmployeeOver100K, Map.of("id", 11));

      /////////////////////////////////////////////////////////////////////////////////////
      // department with an employee, who has a detail, but doesn't match is NOT a match //
      /////////////////////////////////////////////////////////////////////////////////////
      assertOutput(false, DEPARTMENT_TABLE, hasEmployeeOver100K, Map.of("id", 21));

      /////////////////////////////////////////////////////////////////////
      // department with no employee, but no employee detail is no match //
      /////////////////////////////////////////////////////////////////////
      assertOutput(false, DEPARTMENT_TABLE, hasEmployeeOver100K, Map.of("id", 31));

      /////////////////////////////////////////////
      // department with no employee is no match //
      /////////////////////////////////////////////
      assertOutput(false, DEPARTMENT_TABLE, hasEmployeeOver100K, Map.of("id", 41));
   }



   /*******************************************************************************
    ** employee 1:N jobHistory
    *******************************************************************************/
   @Test
   void testOneToManyJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(JOB_HISTORY_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 101).withValue("employeeId", 1).withValue("salaryAtTime", new BigDecimal("120000")),
         new QRecord().withValue("id", 102).withValue("employeeId", 2).withValue("salaryAtTime", new BigDecimal("90000")))));

      QQueryFilter hasJobHistoryOver100K = new QQueryFilter().withCriteria(JOB_HISTORY_TABLE + ".salaryAtTime", QCriteriaOperator.GREATER_THAN, new BigDecimal("100000"));

      //////////////////////////////////////////////////
      // employee with a matching job history matches //
      //////////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_TABLE, hasJobHistoryOver100K, Map.of("id", 1));

      ///////////////////////////////////////////////////
      // employee with only non-matching rows does not //
      ///////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, hasJobHistoryOver100K, Map.of("id", 2));

      ////////////////////////////////////
      // employee with no rows does not //
      ////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, hasJobHistoryOver100K, Map.of("id", 3));
   }



   /*******************************************************************************
    ** employee 1:N jobHistory N:1 jobTitle
    *******************************************************************************/
   @Test
   void testOneToManyThenManyToOneJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(JOB_TITLE_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 11).withValue("name", "Engineer"),
         new QRecord().withValue("id", 12).withValue("name", "Intern"))));

      new InsertAction().execute(new InsertInput(JOB_HISTORY_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 101).withValue("employeeId", 1).withValue("jobTitleId", 11),
         new QRecord().withValue("id", 102).withValue("employeeId", 1).withValue("jobTitleId", 12),
         new QRecord().withValue("id", 201).withValue("employeeId", 2).withValue("jobTitleId", 12),
         new QRecord().withValue("id", 301).withValue("employeeId", 3).withValue("jobTitleId", null))));

      QQueryFilter hasHadEngineerTitle = new QQueryFilter().withCriteria(JOB_TITLE_TABLE + ".name", QCriteriaOperator.EQUALS, "Engineer");
      QQueryFilter hasHadInternTitle   = new QQueryFilter().withCriteria(JOB_TITLE_TABLE + ".name", QCriteriaOperator.EQUALS, "Intern");

      ////////////////////////////////////////////////////////////
      // employee with multiple matching titles through history //
      ////////////////////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_TABLE, hasHadEngineerTitle, Map.of("id", 1));
      assertOutput(true, EMPLOYEE_TABLE, hasHadInternTitle, Map.of("id", 1));

      /////////////////////////////////////////////////////////
      // employee with single matching title through history //
      /////////////////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_TABLE, hasHadInternTitle, Map.of("id", 1));

      //////////////////////////////////////////////////////
      // employee with non-matching title through history //
      //////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, hasHadEngineerTitle, Map.of("id", 2));

      ////////////////////////////////////////////////////////
      // employee with job history but missing title target //
      ////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, hasHadEngineerTitle, Map.of("id", 3));

      //////////////////////////////////
      // employee with no job history //
      //////////////////////////////////
      assertOutput(false, EMPLOYEE_TABLE, hasHadEngineerTitle, Map.of("id", 4));
   }



   /*******************************************************************************
    ** company 1:N department 1:N employee
    *******************************************************************************/
   @Test
   void testOneToManyThenOneToManyJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(DEPARTMENT_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 11).withValue("companyId", 1).withValue("name", "Engineering"),
         new QRecord().withValue("id", 12).withValue("companyId", 1).withValue("name", "Support"),
         new QRecord().withValue("id", 21).withValue("companyId", 2).withValue("name", "Support"),
         new QRecord().withValue("id", 31).withValue("companyId", 3).withValue("name", "Safety"),
         new QRecord().withValue("id", 41).withValue("companyId", 4).withValue("name", "Empty Department"))));

      new InsertAction().execute(new InsertInput(EMPLOYEE_TABLE).withRecords(List.of(
         new QRecord().withValue("departmentId", 11).withValue("lastName", "Smith").withValue("firstName", "Ozzie"),
         new QRecord().withValue("departmentId", 12).withValue("lastName", "Smith").withValue("firstName", "Will"),
         new QRecord().withValue("departmentId", 11).withValue("lastName", "Jones"),
         new QRecord().withValue("departmentId", 21).withValue("lastName", "Jones"),
         new QRecord().withValue("departmentId", 31).withValue("lastName", "Simpson"),
         new QRecord().withValue("departmentId", 21).withValue("lastName", "Taylor"))));

      QQueryFilter hasEmployeeNamedSmith   = new QQueryFilter().withCriteria(EMPLOYEE_TABLE + ".lastName", QCriteriaOperator.EQUALS, "Smith");
      QQueryFilter hasEmployeeNamedSimpson = new QQueryFilter().withCriteria(EMPLOYEE_TABLE + ".lastName", QCriteriaOperator.EQUALS, "Simpson");

      /////////////////////////////////////////////////
      // company with at least one matching employee //
      /////////////////////////////////////////////////
      assertOutput(true, COMPANY_TABLE, hasEmployeeNamedSmith, Map.of("id", 1));
      assertOutput(true, COMPANY_TABLE, hasEmployeeNamedSimpson, Map.of("id", 3));

      //////////////////////////////////////////////
      // company with employees but none matching //
      //////////////////////////////////////////////
      assertOutput(false, COMPANY_TABLE, hasEmployeeNamedSmith, Map.of("id", 2));

      ///////////////////////////////////////////////
      // company with departments but no employees //
      ///////////////////////////////////////////////
      assertOutput(false, COMPANY_TABLE, hasEmployeeNamedSmith, Map.of("id", 4));
   }



   /*******************************************************************************
    ** department N:1 manager(employee) 1:N jobHistory
    *******************************************************************************/
   @Test
   void testManyToOneThenOneToManyJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(EMPLOYEE_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 1).withValue("lastName", "Smith"),
         new QRecord().withValue("id", 2).withValue("lastName", "Jones"),
         new QRecord().withValue("id", 3).withValue("lastName", "Milton")
      )));

      new InsertAction().execute(new InsertInput(JOB_HISTORY_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 301).withValue("employeeId", 1).withValue("salaryAtTime", new BigDecimal("130000")),
         new QRecord().withValue("id", 302).withValue("employeeId", 1).withValue("salaryAtTime", new BigDecimal("120000")),
         new QRecord().withValue("id", 303).withValue("employeeId", 2).withValue("salaryAtTime", new BigDecimal("80000")))));

      QQueryFilter managerHasHistoryOver100K = new QQueryFilter().withCriteria(JOB_HISTORY_TABLE + ".salaryAtTime", QCriteriaOperator.GREATER_THAN, new BigDecimal("100000"));

      //////////////////////////////////////////////////////////
      // department whose manager has matching history row(s) //
      //////////////////////////////////////////////////////////
      assertOutput(true, DEPARTMENT_TABLE, managerHasHistoryOver100K, Map.of("managerId", 1));

      /////////////////////////////////////////////////////////
      // department whose manager has only non-matching rows //
      /////////////////////////////////////////////////////////
      assertOutput(false, DEPARTMENT_TABLE, managerHasHistoryOver100K, Map.of("managerId", 2));

      ////////////////////////////////////////////////////////////
      // department with null or unknown manager does not match //
      ////////////////////////////////////////////////////////////
      assertOutput(false, DEPARTMENT_TABLE, managerHasHistoryOver100K, Map.of());
      assertOutput(false, DEPARTMENT_TABLE, managerHasHistoryOver100K, Map.of("managerId", 99));

      //////////////////////////////////////////////////
      // manager who has no job history doesn't match //
      //////////////////////////////////////////////////
      assertOutput(false, DEPARTMENT_TABLE, managerHasHistoryOver100K, Map.of("managerId", 3));
   }



   /*******************************************************************************
    ** employeeDetail 1:1 employee (reverse direction of employee->employeeDetail)
    *******************************************************************************/
   @Test
   void testOneToOneReverseJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(EMPLOYEE_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 100).withValue("lastName", "Smith"),
         new QRecord().withValue("id", 101).withValue("lastName", "Jones"))));

      QQueryFilter employeeLastNameIsSmith = new QQueryFilter().withCriteria(EMPLOYEE_TABLE + ".lastName", QCriteriaOperator.EQUALS, "Smith");

      /////////////////////////////////////////////////////
      // detail row linked to matching employee is match //
      /////////////////////////////////////////////////////
      assertOutput(true, EMPLOYEE_DETAIL_TABLE, employeeLastNameIsSmith, Map.of("employeeId", 100));

      /////////////////////////////////////////////////////////
      // detail row linked to non-matching employee no match //
      /////////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_DETAIL_TABLE, employeeLastNameIsSmith, Map.of("employeeId", 101));

      ////////////////////////////////////////////////////
      // detail row with null/unknown employee no match //
      ////////////////////////////////////////////////////
      assertOutput(false, EMPLOYEE_DETAIL_TABLE, employeeLastNameIsSmith, MapBuilder.of("employeeId", null));
      assertOutput(false, EMPLOYEE_DETAIL_TABLE, employeeLastNameIsSmith, Map.of("employeeId", 999));
   }



   /*******************************************************************************
    ** company 1:N department N:1 manager(employee) 1:1 employeeDetail
    *******************************************************************************/
   @Test
   void testOneToManyThenManyToOneThenOneToOneJoin() throws Exception
   {
      new InsertAction().execute(new InsertInput(EMPLOYEE_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 11).withValue("lastName", "Smith"),
         new QRecord().withValue("id", 21).withValue("lastName", "Jones"),
         new QRecord().withValue("id", 31).withValue("lastName", "Milton")
      )));

      new InsertAction().execute(new InsertInput(DEPARTMENT_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 401).withValue("companyId", 1).withValue("managerId", 11).withValue("name", "Engineering"),
         new QRecord().withValue("id", 402).withValue("companyId", 2).withValue("managerId", 21).withValue("name", "Support"),
         new QRecord().withValue("id", 403).withValue("companyId", 3).withValue("managerId", 31).withValue("name", "Office Space"),
         new QRecord().withValue("id", 404).withValue("companyId", 4).withValue("managerId", null).withValue("name", "No Manager"))));

      new InsertAction().execute(new InsertInput(EMPLOYEE_DETAIL_TABLE).withRecords(List.of(
         new QRecord().withValue("id", 501).withValue("employeeId", 11).withValue("benefitsTier", "PLATINUM"),
         new QRecord().withValue("id", 502).withValue("employeeId", 21).withValue("benefitsTier", "BASIC"))));

      QQueryFilter hasManagerWithPlatinumTier = new QQueryFilter().withCriteria(EMPLOYEE_DETAIL_TABLE + ".benefitsTier", QCriteriaOperator.EQUALS, "PLATINUM");

      ///////////////////////////////////////////////////////////////
      // company with department whose manager has matching detail //
      ///////////////////////////////////////////////////////////////
      assertOutput(true, COMPANY_TABLE, hasManagerWithPlatinumTier, Map.of("id", 1));

      ///////////////////////////////////////////////////////////////
      // company with department whose manager detail is non-match //
      ///////////////////////////////////////////////////////////////
      assertOutput(false, COMPANY_TABLE, hasManagerWithPlatinumTier, Map.of("id", 2));

      ///////////////////////////////////////////////////
      // company with department whose manager is null //
      ///////////////////////////////////////////////////
      assertOutput(false, COMPANY_TABLE, hasManagerWithPlatinumTier, Map.of("id", 4));

      //////////////////////////////////////////////////////////
      // company with department whose manager has no history //
      //////////////////////////////////////////////////////////
      assertOutput(false, COMPANY_TABLE, hasManagerWithPlatinumTier, Map.of("id", 3));

      /////////////////////////////////////////////
      // company with no departments is no match //
      /////////////////////////////////////////////
      assertOutput(false, COMPANY_TABLE, hasManagerWithPlatinumTier, Map.of("id", 5));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   void assertOutput(boolean expectedOutput, String tableName, QQueryFilter filter, Map<String, Serializable> recordValues) throws QException
   {
      QRecord record = new QRecord();
      record.setValues(recordValues);
      assertOutput(expectedOutput, tableName, filter, record);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   void assertOutput(boolean expectedOutput, String tableName, QQueryFilter filter, QRecord record) throws QException
   {
      WorkflowStep              step        = new WorkflowStep();
      Map<String, Serializable> inputValues = Map.of("queryFilterJson", JsonUtils.toJson(filter));

      RecordWorkflowContext context = new RecordWorkflowContext();
      context.setWorkflow(new Workflow().withTableName(tableName));
      context.record.set(record);
      record.setTableName(tableName);

      WorkflowStepOutput stepOutput = new InputRecordFilterStep().execute(step, inputValues, context);
      assertEquals(expectedOutput, stepOutput.outputData());
   }
}
