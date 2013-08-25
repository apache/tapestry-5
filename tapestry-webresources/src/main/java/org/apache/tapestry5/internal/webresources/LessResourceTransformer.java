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

package org.apache.tapestry5.internal.webresources;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;

import java.io.*;

/**
 * Direct wrapper around the LessCompiler, so that Less source files may use {@code @import}, which isn't
 * supported by the normal WRO4J processor.
 */
public class LessResourceTransformer implements ResourceTransformer
{
    private final LessCompiler compiler = new DefaultLessCompiler();

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


    public InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException
    {
        BytestreamCache compiled = invokeLessCompiler(source, dependencies);

        return compiled.openStream();
    }

    private BytestreamCache invokeLessCompiler(Resource source, ResourceDependencies dependencies) throws IOException
    {
        try
        {
            LessSource lessSource = new ResourceLessSource(source, dependencies);

            LessCompiler.CompilationResult compilationResult = compiler.compile(lessSource);

            // Currently, ignoring any warnings.

            return new BytestreamCache(compilationResult.getCss().getBytes("utf-8"));

        } catch (Less4jException ex)
        {
            throw new IOException(ex);
        } catch (UnsupportedEncodingException ex)
        {
            throw new IOException(ex);
        }
    }
}