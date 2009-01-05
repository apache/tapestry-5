// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.ioc.ObjectCreator;

/**
 * An {@link org.apache.tapestry5.ioc.ObjectCreator} for a statically identified object (typically, a bean from the
 * Spring application context).
 */
public class StaticObjectCreator implements ObjectCreator
{
    private final Object object;

    private final String description;

    public StaticObjectCreator(Object object, String description)
    {
        this.object = object;
        this.description = description;
    }

    public Object createObject()
    {
        return object;
    }

    @Override
    public String toString()
    {
        return String.format("<ObjectCreator for %s>", description);
    }
}
