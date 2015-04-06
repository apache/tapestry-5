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

package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * Used to perform a pre-load of pages, at startup time. This helps ensure that the first actual
 * request is processed promptly.
 *
 * The configuration is simply the logical names of pages to load initially.
 *
 * Pages are loaded in the default locale (the first locale listed in
 * {@link org.apache.tapestry5.SymbolConstants#SUPPORTED_LOCALES}). This ensures that the majority
 * of class loading and transformation, template parsing, and so forth occurs immediately (loading
 * an existing page in a different locale is a relatively inexpensive operation compared to the
 * first load of the page).
 *
 * @since 5.4
 */
@UsesConfiguration(String.class)
public interface PagePreloader
{
    /**
     * Loads any pages, subject to the {@linkplain org.apache.tapestry5.SymbolConstants#PRELOADER_MODE preloader mode}.
     */
    void preloadPages();
}
