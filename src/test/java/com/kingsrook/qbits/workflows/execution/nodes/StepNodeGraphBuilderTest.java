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


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.workflows.BaseTest;
import com.kingsrook.qbits.workflows.TestWorkflowDefinitions;
import com.kingsrook.qbits.workflows.WorkflowsTestDataSource;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;


/*******************************************************************************
 ** Unit test for StepNodeGraphBuilder 
 *******************************************************************************/
class StepNodeGraphBuilderTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   void beforeEach() throws QException
   {
      TestWorkflowDefinitions.registerTestWorkflowTypes();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSimpleFlat()
   {
      List<WorkflowStep> steps = new ArrayList<>();
      WorkflowStep       step1 = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);
      WorkflowStep       step2 = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);
      WorkflowStep       step3 = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);

      List<WorkflowLink> links = new ArrayList<>();
      links.add(WorkflowsTestDataSource.newLink(step1.getStepNo(), step2.getStepNo()));
      links.add(WorkflowsTestDataSource.newLink(step2.getStepNo(), step3.getStepNo()));

      NodeSequence nodeSequence = new StepNodeGraphBuilder().buildNodeSequence(step1.getStepNo(), steps, links);

      assertSame(step1, nodeSequence.get(0).step());
      assertSame(step2, nodeSequence.get(1).step());
      assertSame(step3, nodeSequence.get(2).step());
   }



   /*******************************************************************************
    * we'll evolve this graph through these 4 versions:
    *
    * . add1  >    add1    >    add1 .  > (wrap it all in a container)
    * .  |    >     |      >     |
    * .branch >   branch   >   branch
    * ./      >   /   \    >   /   \
    * add2    > add2 add3  > add2 add3
    * .                        \  /
    * .                        add4
    *******************************************************************************/
   @Test
   void testSimpleBranch()
   {
      List<WorkflowStep> steps = new ArrayList<>();
      List<WorkflowLink> links = new ArrayList<>();

      WorkflowStep add1   = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);
      WorkflowStep branch = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.BOOLEAN_CONDITIONAL, null);
      WorkflowStep add2   = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);

      links.add(WorkflowsTestDataSource.newLink(add1.getStepNo(), branch.getStepNo()));
      links.add(WorkflowsTestDataSource.newLink(branch.getStepNo(), add2.getStepNo(), "true"));

      {
         NodeSequence nodeSequence = StepNodeGraphBuilder.buildNodeSequence(add1.getStepNo(), steps, links);
         assertEquals(2, nodeSequence.size());
         assertSame(add1, nodeSequence.get(0).step());
         assertSame(branch, nodeSequence.get(1).step());

         StepNode branchNode = nodeSequence.get(1);
         assertEquals(1, branchNode.subSequences().size());
         assertEquals(1, branchNode.subSequences().get("true").size());
         assertSame(add2, branchNode.subSequences().get("true").get(0).step());
      }

      ////////////////////////////////////
      // add a step in the false branch //
      ////////////////////////////////////
      WorkflowStep add3 = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);
      links.add(WorkflowsTestDataSource.newLink(branch.getStepNo(), add3.getStepNo(), "false"));

      {
         NodeSequence nodeSequence = StepNodeGraphBuilder.buildNodeSequence(add1.getStepNo(), steps, links);
         assertEquals(2, nodeSequence.size());
         assertSame(add1, nodeSequence.get(0).step());
         assertSame(branch, nodeSequence.get(1).step());

         StepNode branchNode = nodeSequence.get(1);
         assertEquals(2, branchNode.subSequences().size());
         assertEquals(1, branchNode.subSequences().get("true").size());
         assertSame(add2, branchNode.subSequences().get("true").get(0).step());
         assertEquals(1, branchNode.subSequences().get("false").size());
         assertSame(add3, branchNode.subSequences().get("false").get(0).step());
      }

      /////////////////////////////////////////////////
      // add a step after the branch now, and re-run //
      /////////////////////////////////////////////////
      WorkflowStep add4 = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.ADD_X_TO_SUM_ACTION, null);
      links.add(WorkflowsTestDataSource.newLink(add2.getStepNo(), add4.getStepNo()));
      links.add(WorkflowsTestDataSource.newLink(add3.getStepNo(), add4.getStepNo()));

      {
         NodeSequence nodeSequence = StepNodeGraphBuilder.buildNodeSequence(add1.getStepNo(), steps, links);
         assertEquals(3, nodeSequence.size());
         assertSame(add1, nodeSequence.get(0).step());
         assertSame(branch, nodeSequence.get(1).step());
         assertSame(add4, nodeSequence.get(2).step());
      }

      ///////////////////////////////////
      // now put it all in a container //
      ///////////////////////////////////
      WorkflowStep container = WorkflowsTestDataSource.newStep(steps, TestWorkflowDefinitions.CONTAINER, null);
      links.add(WorkflowsTestDataSource.newLink(container.getStepNo(), add1.getStepNo(), "push"));
      {
         NodeSequence nodeSequence = StepNodeGraphBuilder.buildNodeSequence(container.getStepNo(), steps, links);
         assertEquals(1, nodeSequence.size());
         assertSame(container, nodeSequence.get(0).step());

         StepNode containerNode = nodeSequence.get(0);
         assertEquals(1, containerNode.subSequences().size());
         NodeSequence containedSequence = containerNode.subSequences().get("push");

         assertEquals(3, containedSequence.size());
         assertSame(add1, containedSequence.get(0).step());
         assertSame(branch, containedSequence.get(1).step());
         assertSame(add4, containedSequence.get(2).step());
      }
   }

}