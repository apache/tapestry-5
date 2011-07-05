// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.dynamic;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.services.dynamic.DynamicDelegate;

/**
 * Represents an dynamically calculated attribute within a {@link DynamicTemplateElement}.
 * 
 * @since 5.3
 */
class DynamicTemplateAttribute
{
    private final String namespaceURI, name;

    private final Mapper<DynamicDelegate, String> valueExtractor;

    public DynamicTemplateAttribute(String namespaceURI, String name, Mapper<DynamicDelegate, String> valueExtractor)
    {
        this.namespaceURI = namespaceURI;
        this.name = name;
        this.valueExtractor = valueExtractor;
    }

    void write(MarkupWriter writer, DynamicDelegate delegate)
    {
        String value = valueExtractor.map(delegate);

        writer.attributeNS(namespaceURI, name, value);
    }
}
