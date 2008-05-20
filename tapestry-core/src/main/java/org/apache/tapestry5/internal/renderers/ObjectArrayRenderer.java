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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ObjectRenderer;

import java.util.Arrays;
import java.util.List;

/**
 * Renders an Object[] array as an unordered list.
 */
public class ObjectArrayRenderer implements ObjectRenderer<Object[]>
{
    private final ObjectRenderer masterRenderer;

    public ObjectArrayRenderer(@Primary ObjectRenderer masterRenderer)
    {
        this.masterRenderer = masterRenderer;
    }

    public void render(Object[] array, MarkupWriter writer)
    {
        if (array.length == 0)
        {
            writer.element("em");
            writer.write("empty array");
            writer.end();
            return;
        }

        List list = Arrays.asList(array);

        masterRenderer.render(list, writer);
    }
}
