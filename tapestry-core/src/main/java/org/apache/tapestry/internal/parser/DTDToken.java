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

package org.apache.tapestry.internal.parser;

import org.apache.tapestry.ioc.Location;

/**
 * Represents the presence of a Document Type declaration within a template. The Document type
 * declaration will be output to the client. In the event that multiple declarations are encountered
 * (a page and one or more nested components all declare a document type), the first document type
 * declared will be used.
 */
public class DTDToken extends TemplateToken
{
    private final String _name;

    private final String _publicId;

    private final String _systemId;

    public DTDToken(String name, String publicId, String systemId, Location location)
    {
        super(TokenType.DTD, location);

        _name = name;
        _publicId = publicId;
        _systemId = systemId;
    }

    /** Returns the doctype name (the name of the document root element) */
    public String getName()
    {
        return _name;
    }

    /** Returns the public identifier of the DTD */
    public String getPublicId()
    {
        return _publicId;
    }

    /** Returns the system identifier of the DTD */
    public String getSystemId()
    {
        return _systemId;
    }

    public String toString()
    {
        return String.format("DTD[name=%s; publicId=%s; systemId=%s]", _name, _publicId, _systemId);
    }
}
