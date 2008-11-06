// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * A source for {@link org.apache.tapestry5.Translator}s, either by name or by property type.
 * <p/>
 * The configuration includes all the translators; each contributed translator must have a unique {@linkplain
 * org.apache.tapestry5.Translator#getName() name}.
 */
@UsesConfiguration(Translator.class)
public interface TranslatorSource
{
    /**
     * Returns the translator with the given logical name.
     *
     * @param name name of translator (as configured)
     * @return the shared translator instance
     * @throws RuntimeException if no translator is configured for the provided name
     */
    Translator get(String name);

    /**
     * Finds a {@link Translator} that is appropriate to the given type, which is usually obtained via {@link
     * org.apache.tapestry5.Binding#getBindingType()}. Performs an inheritanced-based search for the best match.
     *
     * @param valueType the type of value for which a default translator is needed
     * @return the matching translator, or null if no match can be found
     */
    Translator findByType(Class valueType);

    /**
     * Finds a {@link Translator} that is appropriate to the given type, which is usually obtained via {@link
     * org.apache.tapestry5.Binding#getBindingType()}. Performs an inheritanced-based search for the best match.
     *
     * @param valueType the type of value for which a default translator is needed
     * @return the matching translator
     * @throws IllegalArgumentException if no known validator matches the provided type
     */
    Translator getByType(Class valueType);
}
