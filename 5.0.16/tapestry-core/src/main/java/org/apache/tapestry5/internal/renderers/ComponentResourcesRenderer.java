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

package org.apache.tapestry5.internal.renderers;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ObjectRenderer;

/**
 * Renders {@link ComponentResources} instance, showing the complete id and the class name and the location (if a
 * location is available, it won't be for pages).
 */
public class ComponentResourcesRenderer implements ObjectRenderer<ComponentResources>
{
    private final ObjectRenderer masterRenderer;

    public ComponentResourcesRenderer(@Primary ObjectRenderer masterRenderer)
    {
        this.masterRenderer = masterRenderer;
    }

    public void render(ComponentResources object, MarkupWriter writer)
    {
        writer.writef("%s (class %s)", object.getCompleteId(), object.getComponentModel().getComponentClassName());

        Location location = object.getLocation();

        if (location != null)
        {
            writer.element("br");
            writer.end();

            masterRenderer.render(location, writer);
        }
    }
}
