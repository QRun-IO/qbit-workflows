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


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 * interface for the code that validates a step in a workflow, e.g., when it's
 * being saved.
 *******************************************************************************/
public interface WorkflowStepValidatorInterface
{

   /***************************************************************************
    * validate that a workflow step can be saved.
    *
    * @param step the step to validate against all the other params.
    * @param inputValues map of values from user
    * @param workflowRevision the workflow revision that the step will belong to
    * @param workflow the workflow that the revision will belong to
    * @param errors out param - any validation errors should be added to this list.
    ***************************************************************************/
   void validate(WorkflowStep step, Map<String, Serializable> inputValues, QRecord workflowRevision, QRecord workflow, List<String> errors) throws QException;

}
