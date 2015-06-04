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

package org.apache.tapestry5.webresources;

public class WebResourcesSymbols
{
    /**
     * Directory that stores cached copies of compiled CoffeeScript files. The directory will be created
     * as necessary. This allows compilation (e.g., CoffeeScript to JavaScript) to be avoided after a restart.
     * The default is under the {@code java.io.tmpdir} system property (which is not necessarily stable between executions).
     * It is common to set this to {@code target/asset-cache} during development.
     */
    public static final String CACHE_DIR = "tapestry.compiled-asset-cache-dir";

    /**
     * Defines the level at which the Google Closure compiler operations, as per
     * {@link com.google.javascript.jscomp.CompilationLevel}.  The default is "WHITESPACE_ONLY", though
     * "SIMPLE_OPTIMIZATIONS" should also work. Advanced optimizations will likely not work, as Tapestry's more
     * incremental approach to JavaScript (including the use of jQuery and jQuery plugins) is not compatible
     * with Closure.
     */
    public static final String COMPILATION_LEVEL  = "tapestry.closure-compiler-level";
}
