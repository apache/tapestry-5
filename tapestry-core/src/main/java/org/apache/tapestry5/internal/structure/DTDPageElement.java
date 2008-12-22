// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

public class DTDPageElement implements RenderCommand
{
    private final String name;

    private final String publicId;

    private final String systemId;

    public DTDPageElement(String name, String publicId, String systemId)
    {
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        writer.getDocument().dtd(name, publicId, systemId);
    }

    @Override
    public String toString()
    {
        return String.format("DTD[name=%s; publicId=%s; systemId=%s]", name, publicId, systemId);
    }
}
