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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.javascript.JavaScriptAggregationStrategy;

import java.io.IOException;

/**
 * Assembles the individual assets of a {@link org.apache.tapestry5.services.javascript.JavaScriptStack} into
 * a single resource; this is needed to generate a checksum for the aggregated assets, and also to service the
 * aggregated stack content.
 *
 * @see org.apache.tapestry5.SymbolConstants#COMBINE_SCRIPTS
 * @since 5.4
 */
public interface JavaScriptStackAssembler
{
    /**
     * Obtains the {@link org.apache.tapestry5.services.javascript.JavaScriptStack} by name, and then
     * uses the {@link org.apache.tapestry5.services.assets.StreamableResourceSource} service to
     * obtain the assets, which are combined together.
     *
     * Expects the {@linkplain org.apache.tapestry5.services.LocalizationSetter#setNonPersistentLocaleFromLocaleName(String) non-persistent locale} to be set before invoking!
     */
    StreamableResource assembleJavaScriptResourceForStack(String stackName, boolean compress, JavaScriptAggregationStrategy javascriptAggregationStrategy) throws IOException;
}
