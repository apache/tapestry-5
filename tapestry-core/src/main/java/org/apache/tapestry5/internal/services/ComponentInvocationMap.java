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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.test.PageTester;

/**
 * Used by the {@link PageTester} to map {@link Element}s (pulled from the rendered {@link Document}) into {@link
 * ComponentInvocation}s, that can be used to to trigger further (simulated) requests. In this way, a unit test can have
 * the {@link PageTester#clickLink(Element) click a link} or {@link PageTester#submitForm(Element, java.util.Map) submit
 * a form}.
 * <p/>
 * The information needed is generated in slightly disparate places, so the {@link LinkFactory} tells the map about
 * Links and ComponentInvocations, and the {@link MarkupWriter} will link Elements to Link instance.
 */
public interface ComponentInvocationMap
{
    /**
     * Stores a connection between a particular link and an invocation of a component.
     */
    void store(Link link, ComponentInvocation invocation);

    /**
     * Stores a connection between an element and the link associated with that element.
     */
    void store(Element element, Link link);

    /**
     * Returns the invocation associated with a link.
     *
     * @param link previously create link
     * @return associcated component invocation, or null
     */
    ComponentInvocation get(Link link);

    /**
     * Returns the invocation associated with a rendered element.
     *
     * @param element extracted from the rendered {@link Document}
     * @return the corresponding invocation
     */
    ComponentInvocation get(Element element);

    void clear();
}
