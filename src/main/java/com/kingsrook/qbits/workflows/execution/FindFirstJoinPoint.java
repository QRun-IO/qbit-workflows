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


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;


/*******************************************************************************
 * given a graph, find the first "join point" where multiple forks come together.
 * Credit: https://chatgpt.com/share/68000e8f-879c-8011-bb33-0564eb77a1f2
 *******************************************************************************/
public class FindFirstJoinPoint
{

   /***************************************************************************
    **
    ***************************************************************************/
   public static Integer executeForFromStepNo(Integer fromStepNo, Map<Integer, WorkflowStep> stepMap, ListingHash<Integer, WorkflowLink> linksMapByFromStepNo)
   {
      List<Integer> startLinks = CollectionUtils.nonNullList(linksMapByFromStepNo.get(fromStepNo)).stream().map(ws -> ws.getToStepNo()).toList();
      return execute(startLinks, stepMap, linksMapByFromStepNo);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static Integer executeForLinks(Collection<WorkflowLink> startLinks, Map<Integer, WorkflowStep> stepMap, ListingHash<Integer, WorkflowLink> linksMapByFromStepNo)
   {
      return execute(startLinks.stream().map(ws -> ws.getToStepNo()).toList(), stepMap, linksMapByFromStepNo);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @SuppressWarnings("Java8MapApi")
   public static Integer execute(Collection<Integer> startStepNos, Map<Integer, WorkflowStep> stepMap, ListingHash<Integer, WorkflowLink> linksMapByFromStepNo)
   {
      //////////////////////////////////////////////////////////////////////////////////
      // visited: map each node to a set of source step numbers that have reached it. //
      //////////////////////////////////////////////////////////////////////////////////
      ListingHash<Integer, Integer> visited = new ListingHash<>();

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // Initialize the multi-source BFS queue. Each entry carries the current node id and its originating source. //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
      Deque<BFSQueueNode> queue = new ArrayDeque<>();

      ////////////////////////////////////////////////////////////////////////////////
      // Seed the queue with each starting node, marking each as visited by itself. //
      ////////////////////////////////////////////////////////////////////////////////
      for(Integer node : startStepNos)
      {
         visited.add(node, node);
         queue.add(new BFSQueueNode(node, node));
      }

      ///////////////////////////////////
      // Perform the multi-source BFS. //
      ///////////////////////////////////
      while(!queue.isEmpty())
      {
         BFSQueueNode firstNode = queue.removeFirst();
         Integer      current   = firstNode.current;
         Integer      source    = firstNode.source;

         ////////////////////////////////////////////////////////////////////
         // Get the list of outgoing links/neighbors for the current node. //
         ////////////////////////////////////////////////////////////////////
         List<WorkflowLink> neighbors = Objects.requireNonNullElse(linksMapByFromStepNo.get(current), Collections.emptyList());
         for(WorkflowLink neighbor : neighbors)
         {
            Integer neighborId = neighbor.getToStepNo();
            if(neighborId == null)
            {
               continue;
            }

            ///////////////////////////////////////////////////////////////////////////
            // Retrieve or initialize the set of sources that visited this neighbor. //
            ///////////////////////////////////////////////////////////////////////////
            List<Integer> sourcesSet = visited.get(neighborId);
            if(sourcesSet == null)
            {
               sourcesSet = new ArrayList<>();
               visited.put(neighborId, sourcesSet);
            }

            /////////////////////////////////////////////////////////////////////
            // If the current source has not visited the neighbor yet, add it. //
            /////////////////////////////////////////////////////////////////////
            if(!sourcesSet.contains(source))
            {
               sourcesSet.add(source);

               //////////////////////////////////////////////////////////////
               // Check if the neighbor was reached from multiple sources. //
               // todo: should this be it has to equal all of them?        //
               //  if(sourcesSet.size() == startStepNos.size())            //
               //////////////////////////////////////////////////////////////
               if(sourcesSet.size() > 1)
               {
                  ///////////////////////////////////////////////////////////////////////////////////////
                  // This is the first join point encountered – return the corresponding WorkflowStep. //
                  ///////////////////////////////////////////////////////////////////////////////////////
                  return (stepMap.get(neighborId).getStepNo());
               }

               /////////////////////////////////////////////////////////////////
               // Continue the BFS from this neighbor for the current source. //
               /////////////////////////////////////////////////////////////////
               queue.push(new BFSQueueNode(neighborId, source));
            }
         }
      }

      ///////////////////////////////////////////////////////////////
      // If no join point was found across all paths, return null. //
      ///////////////////////////////////////////////////////////////
      return null;
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private record BFSQueueNode(Integer current, Integer source)
   {

   }

}
