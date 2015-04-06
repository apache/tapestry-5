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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.util.Map;

/**
 * This service is used by {@link TranslatorSource} to specify {@link Translator} <em>alternates</em>: translators that
 * are used when specified explicitly by name. These translators may overlap the standard translators
 * by type (thus requiring a separate configuration).
 *
 * Translators contributed to this configuration must have names that do not overlap the standard translators. Further,
 * the contribution key must match the {@linkplain Translator#getName() translator name}.
 * 
 * @since 5.2.0
 */
@UsesMappedConfiguration(Translator.class)
public interface TranslatorAlternatesSource
{
    /**
     * Get the mapping from name to Translator, based on the contributions to the service. It will be verified
     * that the keys of the map corresponding to the names of the Translator values.
     */
    Map<String, Translator> getTranslatorAlternates();
}
