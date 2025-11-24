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


/***************************************************************************
 * list of nodes in a graph representation of a workflow
 ***************************************************************************/
public record NodeSequence(List<StepNode> nodes)
{
   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public NodeSequence()
   {
      this(new ArrayList<>());
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public void add(StepNode node)
   {
      nodes.add(node);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public StepNode get(int i)
   {
      return nodes.get(i);
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public int size()
   {
      return (nodes.size());
   }
}
