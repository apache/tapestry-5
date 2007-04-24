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

package org.apache.tapestry.dom;

import java.io.PrintWriter;

/**
 * Representation of a document type. Note that technically, a Doctype isn't a node in an xml
 * document; hence this doesn't extend node.
 */
public class DTD
{
    private final String _name;

    private final String _publicId;

    private final String _systemId;

    public DTD(String name, String publicId, String systemId)
    {
        _name = name;
        _publicId = publicId;
        _systemId = systemId;
    }

    public String getName()
    {
        return _name;
    }

    public String getPublicId()
    {
        return _publicId;
    }

    public String getSystemId()
    {
        return _systemId;
    }

    public void toMarkup(PrintWriter writer)
    {
        if (_publicId != null)
        {
            if (_systemId != null)
            {
                writer.printf("<!DOCTYPE %s PUBLIC \"%s\" \"%s\">", _name, _publicId, _systemId);
            }
            else
            {
                writer.printf("<!DOCTYPE %s PUBLIC \"%s\">", _name, _publicId);
            }
        }
        else if (_systemId != null)
        {
            writer.printf("<!DOCTYPE %s SYSTEM \"%s\">", _name, _systemId);
        }
    }
}
