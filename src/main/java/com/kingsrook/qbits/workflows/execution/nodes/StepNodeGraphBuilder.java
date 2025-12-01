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

package com.kingsrook.qbits.workflows.execution.nodes;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.kingsrook.qbits.workflows.definition.OutboundLinkMode;
import com.kingsrook.qbits.workflows.definition.WorkflowStepType;
import com.kingsrook.qbits.workflows.definition.WorkflowsRegistry;
import com.kingsrook.qbits.workflows.execution.FindFirstJoinPoint;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 * Class that can convert a list of steps & links (as a workflow revision is
 * stored in a database) to a hierarchical graph data structure - a list of nodes,
 * each of which is a step, and optionally a map of lists of sub-branches
 * (which can be more useful for various kinds of processing).
 *******************************************************************************/
public class StepNodeGraphBuilder
{
   private static final QLogger LOG = QLogger.getLogger(StepNodeGraphBuilder.class);



   /***************************************************************************
    *
    ***************************************************************************/
   public static NodeSequence buildNodeSequence(Integer startStepNo, List<WorkflowStep> workflowSteps, List<WorkflowLink> workflowLinks)
   {
      Map<Integer, WorkflowStep> stepMap = CollectionUtils.listToMap(workflowSteps, ws -> ws.getStepNo());
      ListingHash<Integer, WorkflowLink> linksFromMap = new ListingHash<>();
      Map<Integer, Set<Integer>>         linksToMap   = new HashMap<>();

      for(WorkflowLink workflowLink : workflowLinks)
      {
         linksFromMap.add(workflowLink.getFromStepNo(), workflowLink);

         if(workflowLink.getToStepNo() != null)
         {
            Set<Integer> subToSet = linksToMap.computeIfAbsent(workflowLink.getToStepNo(), k -> new HashSet<>());
            subToSet.add(workflowLink.getFromStepNo());
         }
      }

      NodeSequence nodeSequence = new NodeSequence();
      buildNodeSequence(nodeSequence, stepMap.get(startStepNo), stepMap, linksFromMap, null, 0);
      return (nodeSequence);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static void buildNodeSequence(NodeSequence nodeSequence, WorkflowStep startStep, Map<Integer, WorkflowStep> stepMap, ListingHash<Integer, WorkflowLink> linksFromMap, Integer joinStepNo, Integer depth)
   {
      WorkflowsRegistry workflowsRegistry = WorkflowsRegistry.of(QContext.getQInstance());

      WorkflowStep currentWorkflowStep = startStep;
      while(currentWorkflowStep != null)
      {
         WorkflowStepType workflowStepType = workflowsRegistry.getWorkflowStepType(currentWorkflowStep.getWorkflowStepTypeName());
         if(workflowStepType == null)
         {
            LOG.warn("Unrecognized workflow step type: " + currentWorkflowStep.getWorkflowStepTypeName());
            return;
         }

         if(Objects.equals(currentWorkflowStep.getStepNo(), joinStepNo))
         {
            //////////////////////////////////////////////////////////////////////////////
            // if we've reached the join step, it's the end of this sequence, so return //
            //////////////////////////////////////////////////////////////////////////////
            return;
         }

         StepNode stepNode = new StepNode(currentWorkflowStep);
         nodeSequence.add(stepNode);

         List<WorkflowLink> outboundLinks = linksFromMap.get(currentWorkflowStep.getStepNo());
         if(CollectionUtils.nullSafeIsEmpty(outboundLinks))
         {
            /////////////////////////////////////////////////////////////////////////////
            // if no outbound links, then we're at the end of this sequence, so return //
            /////////////////////////////////////////////////////////////////////////////
            return;
         }
         else if(outboundLinks.size() == 1 && !StringUtils.hasContent(outboundLinks.get(0).getConditionValue())) // ?? avoid being tripped by a branch w/ only 1 side populated at the end of the graph
         {
            ///////////////////////////////////////////////////////////////////
            // 1 outbound link here means we're not in a switch/if-else/fork //
            ///////////////////////////////////////////////////////////////////
            WorkflowLink outboundLink = outboundLinks.get(0);
            currentWorkflowStep = outboundLink.getToStepNo() == null ? null : stepMap.get(outboundLink.getToStepNo());

            ////////////////////////////////
            // todo is this == null okay? //
            ////////////////////////////////
            if(currentWorkflowStep == null || currentWorkflowStep.getStepNo().equals(joinStepNo))
            {
               ///////////////////////////////////////////////////////
               // this means we're ending a nested (recursive) call //
               ///////////////////////////////////////////////////////
               return;
            }
         }
         else
         {
            if(workflowStepType.getOutboundLinkMode().equals(OutboundLinkMode.TWO) || workflowStepType.getOutboundLinkMode().equals(OutboundLinkMode.VARIABLE))
            {
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               // this means we are starting a fork.  find where it ends - where the branches first re-converge //
               ///////////////////////////////////////////////////////////////////////////////////////////////////
               Integer branchJoin = FindFirstJoinPoint.executeForLinks(outboundLinks, stepMap, linksFromMap);

               ////////////////////////////////////////////
               // for each branch, make a recursive call //
               ////////////////////////////////////////////
               for(WorkflowLink outboundLink : outboundLinks)
               {
                  String       conditionValue = outboundLink.getConditionValue();
                  NodeSequence branchSequence = new NodeSequence();
                  stepNode.subSequences().put(conditionValue, branchSequence);

                  if(branchJoin != null && Objects.equals(branchJoin, outboundLink.getToStepNo()))
                  {
                     ////////////////////////////////////////////////////////////////////////
                     // this indicates an empty branch - so continue to next outbound link //
                     ////////////////////////////////////////////////////////////////////////
                     continue;
                  }

                  ///////////////////////////////////////////////////////////////////////////////////////////
                  // if this is a step out of the branch (e.g., to a join), then don't process it in here. //
                  // also though, this line doesn't actually get hit, so...                                //
                  ///////////////////////////////////////////////////////////////////////////////////////////
                  if(Objects.equals(outboundLink.getToStepNo(), joinStepNo))
                  {
                     continue;
                  }
                  else
                  {
                     if(outboundLink.getToStepNo() != null)
                     {
                        buildNodeSequence(branchSequence, stepMap.get(outboundLink.getToStepNo()), stepMap, linksFromMap, branchJoin, depth + 1);
                     }
                  }
               }

               ////////////////////////////////////////////////////////////////////////////////
               // after handling all the branches, then continue the loop with the join step //
               ////////////////////////////////////////////////////////////////////////////////
               currentWorkflowStep = branchJoin == null ? null : stepMap.get(branchJoin);
            }
            else if(workflowStepType.getOutboundLinkMode().equals(OutboundLinkMode.CONTAINER))
            {
               ///////////////////////////////////////////////////////////////////////
               // if the container is non-empty, there should be a push link for it //
               ///////////////////////////////////////////////////////////////////////
               Optional<WorkflowLink> pushLink = outboundLinks.stream().filter(ol -> "push".equals(ol.getConditionValue())).findFirst();
               if(pushLink.isPresent())
               {
                  if(pushLink.get().getToStepNo() != null)
                  {
                     NodeSequence containerSequence = new NodeSequence();
                     stepNode.subSequences().put("push", containerSequence);
                     buildNodeSequence(containerSequence, stepMap.get(pushLink.get().getToStepNo()), stepMap, linksFromMap, null, depth + 1);
                  }
               }

               Optional<WorkflowLink> popLink = outboundLinks.stream().filter(ol -> "pop".equals(ol.getConditionValue())).findFirst();
               if(popLink.isPresent())
               {
                  if(popLink.get().getToStepNo() != null)
                  {
                     currentWorkflowStep = stepMap.get(popLink.get().getToStepNo());
                  }
               }
               else
               {
                  return;
               }
            }
            else
            {
               LOG.warn("expected to have multiple outbound links for a step of type: " + currentWorkflowStep.getWorkflowStepTypeName());
            }
         }
      }
   }

}
