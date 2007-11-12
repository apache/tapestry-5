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

/*
 * Created on Mar 15, 2007
 * 
 * 
 */
package org.apache.tapestry.internal.structure;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.runtime.RenderQueue;

public class DTDPageElement implements PageElement
{

    private final String _name;
    private final String _publicId;
    private final String _systemId;

    public DTDPageElement(String name, String publicId, String systemId)
    {
        _name = name;
        _publicId = publicId;
        _systemId = systemId;
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        writer.getDocument().dtd(_name, _publicId, _systemId);
    }

    @Override
    public String toString()
    {
        return String.format("DTD[name=%s; publicId=%s; systemId=%s]", _name, _publicId, _systemId);
    }

}
