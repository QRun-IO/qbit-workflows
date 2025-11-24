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
import java.util.Map;
import com.kingsrook.qbits.workflows.model.WorkflowStep;


/***************************************************************************
 * Single step, plus, if it opens a nested scope (e.g., branches, or a container)
 * then those subSequences, along with their conditionalValues (guards/labels)
 * as map keys
 ***************************************************************************/
public record StepNode(WorkflowStep step, Map<String, NodeSequence> subSequences)
{

   /*******************************************************************************
    ** Constructor for step w/o subSequences
    **
    *******************************************************************************/
   public StepNode(WorkflowStep step)
   {
      this(step, new HashMap<>());
   }

}
