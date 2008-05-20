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
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.corelib.base.AbstractPropertyOutput;

/**
 * Outputs a single property value. Overrides for individual properties come from block parameters whose name matches
 * the {@linkplain PropertyModel#getId() property id}. This component is rarely used on its own, but is a critical piece
 * of the {@link BeanDisplay} component.
 */
public class PropertyDisplay extends AbstractPropertyOutput
{
    Object beginRender(MarkupWriter writer)
    {
        return renderPropertyValue(writer, getPropertyModel().getId());
    }
}
