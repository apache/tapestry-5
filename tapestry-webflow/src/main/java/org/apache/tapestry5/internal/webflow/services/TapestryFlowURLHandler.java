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

package org.apache.tapestry5.internal.webflow.services;

import org.apache.tapestry5.webflow.WebflowConstants;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.core.collection.AttributeMap;

import javax.servlet.http.HttpServletRequest;

public class TapestryFlowURLHandler implements FlowUrlHandler
{
    public String getFlowExecutionKey(HttpServletRequest request)
    {
        return request.getParameter(WebflowConstants.FLOW_EXECUTION_KEY_PARAMETER);
    }

    public String getFlowId(HttpServletRequest request)
    {
        return request.getParameter(WebflowConstants.FLOW_ID_PARAMETER);
    }

    public String createFlowDefinitionUrl(String flowId, AttributeMap input, HttpServletRequest request)
    {
        return String.format("%s%s?%s=%s",
                             request.getContextPath(),
                             WebflowConstants.WEB_FLOW_PATH,
                             WebflowConstants.FLOW_ID_PARAMETER, flowId);
    }

    public String createFlowExecutionUrl(String flowId, String flowExecutionKey, HttpServletRequest request)
    {
        return String.format("%s%s?%s=%s&%s=%s",
                             request.getContextPath(),
                             WebflowConstants.WEB_FLOW_PATH,
                             WebflowConstants.FLOW_ID_PARAMETER, flowId,
                             WebflowConstants.FLOW_EXECUTION_KEY_PARAMETER, flowExecutionKey);
    }
}
