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

import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.slf4j.Logger;
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor;
import ro.isdc.wro.extensions.processor.support.coffeescript.CoffeeScript;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.*;

/**
 * Compiles CoffeeScript to JavaScript, using {@link RhinoCoffeeScriptProcessor}. Because what is most commonly written
 * are AMD Modules, which have (effectively) an implicit hygenic function wrapper, we compile as with "--bare".
 *
 * @since 5.4
 */
public class CoffeeScriptResourceCompiler implements ResourceTransformer
{
    private final Logger logger;

    private final OperationTracker tracker;

    private static final double NANOS_TO_MILLIS = 1.0d / 1000000.0d;

    private final ResourcePreProcessor compiler =
            new RhinoCoffeeScriptProcessor()
            {

                @Override
                protected CoffeeScript newCoffeeScript()
                {
                    return new CoffeeScript().setOptions("bare");
                }
            };

    public CoffeeScriptResourceCompiler(Logger logger, OperationTracker tracker)
    {
        this.logger = logger;
        this.tracker = tracker;
    }

    public String getTransformedContentType()
    {
        return "text/javascript";
    }


    public InputStream transform(final Resource source, ResourceDependencies dependencies) throws IOException
    {

        return tracker.perform(String.format("Compiling %s from CoffeeScript to JavaScript", source),
                new IOOperation<InputStream>()
                {
                    public InputStream perform() throws IOException
                    {
                        final long startTime = System.nanoTime();


                        ro.isdc.wro.model.resource.Resource res = ro.isdc.wro.model.resource.Resource.create(
                                source.toURL().toString(),
                                ResourceType.JS);

                        Reader reader = new InputStreamReader(source.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream(5000);
                        Writer writer = new OutputStreamWriter(out);

                        compiler.process(res, reader, writer);

                        final long elapsedTime = System.nanoTime() - startTime;


                        logger.info(String.format("Compiled %s to JavaScript in %.2f ms",
                                source,
                                ((double) elapsedTime) * NANOS_TO_MILLIS));

                        return new BytestreamCache(out).openStream();
                    }
                });
    }
}
