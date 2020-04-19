// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Provides support for elements that will optionally render a unique {@code id} attribute, but only if it is
 * requested. Subclasses should invoke {@link #storeElement(org.apache.tapestry5.dom.Element)}
 * when they begin an element that requires an id.
 *
 * @since 5.4
 */
public abstract class BaseClientElement implements ClientElement
{
    private Element element;

    private String clientId;

    @Inject
    protected ComponentResources resources;

    @Environmental
    protected JavaScriptSupport javaScriptSupport;

    /**
     * Invoked (usually from a {@link org.apache.tapestry5.annotations.BeginRender} phase method) to assign
     * the element, and clear the clientId (only relevant for components that render in a loop).
     * @param element the element to store
     */
    protected void storeElement(Element element)
    {
        assert element != null;

        this.element = element;
        clientId = null; // Until asked.
    }

    /**
     * When invoked the first time (per request), a unique id is assigned and and id attribute added to the {@linkplain #element element} for
     * the component.
     */
    public String getClientId()
    {

        if (clientId == null)
        {
            if (element == null)
            {
                throw new IllegalStateException(String.format("Component %s has not yet rendered; it is not possible to request its client id until after it has begun rendering.",
                        resources.getCompleteId()));
            }

            clientId = javaScriptSupport.allocateClientId(resources);

            element.forceAttributes("id", clientId);
        }

        return clientId;
    }
}
