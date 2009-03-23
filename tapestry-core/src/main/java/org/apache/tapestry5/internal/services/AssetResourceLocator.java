// Copyright 2009 The Apache Software Foundation
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

import java.io.IOException;

/**
 * Responsible for converting a path (from {@link org.apache.tapestry5.internal.services.AssetDispatcher} into a {@link
 * org.apache.tapestry5.ioc.Resource} that can be {@linkplain org.apache.tapestry5.internal.services.ResourceStreamer
 * streamed to the client}.
 *
 * @since 5.1.0.2
 */
public interface AssetResourceLocator
{
    /**
     * Analyzes the path and identifies the underying Asset Resource for that path. Handles context resources and
     * checking for a digest.
     *
     * @param path to translate
     * @return resource corresponding to path (may be for a non-existent resource), or null if path is invalid (i.e.,
     *         incorrect digest)
     */
    Resource findResourceForPath(String path) throws IOException;
}
