// Copyright 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.io.IOException;

import org.apache.tapestry5.ioc.Resource;

/**
 * Responsible for converting a path into a {@link org.apache.tapestry5.ioc.Resource} that can be
 * {@linkplain org.apache.tapestry5.internal.services.ResourceStreamer
 * streamed to the client}. The path is on the classpath, but may have been changed to include a checksum (in
 * certain cases).
 * 
 * @since 5.1.0.2
 */
public interface AssetResourceLocator
{
    /**
     * For a complete classpath path, returns the Resource for the path. This include checking for a
     * digest for protected files.
     * 
     * @return
     *         resource corresponding to path (may be for a non-existent resource), or null if path is invalid (i.e.,
     *         incorrect digest)
     */
    Resource findClasspathResourceForPath(String path) throws IOException;
}
