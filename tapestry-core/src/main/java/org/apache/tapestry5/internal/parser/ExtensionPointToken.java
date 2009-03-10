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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Location;

/**
 * A token that represents the replacement of a portion of the template with the content of an extension point, or an
 * override of that extension point.
 *
 * @see org.apache.tapestry5.internal.parser.ComponentTemplate#getExtensionPointTokens(String)
 * @since 5.1.0.1
 */
public class ExtensionPointToken extends TemplateToken
{
    private final String extentionPointId;

    public ExtensionPointToken(String extentionPointId, Location location)
    {
        super(TokenType.EXTENSION_POINT, location);
        this.extentionPointId = extentionPointId;
    }

    public String getExtentionPointId()
    {
        return extentionPointId;
    }
}
