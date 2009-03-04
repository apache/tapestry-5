// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.io.IOException;

/**
 * Responsible for streaming the contents of a resource to the client. The {@link org.apache.tapestry5.ioc.Resource} to
 * stream is almost always a {@link org.apache.tapestry5.ioc.internal.util.ClasspathResource}.
 * <p/>
 * The service's configuration is used to map file extensions to content types. Note: this only works for simple
 * extensions (i.e., "jpg") not for complex extensions (i.e., "tar.gz").
 *
 * @since 5.1.0.0
 */
@UsesMappedConfiguration(String.class)
public interface ResourceStreamer
{
    /**
     * Streams the content of the resource to the client.
     */
    void streamResource(Resource resource) throws IOException;

    /**
     * Analyzes the resource to determine what its content type is, possibly using the service's configuration.
     *
     * @param resource to analyze
     * @return content type
     * @throws IOException
     */
    String getContentType(Resource resource) throws IOException;
}
