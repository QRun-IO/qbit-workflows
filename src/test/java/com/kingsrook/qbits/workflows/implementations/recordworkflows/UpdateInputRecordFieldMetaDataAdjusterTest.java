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