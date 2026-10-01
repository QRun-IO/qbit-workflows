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
