// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services;

/**
 * An event handler method may return an instance of this class to trigger the rendering 
 * of a particular page without causing a redirect to that page.
 * 
 * @since 5.2.0
 *
 */
public final class StreamPageContent
{
    private final Class<?> pageClass;
    private final Object[] pageActivationContext;

    /**
     * 
     * @param pageClass class of the page to render
     */
    public StreamPageContent(final Class<?> pageClass)
    {
        this(pageClass, (Object[]) null);
    }

    /**
     * 
     * @param pageClass class of the page to render
     * @param pageActivationContext activation context of the page
     */
    public StreamPageContent(final Class<?> pageClass, final Object... pageActivationContext)
    {
        super();
        this.pageClass = pageClass;
        this.pageActivationContext = pageActivationContext;
    }

    /**
     * Returns the class of the page to render.
     */
    public Class<?> getPageClass()
    {
        return this.pageClass;
    }

    /**
     * Returns the activation context of the page.
     */
    public Object[] getPageActivationContext()
    {
        return this.pageActivationContext;
    }
}
