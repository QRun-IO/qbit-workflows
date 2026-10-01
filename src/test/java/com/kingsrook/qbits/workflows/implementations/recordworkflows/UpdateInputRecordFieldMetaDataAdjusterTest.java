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
import java.util.Map;
import java.util.Set;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.frontend.QFrontendFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterOutput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.RunFormAdjusterProcess;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for UpdateInputRecordFieldMetaDataAdjuster 
 *******************************************************************************/
class UpdateInputRecordFieldMetaDataAdjusterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExecute() throws QException
   {
      Map<String, Serializable> allValues = Map.of(
         "workflowValuesJSON", JsonUtils.toJson(Map.of("tableName", TABLE_NAME_PERSON)),
         "workflowRevisionValuesJSON", JsonUtils.toJson(Map.of()),
         "value", 1
      );

      ///////////////////////
      // run for on-change //
      ///////////////////////
      FormAdjusterOutput onChangeOutput = new UpdateInputRecordFieldMetaDataAdjuster().execute(new FormAdjusterInput()
         .withFieldName("fieldName")
         .withNewValue(TABLE_NAME_PERSON + ".firstName")
         .withAllValues(allValues)
         .withEvent(RunFormAdjusterProcess.EVENT_ON_CHANGE));
      assertEquals(Set.of("value"), onChangeOutput.getFieldsToClear());

      assertEquals(1, onChangeOutput.getUpdatedFieldMetaData().size());
      assertThat(onChangeOutput.getUpdatedFieldDisplayValues()).isNullOrEmpty();
      QFrontendFieldMetaData updatedValueField = onChangeOutput.getUpdatedFieldMetaData().get("value");
      assertEquals("Value (First Name)", updatedValueField.getLabel());

      /////////////////////
      // run for on-load //
      /////////////////////
      new InsertAction().execute(new InsertInput(TABLE_NAME_SHAPE).withRecord(new QRecord().withValue("id", 1).withValue("name", "Square")));

      FormAdjusterOutput onLoadOutput = new UpdateInputRecordFieldMetaDataAdjuster().execute(new FormAdjusterInput()
         .withFieldName("fieldName")
         .withNewValue(TABLE_NAME_PERSON + ".favoriteShapeId")
         .withAllValues(allValues)
         .withEvent(RunFormAdjusterProcess.EVENT_ON_LOAD));
      assertThat(onLoadOutput.getFieldsToClear()).isNullOrEmpty();
      assertEquals(Map.of("value", "Square"), onLoadOutput.getUpdatedFieldDisplayValues());
      updatedValueField = onLoadOutput.getUpdatedFieldMetaData().get("value");
      assertEquals("Value (Favorite Shape)", updatedValueField.getLabel());
      assertEquals(TABLE_NAME_SHAPE, updatedValueField.getPossibleValueSourceName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testAdjustPossibleValueSourceFilterCriteriaInputs()
   {
      QFieldMetaData field = new QFieldMetaData();
      UpdateInputRecordFieldMetaDataAdjuster.adjustPossibleValueSourceFilterCriteriaInputs(field.getPossibleValueSourceFilter());
      assertNull(field.getPossibleValueSourceFilter());

      field.setPossibleValueSourceFilter(new QQueryFilter(new QFilterCriteria("id", QCriteriaOperator.EQUALS, 1)));
      UpdateInputRecordFieldMetaDataAdjuster.adjustPossibleValueSourceFilterCriteriaInputs(field.getPossibleValueSourceFilter());
      assertEquals(1, field.getPossibleValueSourceFilter().getCriteria().get(0).getValues().get(0));

      field.setPossibleValueSourceFilter(new QQueryFilter(new QFilterCriteria("someId", QCriteriaOperator.EQUALS, "${input.someId}")));
      UpdateInputRecordFieldMetaDataAdjuster.adjustPossibleValueSourceFilterCriteriaInputs(field.getPossibleValueSourceFilter());
      assertEquals("${input.someId}??${input.workflow.someId}", field.getPossibleValueSourceFilter().getCriteria().get(0).getValues().get(0));

      field.setPossibleValueSourceFilter(new QQueryFilter().withSubFilter(new QQueryFilter(new QFilterCriteria("someId", QCriteriaOperator.EQUALS, "${input.someId}"))));
      UpdateInputRecordFieldMetaDataAdjuster.adjustPossibleValueSourceFilterCriteriaInputs(field.getPossibleValueSourceFilter());
      assertEquals("${input.someId}??${input.workflow.someId}", field.getPossibleValueSourceFilter().getSubFilters().get(0).getCriteria().get(0).getValues().get(0));

   }

}