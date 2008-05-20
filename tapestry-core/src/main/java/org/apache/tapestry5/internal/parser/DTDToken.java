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
 * Represents the presence of a Document Type declaration within a template. The Document type declaration will be
 * output to the client. In the event that multiple declarations are encountered (a page and one or more nested
 * components all declare a document type), the first document type declared will be used.
 */
public class DTDToken extends TemplateToken
{
    private final String name;

    private final String publicId;

    private final String systemId;

    public DTDToken(String name, String publicId, String systemId, Location location)
    {
        super(TokenType.DTD, location);

        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    /**
     * Returns the doctype name (the name of the document root element)
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the public identifier of the DTD
     */
    public String getPublicId()
    {
        return publicId;
    }

    /**
     * Returns the system identifier of the DTD
     */
    public String getSystemId()
    {
        return systemId;
    }

    @Override
    public String toString()
    {
        return String.format("DTD[name=%s; publicId=%s; systemId=%s]", name, publicId, systemId);
    }
}
