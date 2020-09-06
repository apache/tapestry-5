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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.ContentType;

import java.io.IOException;
import java.io.InputStream;

/**
 * A transformer is used to read a {@link Resource} and pass it through a transformation stage, to get a
 * stream that can be used on the client side. Examples of this are languages that "compile" to
 * JavaScript, or any of a few higher-level versions of CSS that are compiled to standard CSS.
 * ResourceTransformers are contributed to the {@link StreamableResourceSource} service.
 *
 * @since 5.3
 * @see StreamableResourceSource
 */
public interface ResourceTransformer
{
    /**
     * Returns the MIME type of a transformed stream.
     *
     * @since 5.4
     */
    ContentType getTransformedContentType();

    /**
     * Read the source input stream and provide a new input stream of the transformed content.
     *
     * @param source
     *         input content
     * @param dependencies
     *         allows additional dependencies of the source to be tracked
     * @return stream of output content
     * @throws IOException
     */
    InputStream transform(Resource source, ResourceDependencies dependencies) throws IOException;
}
