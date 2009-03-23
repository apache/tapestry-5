// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

/**
 * Constants used when processing requests from the client web browser.
 */
public final class RequestConstants
{

    /**
     * Request path prefix that identifies an internal (on the classpath) asset.
     */
    public static final String ASSET_PATH_PREFIX = "/assets/";


    /**
     * Virtual folder name for assets that are actually stored in the context, but are exposed (much like classpath
     * assets) to gain far-future expires headers and automatic content compression. The application version number
     * comes after this prefix and before the true path.
     *
     * @since 5.1.0.0
     */
    public static final String CONTEXT_FOLDER = "ctx/";


    /**
     * Folder for virtual assets: combined JavaScript files. The file name is actualy a compressed bytestream
     * of the names of each file.
     *
     * @since 5.1.0.2
     */
    public static final String VIRTUAL_FOLDER = "virtual/";
}
