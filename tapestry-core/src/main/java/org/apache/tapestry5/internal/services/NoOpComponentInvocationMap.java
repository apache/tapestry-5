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
import org.apache.tapestry5.dom.Element;

/**
 * The production implementation for {@link org.apache.tapestry5.internal.services.ComponentInvocationMap}. It does
 * absolutely nothing because it is not needed in production.
 */
public class NoOpComponentInvocationMap implements ComponentInvocationMap
{
    public void store(Element element, Link link)
    {
    }

    public void store(Link link, ComponentInvocation invocations)
    {
    }

    public ComponentInvocation get(Link link)
    {
        return null;
    }

    public void clear()
    {
    }

    public ComponentInvocation get(Element element)
    {
        return null;
    }
}
