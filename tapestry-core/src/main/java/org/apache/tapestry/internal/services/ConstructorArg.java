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

package org.apache.tapestry.internal.services;

import javassist.CtClass;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

/**
 * Stores transformation type data about one argument to a class constructor.
 */
class ConstructorArg
{
    private final CtClass _type;

    private final Object _value;

    /**
     * Constructs new instance.
     *
     * @param type  type of the parameter to be created (may not be null)
     * @param value value to be injected via the constructor (may be null)
     */
    ConstructorArg(CtClass type, Object value)
    {
        _type = notNull(type, "type");
        _value = value;
    }

    public CtClass getType()
    {
        return _type;
    }

    /**
     * The value to be injected (may be null).
     */
    public Object getValue()
    {
        return _value;
    }
}