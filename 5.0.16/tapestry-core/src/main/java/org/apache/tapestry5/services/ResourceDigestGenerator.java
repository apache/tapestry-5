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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

import java.net.URL;

/**
 * Responsible for determining which classpath resources require checksums, and for generating checksums for such
 * resources.
 * <p/>
 * The service's configuration identifies which file extensions will be secured using an checksum. The default list is
 * "class" and "tml".
 *
 * @see org.apache.tapestry5.ioc.internal.util.ClasspathResource
 * @see org.apache.tapestry5.internal.services.ClasspathAssetFactory
 */
@UsesConfiguration(String.class)
public interface ResourceDigestGenerator
{
    /**
     * Examines the path (typically, the file name extension at the end of the path) to determine if a checksum is
     * required for the path. The path is {@link org.apache.tapestry5.ioc.Resource} style, without a leading slash.
     */
    boolean requiresDigest(String path);

    /**
     * Reads the content of a URL (presumably, for a resource on the classpath) and generates a digest of its content.
     * This digest will be incorporated into the URL provided to the client, to verify that the client has been
     * "granted" access to this resource. This is only used for resources where {@link #requiresDigest(String)} is
     * true.
     *
     * @param url
     * @return the digest for the resource
     */
    String generateDigest(URL url);
}
