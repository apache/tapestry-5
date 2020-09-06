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
package org.apache.tapestry5.http.internal;


public class TapestryHttpInternalConstants {

    /**
     * Init parameter used to identify the package from which application classes are loaded. Such
     * classes are in the
     * pages, components and mixins sub-packages.
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = "tapestry.app-package";
    
    /**
     * Turns off loading of default modules (as driven by JAR file Manifest entries).
     */
    public static final String DISABLE_DEFAULT_MODULES_PARAM = "tapestry.disable-default-modules";

    public static final String CHARSET_CONTENT_TYPE_PARAMETER = "charset";

    /**
     * Name of response header for content encoding.
     *
     * @since 5.1.0.0
     */
    public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";

    /**
     * Response content encoding value indicating use of GZIP compression.
     *
     * @since 5.1.0.0
     */
    public static final String GZIP_CONTENT_ENCODING = "gzip";

}
