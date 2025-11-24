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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.kingsrook.qbits.workflows.model.WorkflowLink;
import com.kingsrook.qbits.workflows.model.WorkflowRunLogStep;
import com.kingsrook.qbits.workflows.model.WorkflowStep;
import com.kingsrook.qqq.backend.core.utils.ListingHash;


/***************************************************************************
 * state data carried throughout the {@link WorkflowExecutor} - which is
 * useful to "leak out" in a managed way, e.g., for step types that do their
 * own more sophisticated kind of execution, e.g., {@link WorkflowMultiForkingStepExecutorInterface}
 ***************************************************************************/
record ExecutionPayload(Map<Integer, WorkflowStep> stepMap, ListingHash<Integer, WorkflowLink> linkMap, WorkflowTypeExecutorInterface workflowTypeExecutor, AtomicInteger seqNo, List<WorkflowRunLogStep> logStepList)
{
}
