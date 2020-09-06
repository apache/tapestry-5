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
import org.apache.tapestry5.commons.util.StrategyRegistry;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * A source for {@link org.apache.tapestry5.Translator}s, either by name or by property type. The source knows
 * about two sets of translators: the <em>standard</em> translators contributed directly to the service
 * and the <em>alternate</em> translators, contributed to the {@link TranslatorAlternatesSource} service.
 *
 * Each contributed translator must have a unique {@linkplain org.apache.tapestry5.Translator#getName() name}.
 *
 * Generally, Translators are matched by type (i.e., the type matching a particular property that will be read or
 * updated). Contributions to this service use a {@link StrategyRegistry} to match by type. Translators can also be
 * selected by name. The {@link TranslatorAlternatesSource} service configuration is often used for this purpose.
 *
 * The contribution key must match the {@linkplain Translator#getType() translator type}.
 */
@UsesMappedConfiguration(key=Class.class, value=Translator.class)
@SuppressWarnings("unchecked")
public interface TranslatorSource
{
    /**
     * Returns the translator with the given name (either a standard translator, or an alternate).
     * 
     * @param name
     *            name of translator (as configured, but case is ignored)
     * @return the shared translator instance
     * @throws RuntimeException
     *             if no translator is configured for the provided name
     */
    Translator get(String name);

    /**
     * Finds a {@link Translator} that is appropriate to the given type, which is usually obtained via
     * {@link org.apache.tapestry5.Binding#getBindingType()}. Performs an inheritance-based search for the best match,
     * among the <em>standard</em> translator (not alternates).
     * 
     * @param valueType
     *            the type of value for which a default translator is needed
     * @return the matching translator, or null if no match can be found
     */
    Translator findByType(Class valueType);

    /**
     * Finds a {@link Translator} that is appropriate to the given type, which is usually obtained via
     * {@link org.apache.tapestry5.Binding#getBindingType()}. Performs an inheritance-based search for the best match,
     * among the <em>standard</em> translators (not alternates).
     * 
     * @param valueType
     *            the type of value for which a default translator is needed
     * @return the matching translator
     * @throws IllegalArgumentException
     *             if no standard validator matches the provided type
     */
    Translator getByType(Class valueType);
}
