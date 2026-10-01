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
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.model.WorkflowRevision;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for UpdateInputRecordFieldStep 
 *******************************************************************************/
class UpdateInputRecordFieldStepTest extends BaseTest
{
   private static WorkflowRevision workflowRevisionV1 = new WorkflowRevision().withApiName(API_NAME).withApiVersion(V1);
   private static WorkflowRevision workflowRevisionV2 = new WorkflowRevision().withApiName(API_NAME).withApiVersion(V2);
   private static WorkflowRevision workflowRevisionV3 = new WorkflowRevision().withApiName(API_NAME).withApiVersion(V3);
   private static WorkflowRevision workflowRevisionWithoutApi = new WorkflowRevision();



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetApiField() throws QException
   {
      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".firstName", TABLE_NAME_PERSON, workflowRevisionV1))
         .isPresent().get().hasFieldOrPropertyWithValue("name", "firstName");

      assertThat(UpdateInputRecordFieldStep.getApiField("firstName", TABLE_NAME_PERSON, workflowRevisionV1))
         .isPresent().get().hasFieldOrPropertyWithValue("name", "firstName");

      assertThat(UpdateInputRecordFieldStep.getApiField("noSuchField", TABLE_NAME_PERSON, workflowRevisionV1))
         .isEmpty();

      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".noSuchField", TABLE_NAME_PERSON, workflowRevisionV1))
         .isEmpty();

      /////////////////////////////////////
      // this field is excluded from api //
      /////////////////////////////////////
      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".debt", TABLE_NAME_PERSON, workflowRevisionV1))
         .isEmpty();

      ///////////////////////////////
      // this field is added in V3 //
      ///////////////////////////////
      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".salary", TABLE_NAME_PERSON, workflowRevisionV1))
         .isEmpty();

      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".salary", TABLE_NAME_PERSON, workflowRevisionV3))
         .isPresent().get().hasFieldOrPropertyWithValue("name", "salary");

      //////////////////////////////////////////
      // confirm shoeCount/noOfShoes renaming //
      //////////////////////////////////////////
      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".shoeCount", TABLE_NAME_PERSON, workflowRevisionV1))
         .isPresent().get().hasFieldOrPropertyWithValue("name", "shoeCount");

      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".shoeCount", TABLE_NAME_PERSON, workflowRevisionV2))
         .isEmpty();

      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".noOfShoes", TABLE_NAME_PERSON, workflowRevisionV1))
         .isEmpty();

      assertThat(UpdateInputRecordFieldStep.getApiField(TABLE_NAME_PERSON + ".noOfShoes", TABLE_NAME_PERSON, workflowRevisionV2))
         .isPresent().get().hasFieldOrPropertyWithValue("name", "noOfShoes");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetStepSummary() throws QException
   {
      assertThat(UpdateInputRecordFieldStep.getStepSummary(Map.of("fieldName", TABLE_NAME_PERSON + ".firstName", "value", "John"), TABLE_NAME_PERSON, false))
         .isEqualTo("First Name will be set to 'John'");

      assertThat(UpdateInputRecordFieldStep.getStepSummary(Map.of("fieldName", TABLE_NAME_PERSON + ".firstName", "value", "John"), TABLE_NAME_PERSON, true))
         .isEqualTo("First Name was set to 'John'");

      assertThat(UpdateInputRecordFieldStep.getStepSummary(Map.of("fieldName", TABLE_NAME_PERSON + ".firstName", "value", ""), TABLE_NAME_PERSON, false))
         .isEqualTo("First Name will be cleared out");

      assertThat(UpdateInputRecordFieldStep.getStepSummary(Map.of("fieldName", TABLE_NAME_PERSON + ".firstName"), TABLE_NAME_PERSON, true))
         .isEqualTo("First Name was cleared out");

      new InsertAction().execute(new InsertInput(TABLE_NAME_SHAPE).withRecord(new QRecord().withValue("id", 1).withValue("name", "Square")));

      assertThat(UpdateInputRecordFieldStep.getStepSummary(Map.of("fieldName", TABLE_NAME_PERSON + ".favoriteShapeId", "value", "1"), TABLE_NAME_PERSON, false))
         .isEqualTo("Favorite Shape will be set to 'Square'");

      assertThat(UpdateInputRecordFieldStep.getStepSummary(Map.of("fieldName", TABLE_NAME_PERSON + ".favoriteShapeId", "value", "1"), TABLE_NAME_PERSON, true))
         .isEqualTo("Favorite Shape was set to 'Square'");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValidate() throws QException
   {
      assertThat(runValidate(Map.of("fieldName", TABLE_NAME_PERSON + ".firstName"), workflowRevisionV1)).isEmpty();

      ////////////////////////////////////////////////////////////////////////////////////
      // debt field - found if not using api, else not found as it is excluded from api //
      ////////////////////////////////////////////////////////////////////////////////////
      assertThat(runValidate(Map.of("fieldName", TABLE_NAME_PERSON + ".debt"), workflowRevisionWithoutApi)).isEmpty();
      assertThat(runValidate(Map.of("fieldName", TABLE_NAME_PERSON + ".debt"), workflowRevisionV1)).hasSize(1)
         .element(0).asString()
         .contains("Could not find field 'person.debt' in table '" + TABLE_NAME_PERSON + "' for API '" + API_NAME + "' version 'v1'");
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private List<String> runValidate(Map<String, Serializable> inputValues, WorkflowRevision workflowRevision) throws QException
   {
      List<String> errors = new ArrayList<>();
      UpdateInputRecordFieldStep.validate(TABLE_NAME_PERSON, inputValues, workflowRevision == null ? null : workflowRevision.toQRecord(), errors);
      return (errors);
   }

}

