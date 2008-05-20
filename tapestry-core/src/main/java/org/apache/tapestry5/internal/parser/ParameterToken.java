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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;

/**
 * A parameter block to be passed to a component as a parameter.
 */
public class ParameterToken extends TemplateToken
{
    private final String name;

    /**
     * @param name     the name of the parameter to be bound
     * @param location location of the element
     */
    public ParameterToken(String name, Location location)
    {
        super(TokenType.PARAMETER, location);

        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return String.format("Parameter[%s]", name);
    }
}
