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

package com.kingsrook.qbits.workflows.model;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.ValueTooLongBehavior;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.producers.MetaDataCustomizerInterface;
import com.kingsrook.qqq.backend.core.model.metadata.producers.annotations.QMetaDataProducingEntity;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.SectionFactory;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;


/*******************************************************************************
 ** QRecord Entity for WorkflowLink table
 *******************************************************************************/
@QMetaDataProducingEntity(
   producePossibleValueSource = true,
   produceTableMetaData = true,
   tableMetaDataCustomizer = WorkflowLink.TableMetaDataCustomizer.class
)
public class WorkflowLink extends QRecordEntity
{
   public static final String TABLE_NAME = "workflowLink";



   /***************************************************************************
    **
    ***************************************************************************/
   public static class TableMetaDataCustomizer implements MetaDataCustomizerInterface<QTableMetaData>
   {

      /***************************************************************************
       **
       ***************************************************************************/
      @Override
      public QTableMetaData customizeMetaData(QInstance qInstance, QTableMetaData table) throws QException
      {
         table
            .withUniqueKey(new UniqueKey("workflowRevisionId", "fromStepNo", "conditionValue"))
            .withIcon(new QIcon().withName("link"))
            .withRecordLabelFormat("%s %s")
            .withRecordLabelFields("workflowRevisionId", "fromStepNo", "toStepNo")
            .withSection(SectionFactory.defaultT1("id", "workflowRevisionId", "fromStepNo", "toStepNo"))
            .withSection(SectionFactory.defaultT2("conditionValue"));

         return (table);
      }
   }


   @QField(isEditable = false, isPrimaryKey = true)
   private Integer id;

   @QField(isRequired = true, possibleValueSourceName = WorkflowRevision.TABLE_NAME)
   private Integer workflowRevisionId;

   @QField(isRequired = true)
   private Integer fromStepNo;

   @QField()
   private Integer toStepNo;

   @QField(maxLength = 20, valueTooLongBehavior = ValueTooLongBehavior.ERROR)
   private String conditionValue;


   /*******************************************************************************
    ** Default constructor
    *******************************************************************************/
   public WorkflowLink()
   {
   }



   /*******************************************************************************
    ** Constructor that takes a QRecord
    *******************************************************************************/
   public WorkflowLink(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id
    *******************************************************************************/
   public WorkflowLink withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for workflowRevisionId
    *******************************************************************************/
   public Integer getWorkflowRevisionId()
   {
      return (this.workflowRevisionId);
   }



   /*******************************************************************************
    ** Setter for workflowRevisionId
    *******************************************************************************/
   public void setWorkflowRevisionId(Integer workflowRevisionId)
   {
      this.workflowRevisionId = workflowRevisionId;
   }



   /*******************************************************************************
    ** Fluent setter for workflowRevisionId
    *******************************************************************************/
   public WorkflowLink withWorkflowRevisionId(Integer workflowRevisionId)
   {
      this.workflowRevisionId = workflowRevisionId;
      return (this);
   }





   /*******************************************************************************
    ** Getter for toStepNo
    *******************************************************************************/
   public Integer getToStepNo()
   {
      return (this.toStepNo);
   }



   /*******************************************************************************
    ** Setter for toStepNo
    *******************************************************************************/
   public void setToStepNo(Integer toStepNo)
   {
      this.toStepNo = toStepNo;
   }



   /*******************************************************************************
    ** Fluent setter for toStepNo
    *******************************************************************************/
   public WorkflowLink withToStepNo(Integer toStepNo)
   {
      this.toStepNo = toStepNo;
      return (this);
   }



   /*******************************************************************************
    ** Getter for conditionValue
    *******************************************************************************/
   public String getConditionValue()
   {
      return (this.conditionValue);
   }



   /*******************************************************************************
    ** Setter for conditionValue
    *******************************************************************************/
   public void setConditionValue(String conditionValue)
   {
      this.conditionValue = conditionValue;
   }



   /*******************************************************************************
    ** Fluent setter for conditionValue
    *******************************************************************************/
   public WorkflowLink withConditionValue(String conditionValue)
   {
      this.conditionValue = conditionValue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fromStepNo
    *******************************************************************************/
   public Integer getFromStepNo()
   {
      return (this.fromStepNo);
   }



   /*******************************************************************************
    ** Setter for fromStepNo
    *******************************************************************************/
   public void setFromStepNo(Integer fromStepNo)
   {
      this.fromStepNo = fromStepNo;
   }



   /*******************************************************************************
    ** Fluent setter for fromStepNo
    *******************************************************************************/
   public WorkflowLink withFromStepNo(Integer fromStepNo)
   {
      this.fromStepNo = fromStepNo;
      return (this);
   }


}
