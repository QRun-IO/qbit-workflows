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

package com.kingsrook.qbits.workflows;


import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.ExposedJoin;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;


/*******************************************************************************
 * MetaData Producer that builds several tables and joins, for a mock HR database.
 *
 * <p>ER Diagram (joins only):</p>
 *
 * <pre>
 *   +-------------+
 *   |   company   |
 *   +-------------+
 *         | ↓ 1:N
 *         |
 *   N:1 ↑ |
 *   +-------------+ → M:1 (manager)
 *   | department  |-------------------+
 *   +-------------+                   |
 *         | ↓ 1:N                     |
 *         |                           |
 *   N:1 ↑ |                     1:M ↑ v
 *   +-------------+    1:1    +----------------+
 *   |  employee   |---------->| employeeDetail |
 *   +-------------+           +----------------+
 *   ↓ 1:N |  ^  \
 *         |  |   \ M:1 (mentor, self-join)
 *         |  +----+
 *   N:1 ↑ |
 *   +-------------+ → M:1     +----------+
 *   | jobHistory  |---------->| jobTitle |
 *   +-------------+     1:M ← +----------+
 * </pre>
 *******************************************************************************/
public class CompanyHierarchyTablesAndJoinsMetaDataProducer implements MetaDataProducerInterface<MetaDataProducerMultiOutput>
{
   public static final String COMPANY_TABLE         = "company";
   public static final String DEPARTMENT_TABLE      = "department";
   public static final String EMPLOYEE_TABLE        = "employee";
   public static final String EMPLOYEE_DETAIL_TABLE = "employeeDetail";
   public static final String JOB_TITLE_TABLE       = "jobTitle";
   public static final String JOB_HISTORY_TABLE     = "jobHistory";

   public static final String COMPANY_JOIN_DEPARTMENT          = "companyDepartments";
   public static final String DEPARTMENT_JOIN_COMPANY          = "departmentCompany";
   public static final String DEPARTMENT_JOIN_EMPLOYEE         = "departmentEmployees";
   public static final String EMPLOYEE_JOIN_DEPARTMENT         = "employeeDepartment";
   public static final String EMPLOYEE_JOIN_EMPLOYEE_AS_MENTOR = "employeeMentor";
   public static final String EMPLOYEE_JOIN_EMPLOYEE_DETAIL    = "employeeEmployeeDetail";
   public static final String EMPLOYEE_DETAIL_JOIN_EMPLOYEE    = "employeeDetailEmployee";
   public static final String EMPLOYEE_JOIN_JOB_HISTORY        = "employeeJobHistories";
   public static final String JOB_HISTORY_JOIN_EMPLOYEE        = "jobHistoryEmployee";
   public static final String JOB_HISTORY_JOIN_JOB_TITLE       = "jobHistoryJobTitle";
   public static final String JOB_TITLE_JOIN_JOB_HISTORY       = "jobTitleJobHistories";
   public static final String DEPARTMENT_JOIN_MANAGER          = "departmentManager";



   /***************************************************************************
    *
    ***************************************************************************/
   @Override
   public MetaDataProducerMultiOutput produce(QInstance qInstance) throws QException
   {
      MetaDataProducerMultiOutput rs = new MetaDataProducerMultiOutput();

      ////////////
      // Tables //
      ////////////

      /////////////////////////////////////
      // Company (root of the hierarchy) //
      /////////////////////////////////////
      rs.add(new QTableMetaData()
         .withName(COMPANY_TABLE)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("industry", QFieldType.STRING))
         .withField(new QFieldMetaData("foundedDate", QFieldType.DATE))
         .withExposedJoin(new ExposedJoin().withJoinTable(EMPLOYEE_TABLE).withJoinPath(List.of(DEPARTMENT_JOIN_COMPANY, DEPARTMENT_JOIN_EMPLOYEE)))
         .withExposedJoin(new ExposedJoin().withJoinTable(EMPLOYEE_DETAIL_TABLE).withJoinPath(List.of(COMPANY_JOIN_DEPARTMENT, DEPARTMENT_JOIN_MANAGER, EMPLOYEE_JOIN_EMPLOYEE_DETAIL))));

      //////////////////////////////////////////////////////////////////////////////////
      // Department (many departments per company; has a manager FK back to employee) //
      //////////////////////////////////////////////////////////////////////////////////
      rs.add(new QTableMetaData()
         .withName(DEPARTMENT_TABLE)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("companyId", QFieldType.INTEGER))   // FK -> company
         .withField(new QFieldMetaData("managerId", QFieldType.INTEGER))   // FK -> employee (self-ref via join)
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("budget", QFieldType.DECIMAL))
         .withExposedJoin(new ExposedJoin().withJoinTable(EMPLOYEE_DETAIL_TABLE).withJoinPath(List.of(DEPARTMENT_JOIN_EMPLOYEE, EMPLOYEE_JOIN_EMPLOYEE_DETAIL)))
         .withExposedJoin(new ExposedJoin().withJoinTable(JOB_HISTORY_TABLE).withJoinPath(List.of(DEPARTMENT_JOIN_MANAGER, EMPLOYEE_JOIN_JOB_HISTORY)))
      );

      /////////////////////////////////////////////////////////////////////
      // Employee (many employees per department; 1:1 to EmployeeDetail) //
      /////////////////////////////////////////////////////////////////////
      rs.add(new QTableMetaData()
         .withName(EMPLOYEE_TABLE)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("departmentId", QFieldType.INTEGER))     // FK -> department
         .withField(new QFieldMetaData("mentorEmployeeId", QFieldType.INTEGER)) // FK -> employee (self-join)
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withField(new QFieldMetaData("hireDate", QFieldType.DATE))
         .withField(new QFieldMetaData("isActive", QFieldType.BOOLEAN))
         .withExposedJoin(new ExposedJoin().withJoinTable(COMPANY_TABLE).withJoinPath(List.of(EMPLOYEE_JOIN_DEPARTMENT, DEPARTMENT_JOIN_COMPANY)))
         .withExposedJoin(new ExposedJoin().withJoinTable(JOB_TITLE_TABLE).withJoinPath(List.of(EMPLOYEE_JOIN_JOB_HISTORY, JOB_HISTORY_JOIN_JOB_TITLE)))
      );

      //////////////////////////////////////////////////////////////////
      // EmployeeDetail (1:1 with employee - extended/sensitive info) //
      //////////////////////////////////////////////////////////////////
      rs.add(new QTableMetaData()
         .withName(EMPLOYEE_DETAIL_TABLE)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("employeeId", QFieldType.INTEGER))   // FK -> employee (1:1)
         .withField(new QFieldMetaData("phone", QFieldType.STRING))
         .withField(new QFieldMetaData("emergencyContact", QFieldType.STRING))
         .withField(new QFieldMetaData("salary", QFieldType.DECIMAL))
         .withField(new QFieldMetaData("benefitsTier", QFieldType.STRING)));

      ////////////////////////////////////////////////////////////////////
      // JobTitle (lookup table - many employees can share a job title) //
      ////////////////////////////////////////////////////////////////////
      rs.add(new QTableMetaData()
         .withName(JOB_TITLE_TABLE)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("name", QFieldType.STRING))
         .withField(new QFieldMetaData("grade", QFieldType.STRING))
         .withField(new QFieldMetaData("isExempt", QFieldType.BOOLEAN)));

      ///////////////////////////////////////////////////////////////////////////////
      // JobHistory (many job history rows per employee; each row n:1 to jobTitle) //
      ///////////////////////////////////////////////////////////////////////////////
      rs.add(new QTableMetaData()
         .withName(JOB_HISTORY_TABLE)
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("employeeId", QFieldType.INTEGER))   // FK -> employee
         .withField(new QFieldMetaData("jobTitleId", QFieldType.INTEGER))   // FK -> jobTitle
         .withField(new QFieldMetaData("startDate", QFieldType.DATE))
         .withField(new QFieldMetaData("endDate", QFieldType.DATE))
         .withField(new QFieldMetaData("salaryAtTime", QFieldType.DECIMAL)));

      /////////////////////////////////////////
      // set common attributes on all tables //
      /////////////////////////////////////////
      rs.getEach(QTableMetaData.class).forEach(t ->
      {
         t.setPrimaryKeyField("id");
         t.setBackendName(BaseTest.MEMORY_BACKEND_NAME);
      });

      ///////////
      // Joins //
      ///////////

      /////////////////////////////////////////////////////////////////
      // company 1:n department  (company.id = department.companyId) //
      /////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(COMPANY_JOIN_DEPARTMENT)
         .withLeftTable(COMPANY_TABLE)
         .withRightTable(DEPARTMENT_TABLE)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "companyId"))
         .withOrderBy(new QFilterOrderBy("name")));

      ///////////////////////////////////////////////////////////////////////////////////////////
      // department n:1 company  (flip of above - useful when traversing from department side) //
      ///////////////////////////////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(DEPARTMENT_JOIN_COMPANY)
         .withLeftTable(DEPARTMENT_TABLE)
         .withRightTable(COMPANY_TABLE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("companyId", "id")));

      //////////////////////////////////////////////////////////////////////
      // department 1:n employee  (department.id = employee.departmentId) //
      //////////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(DEPARTMENT_JOIN_EMPLOYEE)
         .withLeftTable(DEPARTMENT_TABLE)
         .withRightTable(EMPLOYEE_TABLE)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "departmentId"))
         .withOrderBy(new QFilterOrderBy("lastName"))
         .withOrderBy(new QFilterOrderBy("firstName")));

      //////////////////////////////////////////////
      // employee n:1 department  (flip of above) //
      //////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(EMPLOYEE_JOIN_DEPARTMENT)
         .withLeftTable(EMPLOYEE_TABLE)
         .withRightTable(DEPARTMENT_TABLE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("departmentId", "id")));

      /////////////////////////////////////////////////
      // employee n:1 employee (mentor relationship) //
      /////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(EMPLOYEE_JOIN_EMPLOYEE_AS_MENTOR)
         .withLeftTable(EMPLOYEE_TABLE)  // the mentee
         .withRightTable(EMPLOYEE_TABLE) // the mentor
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("mentorEmployeeId", "id")));

      ////////////////////////////////////////////////////////////////////////////
      // employee 1:1 employeeDetail  (employee.id = employeeDetail.employeeId) //
      ////////////////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(EMPLOYEE_JOIN_EMPLOYEE_DETAIL)
         .withLeftTable(EMPLOYEE_TABLE)
         .withRightTable(EMPLOYEE_DETAIL_TABLE)
         .withType(JoinType.ONE_TO_ONE)
         .withJoinOn(new JoinOn("id", "employeeId")));

      //////////////////////////////////////////////////
      // employeeDetail 1:1 employee  (flip of above) //
      //////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(EMPLOYEE_DETAIL_JOIN_EMPLOYEE)
         .withLeftTable(EMPLOYEE_DETAIL_TABLE)
         .withRightTable(EMPLOYEE_TABLE)
         .withType(JoinType.ONE_TO_ONE)
         .withJoinOn(new JoinOn("employeeId", "id")));

      ////////////////////////////////////////////////////////////////////
      // employee 1:n jobHistory  (employee.id = jobHistory.employeeId) //
      ////////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(EMPLOYEE_JOIN_JOB_HISTORY)
         .withLeftTable(EMPLOYEE_TABLE)
         .withRightTable(JOB_HISTORY_TABLE)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "employeeId"))
         .withOrderBy(new QFilterOrderBy("startDate", false)));  // most recent first

      //////////////////////////////////////////////
      // jobHistory n:1 employee  (flip of above) //
      //////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(JOB_HISTORY_JOIN_EMPLOYEE)
         .withLeftTable(JOB_HISTORY_TABLE)
         .withRightTable(EMPLOYEE_TABLE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("employeeId", "id")));

      ////////////////////////////////////////////////////////////////////
      // jobHistory n:1 jobTitle  (jobHistory.jobTitleId = jobTitle.id) //
      ////////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(JOB_HISTORY_JOIN_JOB_TITLE)
         .withLeftTable(JOB_HISTORY_TABLE)
         .withRightTable(JOB_TITLE_TABLE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("jobTitleId", "id")));

      //////////////////////////////////////////////
      // jobTitle 1:n jobHistory  (flip of above) //
      //////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(JOB_TITLE_JOIN_JOB_HISTORY)
         .withLeftTable(JOB_TITLE_TABLE)
         .withRightTable(JOB_HISTORY_TABLE)
         .withType(JoinType.ONE_TO_MANY)
         .withJoinOn(new JoinOn("id", "jobTitleId"))
         .withOrderBy(new QFilterOrderBy("startDate", false)));

      /////////////////////////////////////////////////////////////////////////////
      // department n:1 employee (manager)  (department.managerId = employee.id) //
      /////////////////////////////////////////////////////////////////////////////
      rs.add(new QJoinMetaData()
         .withName(DEPARTMENT_JOIN_MANAGER)
         .withLeftTable(DEPARTMENT_TABLE)
         .withRightTable(EMPLOYEE_TABLE)
         .withType(JoinType.MANY_TO_ONE)
         .withJoinOn(new JoinOn("managerId", "id")));

      return (rs);
   }

}
