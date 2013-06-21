// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.wro4j;

import org.slf4j.Logger;
import ro.isdc.wro.extensions.processor.js.NodeCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Creates either a {@link NodeCoffeeScriptProcessor} or a {@link RhinoCoffeeScriptProcessor}
 * and delegates the process() method to it. {@link ro.isdc.wro.extensions.processor.js.CoffeeScriptProcessor} should
 * do this, but doesn't work correctly inside Tapestry due to its home-grown injection system.
 */
public class NodeOrRhinoCoffeeScriptCompiler implements ResourcePreProcessor
{
    private final Logger logger;

    private final ResourcePreProcessor processor;

    public NodeOrRhinoCoffeeScriptCompiler(Logger logger)
    {
        this.logger = logger;
        processor = create();
    }

    private ResourcePreProcessor create()
    {
        NodeCoffeeScriptProcessor processor = new NodeCoffeeScriptProcessor();

        if (processor.isSupported())
        {
            logger.info("'coffee' command is available; using Node to compile CoffeeScript files.");
            return processor;
        }

        logger.info("'coffee' command is not available, using Rhino to compile CoffeeScript files.");

        return new RhinoCoffeeScriptProcessor();
    }

    public void process(Resource resource, Reader reader, Writer writer) throws IOException
    {
        processor.process(resource, reader, writer);
    }
}
