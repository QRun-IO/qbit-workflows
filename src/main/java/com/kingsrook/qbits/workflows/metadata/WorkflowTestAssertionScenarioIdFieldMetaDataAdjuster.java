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

package com.kingsrook.qbits.workflows.metadata;


import java.util.Set;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInput;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterInterface;
import com.kingsrook.qqq.frontend.materialdashboard.actions.formadjuster.FormAdjusterOutput;


/***************************************************************************
 * material-dashboard field-meta-data-adjuster to clear values that depend
 * on workflowTestScenarioId.
 ***************************************************************************/
public class WorkflowTestAssertionScenarioIdFieldMetaDataAdjuster implements FormAdjusterInterface
{

   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public FormAdjusterOutput execute(FormAdjusterInput input) throws QException
   {
      ///////////////////////////////////////////////////////////////////////////////////////////////
      // note, clearing variableName seems to not be working during original deployment of this... //
      ///////////////////////////////////////////////////////////////////////////////////////////////
      return (new FormAdjusterOutput()
         .withFieldsToClear(Set.of("variableName", "expectedValue", "queryFilterJson")));
   }

}
