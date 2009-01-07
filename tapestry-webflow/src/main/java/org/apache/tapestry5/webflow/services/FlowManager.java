// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.webflow.services;

/**
 * Tapestry service used to initiate a flow.  Once a flow is started, the flow will continue to operate until it is
 * cancelled or otherwise reaches its end state. Typically, an action event will initiate a flow.
 */
public interface FlowManager
{
    /**
     * Initiates a new web flow.
     *
     * @param flowId the identity of the flow to initiate
     */
    Object initiateFlow(String flowId);
}
