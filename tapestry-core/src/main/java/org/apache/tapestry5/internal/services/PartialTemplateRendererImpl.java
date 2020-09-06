// Copyright 2014, The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.PartialTemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartialTemplateRendererImpl implements PartialTemplateRenderer
{
    
    final private static Logger LOGGER = LoggerFactory.getLogger(PartialTemplateRendererImpl.class);
    
    final private TypeCoercer typeCoercer;

    public PartialTemplateRendererImpl(TypeCoercer typeCoercer)
    {
        super();
        this.typeCoercer = typeCoercer;
    }

    public Document renderAsDocument(Object object)
    {
        RenderCommand renderCommand = toRenderCommand(object);
        MarkupWriter markupWriter = new MarkupWriterImpl(); 
        RenderQueueImpl renderQueue = new RenderQueueImpl(LOGGER); 
        renderQueue.push(renderCommand); 
        renderQueue.run(markupWriter);
        return markupWriter.getDocument();
    }

    public String render(Object object)
    {
        return renderAsDocument(object).toString();
    }

    private RenderCommand toRenderCommand(Object object)
    {
        RenderCommand renderCommand = null;
        if (object instanceof RenderCommand)
        {
            renderCommand = (RenderCommand) object;
        }
        else {
            try {
                renderCommand = typeCoercer.coerce(object, RenderCommand.class); 
            }
            catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        String.format("Couldn't find a coercion from %s to RenderCommand", object.getClass().getName()), e);
            }
        }
        return renderCommand;
    }

}