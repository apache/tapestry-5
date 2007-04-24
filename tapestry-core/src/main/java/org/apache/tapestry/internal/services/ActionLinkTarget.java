// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

/**
 * It represents an invocation target for an action link.
 */
public class ActionLinkTarget implements InvocationTarget
{
    private final String _action;

    private final String _pageName;

    private final String _componentNestedId;

    public ActionLinkTarget(String action, String pageName, String componentNestedId)
    {
        _action = action;
        _pageName = pageName;
        _componentNestedId = componentNestedId;

    }

    public String getPath()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(_pageName.toLowerCase());
        builder.append(".");
        // Already lower case by design.
        builder.append(_componentNestedId);
        builder.append(".");
        builder.append(_action);

        return builder.toString();
    }

    public String getAction()
    {
        return _action;
    }

    public String getComponentNestedId()
    {
        return _componentNestedId;
    }

    public String getPageName()
    {
        return _pageName;
    }

}
