// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.components;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.components.ActionLink;

public class ActionLinkIndirect
{
    /**
     * The component to be rendered.
     */
    @Parameter(required = true, defaultPrefix = "component")
    private ActionLink _component;

    Object beginRender(MarkupWriter writer)
    {
        writer.element("p");

        return _component;
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end(); // p
    }
}
