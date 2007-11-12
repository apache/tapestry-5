// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import org.apache.tapestry.Binding;
import org.apache.tapestry.Translator;

/**
 * Used by certain form-control element component to obtain a default translator appropriate to the
 * type of property editted by the component.
 */
public interface TranslatorDefaultSource
{
    /**
     * Finds a {@link Translator} that is appropriate to the given type, which is usually obtained
     * via {@link Binding#getBindingType()}. Performs an inheritanced-based search for the best
     * match.
     *
     * @param valueType the type of value for which a default translator is needed
     * @return the matching translator
     * @throws IllegalArgumentException if no translator may be found
     */
    Translator find(Class valueType);
}
