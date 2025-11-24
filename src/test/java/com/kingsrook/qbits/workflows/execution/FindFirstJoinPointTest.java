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

package com.kingsrook.qbits.workflows.execution;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for FindFirstJoinPoint 
 *******************************************************************************/
class FindFirstJoinPointTest extends BaseTest
{

   /*******************************************************************************
    *     1
    *   / | \
    *  /  |   \
    * 2   3    4
    * |   |  /  \
    * |   5  6  7
    * |   |  |  |
    * |   |  |  8
    * |   |  \ /
    * |   |   9
    *  \  |  /
    *    10
    *******************************************************************************/
   @Test
   void test()
   {
      Map<Integer, WorkflowStep> nodeMap = makeNodeMap(10);

      ListingHash<Integer, WorkflowLink> outEdges = new ListingHash<>();
      addEdge(outEdges, 1, 2);
      addEdge(outEdges, 1, 3);
      addEdge(outEdges, 1, 4);
      addEdge(outEdges, 2, 10);
      addEdge(outEdges, 3, 5);
      addEdge(outEdges, 4, 6);
      addEdge(outEdges, 5, 10);
      addEdge(outEdges, 6, 9);
      addEdge(outEdges, 7, 8);
      addEdge(outEdges, 8, 9);
      addEdge(outEdges, 9, 10);

      /////////////////////////////////
      // start from the top (node 1) //
      /////////////////////////////////
      assertEquals(10, FindFirstJoinPoint.execute(List.of(2, 3, 4), nodeMap, outEdges));

      ////////////////////////////////////////////
      // start from the rightmost fork (node 4) //
      ////////////////////////////////////////////
      assertEquals(9, FindFirstJoinPoint.execute(List.of(6, 7), nodeMap, outEdges));

      //////////////////////////////////////////////////////////////////////////////////
      // remove node 10 - so they all just end and don't re-join (so expect null out) //
      //////////////////////////////////////////////////////////////////////////////////
      nodeMap.remove(10);
      outEdges.remove(9);
      outEdges.remove(2);
      outEdges.remove(5);
      assertNull(FindFirstJoinPoint.execute(List.of(2, 3, 4), nodeMap, outEdges));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void addEdge(ListingHash<Integer, WorkflowLink> outEdges, Integer fromStepNo, Integer toStepNo)
   {
      outEdges.add(fromStepNo, new WorkflowLink().withToStepNo(toStepNo));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private Map<Integer, WorkflowStep> makeNodeMap(int n)
   {
      Map<Integer, WorkflowStep> nodeMap = new HashMap<>();
      for(int i = 1; i <= n; i++)
      {
         addNode(nodeMap, i);
      }
      return (nodeMap);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private void addNode(Map<Integer, WorkflowStep> nodeMap, Integer stepNo)
   {
      nodeMap.put(stepNo, new WorkflowStep().withStepNo(stepNo));
   }

}