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

package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Renderable;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Environment;

public class RenderableProvider
{
    @Inject
    private Environment environment;

    @SetupRender
    void setup()
    {
        Renderable r = new Renderable()
        {
            public void render(MarkupWriter writer)
            {
                writer.element("strong");
                writer.write("A message provided by the RenderableProvider component.");
                writer.end();
            }
        };

        environment.push(Renderable.class, r);
    }

    @CleanupRender
    void cleanup()
    {
        environment.pop(Renderable.class);
    }
}
