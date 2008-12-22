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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.EnvironmentalAccess;

class EnvironmentalAccessImpl implements EnvironmentalAccess
{
    private final Environment environment;

    private final Class type;

    private boolean cached = false;

    private Object currentValue;

    public EnvironmentalAccessImpl(Environment environment, Class type)
    {
        this.environment = environment;
        this.type = type;
    }

    public Object peek()
    {
        if (!cached)
        {
            currentValue = environment.peek(type);
            cached = true;
        }

        return currentValue;
    }

    public Object peekRequired()
    {
        if (!cached)
        {
            currentValue = environment.peekRequired(type);
            cached = true;
        }

        return currentValue;
    }

    /**
     * Invoked whenever the value stored in the Environment (for this type) changes.
     */
    void invalidate()
    {
        cached = false;
        currentValue = null;
    }
}
