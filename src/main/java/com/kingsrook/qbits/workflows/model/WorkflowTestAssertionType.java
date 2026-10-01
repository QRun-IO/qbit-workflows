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

package com.kingsrook.qbits.workflows.model;


import java.util.Objects;
import com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType;
import com.kingsrook.qqq.backend.core.model.metadata.fields.FieldAdornment;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingPossibleValueEnum;
import static com.kingsrook.qqq.backend.core.model.metadata.fields.AdornmentType.ChipValues.iconAndColorValues;


/*******************************************************************************
 ** WorkflowTestStatus - possible value enum
 *******************************************************************************/
@QMetaDataProducingPossibleValueEnum()
public enum WorkflowTestAssertionType implements PossibleValueEnum<Integer>
{
   POSITIVE(1, "Positive"),
   NEGATIVE(2, "Negative");

   private final Integer id;
   private final String  label;

   public static final String NAME = "WorkflowTestAssertionType";

   public static final String DEFAULT_VALUE_STRING = "1";



   /*******************************************************************************
    **
    *******************************************************************************/
   WorkflowTestAssertionType(Integer id, String label)
   {
      this.id = id;
      this.label = label;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public static void customizeFieldWitChipAndWidth(QFieldMetaData field)
   {
      field.withFieldAdornment(new FieldAdornment(AdornmentType.CHIP)
            .withValues(iconAndColorValues(POSITIVE.id, "add", AdornmentType.ChipValues.COLOR_SUCCESS))
            .withValues(iconAndColorValues(NEGATIVE.id, "remove", AdornmentType.ChipValues.COLOR_ERROR)))
         .withFieldAdornment(AdornmentType.Size.SMALL.toAdornment());
   }



   /*******************************************************************************
    ** Get instance by id
    **
    *******************************************************************************/
   public static WorkflowTestAssertionType getById(Integer id)
   {
      if(id == null)
      {
         return (null);
      }

      for(WorkflowTestAssertionType value : WorkflowTestAssertionType.values())
      {
         if(Objects.equals(value.id, id))
         {
            return (value);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Get instance by id
    **
    *******************************************************************************/
   public static WorkflowTestAssertionType getByIdOrDefault(Integer id)
   {
      WorkflowTestAssertionType byId = getById(id);
      return Objects.requireNonNullElse(byId, WorkflowTestAssertionType.POSITIVE);
   }



   /*******************************************************************************
    ** Getter for id
    **
    *******************************************************************************/
   public Integer getId()
   {
      return id;
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Integer getPossibleValueId()
   {
      return (getId());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public String getPossibleValueLabel()
   {
      return (getLabel());
   }
}
