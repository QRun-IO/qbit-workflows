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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.gson.reflect.TypeToken;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryJoin;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 * For workflows that are centered around a single record and may work with
 * records joined to that record, an instance of this class can be put into
 * the workflow's context.  It can then be used, specifically, by the
 * {@link InputRecordFilterStep} to build up a cross-product used to do in-
 * memory filter operation.
 *******************************************************************************/
public class RecordWorkflowContextJoinedRecordHelper implements Serializable
{
   private final String  tableName;
   private final QRecord record;

   private Map<String, List<QRecord>> joinRecords = new HashMap<>();



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public RecordWorkflowContextJoinedRecordHelper(String tableName, QRecord record)
   {
      this.tableName = tableName;
      this.record = record;
   }



   /***************************************************************************
    * if the context is collecting records that will be inserted when the
    * workflow is done, and those need to be included, e.g., for in-memory
    * filters - then override this method (e.g., via an anonymous inner class
    * in the specific workflow context class in question), and return appropriate
    * pending-insertion records here.
    ***************************************************************************/
   public List<QRecord> getRecordsToInsert(String tableName)
   {
      return Collections.emptyList();
   }



   /***************************************************************************
    * if the context is collecting primary keys that will be deleted when the
    * workflow is done, and those records need to be excluded, e.g., for in-memory
    * filters - then override this method (e.g., via an anonymous inner class
    * in the specific workflow context class in question), and return appropriate
    * pending-deletion keys here.
    ***************************************************************************/
   public Set<Serializable> getPrimaryKeysToDelete(String tableName)
   {
      return Collections.emptySet();
   }



   /***************************************************************************
    * Clear the join records map.  Specifically useful if the context is being
    * used both during and after workflow execution (e.g. before and after
    * records have been stored).
    ***************************************************************************/
   public void reset()
   {
      joinRecords.clear();
   }



   /***************************************************************************
    * get the join records for a specific query join against the main record
    * of the workflow.
    *
    * <p>internally this method does cache those lookups - see {@code reset()}
    * to clear that cache.</p>
    ***************************************************************************/
   public List<QRecord> getJoinRecords(QueryJoin queryJoin) throws QException
   {
      String joinTableOrAlias = queryJoin.getJoinTableOrItsAlias();
      if(!StringUtils.hasContent(joinTableOrAlias))
      {
         throw (new QException("Missing joinTableOrAlias in queryJoin: " + queryJoin));
      }

      List<QRecord> baseRecords;
      if(queryJoin.getBaseTableOrAlias().equals(tableName))
      {
         baseRecords = List.of(record);
      }
      else
      {
         baseRecords = joinRecords.get(queryJoin.getBaseTableOrAlias());
      }

      QQueryFilter joinRecordsFilter = getJoinRecordsFilter(queryJoin, baseRecords);
      if(joinRecords.get(joinTableOrAlias) == null)
      {
         List<QRecord> records = QueryAction.execute(queryJoin.getJoinTable(), joinRecordsFilter);
         joinRecords.put(joinTableOrAlias, records);
      }

      return joinRecords.get(joinTableOrAlias);
   }



   /***************************************************************************
    * For a given queryJoin and a list of 'base records', build a filter that
    * can find join records.
    *
    * <p>Note that this will potentially be a (a=? AND b=?) OR (a=? AND b=?)
    * style query... </p>
    ***************************************************************************/
   protected QQueryFilter getJoinRecordsFilter(QueryJoin queryJoin, List<QRecord> baseRecords)
   {
      String baseTableName = queryJoin.getBaseTableOrAlias();

      QQueryFilter filter = new QQueryFilter().withBooleanOperator(QQueryFilter.BooleanOperator.OR);

      for(QRecord baseRecord : baseRecords)
      {
         QQueryFilter subFilter = new QQueryFilter();
         filter.addSubFilter(subFilter);

         for(JoinOn joinOn : queryJoin.getJoinMetaData().getJoinOns())
         {
            Serializable mainTableValue;
            String       joinTableField;
            if(queryJoin.getJoinMetaData().getLeftTable().equals(baseTableName))
            {
               mainTableValue = baseRecord.getValue(joinOn.getLeftField());
               joinTableField = joinOn.getRightField();
            }
            else
            {
               mainTableValue = baseRecord.getValue(joinOn.getRightField());
               joinTableField = joinOn.getLeftField();
            }

            if(mainTableValue == null)
            {
               subFilter.addCriteria(new QFilterCriteria(joinTableField, QCriteriaOperator.FALSE));
            }
            else
            {
               subFilter.addCriteria(new QFilterCriteria(joinTableField, QCriteriaOperator.EQUALS, mainTableValue));
            }
         }
      }

      return (filter);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void setJoinRecords(QueryJoin queryJoin, List<QRecord> joinRecords)
   {
      ArrayList<QRecord> recordArrayList = CollectionUtils.useOrWrap(joinRecords, new TypeToken<>() {});
      this.joinRecords.put(queryJoin.getJoinTableOrItsAlias(), recordArrayList);
   }



   /*******************************************************************************
    ** Getter for tableName
    **
    *******************************************************************************/
   public String getTableName()
   {
      return tableName;
   }



   /*******************************************************************************
    ** Getter for record
    **
    *******************************************************************************/
   public QRecord getRecord()
   {
      return record;
   }
}

