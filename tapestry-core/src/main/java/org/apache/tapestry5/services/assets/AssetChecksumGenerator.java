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

package org.apache.tapestry5.services.assets;

import java.io.IOException;

import org.apache.tapestry5.commons.Resource;

/**
 * Generates a checksum of an arbitrary {@link org.apache.tapestry5.commons.Resource} or {@link StreamableResource} which can be incorporated into
 * the {@linkplain org.apache.tapestry5.Asset#toClientURL() client URL} of an Asset.
 *
 * @since 5.4
 */
public interface AssetChecksumGenerator
{
    /**
     * Given a raw resource, generates an MD5 checksum of the resource's contents.
     *
     * @param resource
     * @return checksum of contents
     * @throws IOException
     */
    String generateChecksum(Resource resource) throws IOException;

    /**
     * Given a streamable resource, generates an MD5 checksum of the resource's contents.
     *
     * @param resource
     * @return checksum of contents
     * @throws IOException
     */
    String generateChecksum(StreamableResource resource) throws IOException;
}
