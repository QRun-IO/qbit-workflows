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
