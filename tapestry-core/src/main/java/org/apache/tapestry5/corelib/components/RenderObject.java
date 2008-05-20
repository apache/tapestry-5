// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.pages.ExceptionReport;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ObjectRenderer;

/**
 * Renders out an object using the {@link ObjectRenderer} service. Used primarily on the {@link ExceptionReport} page.
 * This is focused on objects that have a specific {@link ObjectRenderer} strategy. The {@link BeanDisplay} component is
 * used for displaying the contents of arbitrary objects in terms of a series of property names and values.
 */
public class RenderObject
{
    @Parameter(required = true)
    private Object object;

    @Inject
    @Primary
    private ObjectRenderer<Object> renderer;

    boolean beginRender(MarkupWriter writer)
    {
        renderer.render(object, writer);

        return false;
    }
}
