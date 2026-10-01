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


import java.util.List;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 * interface for the code that validates the overall structure of a workflow,
 * e.g., when it's being saved.  This would be where you'd validate relationships
 * between steps (e.g., this step must have some other step within it)
 *******************************************************************************/
public interface WorkflowTypeValidatorInterface
{

   /***************************************************************************
    * validate that a workflow can be saved.
    *
    * @param workflow the workflow record under which a new revision is being saved
    * @param workflowRevision the new revision record that's been built
    * @param workflowSteps list of workflow steps being saved
    * @param workflowLinks list of workflow links being saved
    * @param errors out param - any validation errors should be added to this list.
    ***************************************************************************/
   void validate(QRecord workflow, QRecord workflowRevision, List<WorkflowStep> workflowSteps, List<WorkflowLink> workflowLinks, List<String> errors) throws QException;

}
