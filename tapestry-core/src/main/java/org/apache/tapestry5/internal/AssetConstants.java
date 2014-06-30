// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Defines constants for the two basic asset prefixes.
 * 
 * @since 5.2.0
 */
public class AssetConstants
{
    /** For assets that are stored in the web application context. */
    public static final String CONTEXT = "context";

    /** For assets that are stored in the classpath (i.e., inside 3rd party component library JARs). */
    public static final String CLASSPATH = "classpath";

    /** For assets that are external (stored in another service) and have a non-secure URL (http://...) */
    public static final String HTTP = "http";

    /** For assets that are external (stored in another service) and have a secure URL (https://...) */
    public static final String HTTPS = "https";

    /** For assets that are external (stored in another service) and have a protocol-relative URL (//...) */
    public static final String PROTOCOL_RELATIVE = "//";

    /** For assets that are external (stored in another service) and stored in a publicly available FTP server (ftp://...) */
    public static final String FTP = "ftp";

}
