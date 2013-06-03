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

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;
import org.slf4j.Logger;

import java.io.*;
import java.util.Map;

/**
 * Direct wrapper around the LessCompiler, so that Less source files may use {@code @import}, which isn't
 * supported by the normal WRO4J processor.
 */
public class LessResourceTransformer implements ResourceTransformer
{
    private final LessCompiler compiler = new DefaultLessCompiler();

    private final OperationTracker tracker;

    private final Logger logger;

    private final boolean cacheEnabled;

    private final Map<Resource, Cached> cache = CollectionFactory.newConcurrentMap();

    static class Cached
    {
        final BytestreamCache compiled;

        final ContentChangeTracker tracker;

        Cached(BytestreamCache compiled, ContentChangeTracker tracker)
        {
            this.compiled = compiled;
            this.tracker = tracker;
        }

        InputStream openStream() throws IOException
        {
            return compiled.openStream();
        }
    }


    public LessResourceTransformer(OperationTracker tracker, Logger logger, @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        this.tracker = tracker;
        this.logger = logger;

        cacheEnabled = !productionMode;
    }

    public String getTransformedContentType()
    {
        return "text/css";
    }

    class ResourceLessSource extends LessSource
    {
        private final Resource resource;

        private final ResourceDependencies dependencies;


        ResourceLessSource(Resource resource, ResourceDependencies dependencies)
        {
            this.resource = resource;
            this.dependencies = dependencies;
        }

        @Override
        public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile, StringSourceException
        {
            Resource relative = resource.forFile(filename);

            if (!relative.exists())
            {
                throw new FileNotFound();
            }

            dependencies.addDependency(relative);

            return new ResourceLessSource(relative, dependencies);
        }

        @Override
        public String getContent() throws FileNotFound, CannotReadFile
        {
            // Adapted from Less's URLSource
            try
            {
                Reader input = new InputStreamReader(resource.openStream());
                String content = IOUtils.toString(input).replace("\r\n", "\n");

                input.close();

                return content;
            } catch (FileNotFoundException ex)
            {
                throw new FileNotFound();
            } catch (IOException ex)
            {
                throw new CannotReadFile();
            }
        }
    }


    public InputStream transform(final Resource source, final ResourceDependencies dependencies) throws IOException
    {
        if (cacheEnabled)
        {
            Cached cached = cache.get(source);

            if (cached != null && !cached.tracker.changes())
            {
                logger.info(String.format("Resource %s (and any dependencies) are unchanged; serving compiled Less content from cache",
                        source));

                return cached.openStream();
            }

            ContentChangeTracker tracker1 = new ContentChangeTracker();
            tracker1.addDependency(source);

            BytestreamCache compiled = invokeLessCompiler(source, new ResourceDependenciesSplitter(dependencies, tracker1));

            cached = new Cached(compiled, tracker1);

            cache.put(source, cached);

            return cached.openStream();
        } else
        {

            BytestreamCache compiled = invokeLessCompiler(source, dependencies);

            return compiled.openStream();
        }
    }

    private BytestreamCache invokeLessCompiler(final Resource source, final ResourceDependencies dependencies) throws IOException
    {

        return tracker.perform(String.format("Compiling %s from Less to CSS", source), new IOOperation<BytestreamCache>()
        {
            public BytestreamCache perform() throws IOException
            {
                long start = System.nanoTime();

                try
                {
                    LessSource lessSource = new ResourceLessSource(source, dependencies);

                    LessCompiler.CompilationResult compilationResult = compiler.compile(lessSource);

                    BytestreamCache result = new BytestreamCache(compilationResult.getCss().getBytes("utf-8"));

                    logger.info(String.format("Compiled %s to Less in %.2f ms",
                            source, ResourceTransformUtils.nanosToMillis(System.nanoTime() - start)));

                    // Currently, ignoring any warnings.

                    return result;

                } catch (Less4jException ex)
                {
                    throw new IOException(ex);
                } catch (UnsupportedEncodingException ex)
                {
                    throw new IOException(ex);
                }

            }
        });
    }
}
