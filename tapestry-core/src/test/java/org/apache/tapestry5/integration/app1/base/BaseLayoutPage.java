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

package org.apache.tapestry5.integration.app1.base;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Renderable;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.Delegate;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;

public class BaseLayoutPage
{
    @Inject
    private ComponentResources resources;

    @Component(parameters = "to=titleRenderer")
    private Delegate titleDelegate;

    public Renderable getTitleRenderer()
    {
        return new Renderable()
        {
            public void render(MarkupWriter writer)
            {
                writer.write(TapestryInternalUtils.toUserPresentable(resources.getPageName()));
            }
        };
    }
}
