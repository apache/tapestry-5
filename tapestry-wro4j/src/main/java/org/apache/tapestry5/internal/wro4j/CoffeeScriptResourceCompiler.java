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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import org.slf4j.Logger;
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Compiles CoffeeScript to JavaScript, using {@link RhinoCoffeeScriptProcessor}. Because what is most commonly written
 * are AMD Modules, which have (effectively) an implicit hygienic function wrapper, we compile as with "--bare".
 *
 * @since 5.4
 */
public class CoffeeScriptResourceCompiler implements ResourceTransformer
{
    private final Logger logger;

    private static final double NANOS_TO_MILLIS = 1.0d / 1000000.0d;

    private final ResourceProcessor compiler;

    public CoffeeScriptResourceCompiler(Logger logger, ResourceProcessorSource processorSource)
    {
        this.logger = logger;

        // Could set up some special kind of injection for this, but overkill for the couple of places it is used.
        compiler = processorSource.getProcessor("CoffeeScriptCompiler");
    }

    public String getTransformedContentType()
    {
        return "text/javascript";
    }

    public InputStream transform(final Resource source, ResourceDependencies dependencies) throws IOException
    {
        final long startTime = System.nanoTime();

        InputStream result = compiler.process(String.format("Compiling %s from CoffeeScript to JavaScript", source),
                source.toURL().toString(),
                source.openStream(), "text/javascript");

        final long elapsedTime = System.nanoTime() - startTime;

        logger.info(String.format("Compiled %s to JavaScript in %.2f ms",
                source,
                ((double) elapsedTime) * NANOS_TO_MILLIS));

        return result;
    }
}
