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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.pages.ExceptionReport;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.ObjectRenderer;

/**
 * Renders out an object using the {@link ObjectRenderer} service. Used primarily on the
 * {@link ExceptionReport} page.
 */
public class RenderObject
{
    @Parameter(required = true)
    private Object _object;

    @Inject
    private ObjectRenderer<Object> _renderer;

    boolean beginRender(MarkupWriter writer)
    {
        _renderer.render(_object, writer);

        return false;
    }
}
