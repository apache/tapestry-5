// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.runtime.RenderQueue;

/**
 * A page element that renders an attribute (into the most recently started element).
 * 
 * 
 */
public class AttributePageElement implements PageElement
{
    private final String _name;

    private final String _value;

    public AttributePageElement(String name, String value)
    {
        _name = name;
        _value = value;
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        writer.attributes(_name, _value);
    }

    @Override
    public String toString()
    {
        return String.format("Attribute[%s=%s]", _name, _value);
    }
}
