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
