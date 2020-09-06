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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.internal.services.assets.BytestreamCache;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.ResourceTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Direct wrapper around the LessCompiler, so that Less source files may use {@code @import}, which isn't
 * supported by the normal WRO4J processor.
 */
public class LessResourceTransformer implements ResourceTransformer
{
    private static final ContentType CSS = new ContentType("text/css");

    private final LessCompiler compiler = new DefaultLessCompiler();

    @Override
    public ContentType getTransformedContentType()
    {
        return CSS;
    }


    @Override
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

            LessCompiler.CompilationResult compilationResult = compile(compiler, lessSource);

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

    /**
     * Invoked from {@link #transform(org.apache.tapestry5.commons.Resource, org.apache.tapestry5.services.assets.ResourceDependencies)}
     * to perform the actual work of compiling a {@link org.apache.tapestry5.commons.Resource} which has been wrapped as a
     * {@link com.github.sommeri.less4j.LessSource}.
     */
    protected LessCompiler.CompilationResult compile(LessCompiler compiler, LessSource lessSource) throws Less4jException
    {
        return compiler.compile(lessSource);
    }
}