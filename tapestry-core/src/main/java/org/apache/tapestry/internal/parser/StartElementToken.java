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

package org.apache.tapestry.internal.parser;

import org.apache.tapestry.ioc.Location;

/**
 * The start of an ordinary element within the template (as opposed to
 * {@link org.apache.tapestry.internal.parser.StartComponentToken}, which represents an active
 * Tapestry token. A start element token may be immediately followed by
 * {@link org.apache.tapestry.internal.parser.AttributeToken}s that represents the attributes
 * associated with the element. A start element token will always be balanced by a
 * {@link org.apache.tapestry.internal.parser.EndElementToken} (though there will likely be some
 * amount of intermediate tokens).
 * 
 * 
 */
public class StartElementToken extends TemplateToken
{
    private final String _name;

    public StartElementToken(String name, Location location)
    {
        super(TokenType.START_ELEMENT, location);

        _name = name;
    }

    /** Returns local name for the element. */
    public String getName()
    {
        return _name;
    }

    @Override
    public String toString()
    {
        return String.format("Start[%s]", _name);
    }
}
