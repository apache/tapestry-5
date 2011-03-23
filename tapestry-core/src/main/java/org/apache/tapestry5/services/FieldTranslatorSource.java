//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Messages;

import java.util.Locale;

/**
 * For a particular field, generates the default {@link org.apache.tapestry5.FieldTranslator} for the field.
 */
public interface FieldTranslatorSource
{
    /**
     * Common shorthand for {@link #createDefaultTranslator(org.apache.tapestry5.Field, String,
     * org.apache.tapestry5.ioc.Messages, java.util.Locale, Class, org.apache.tapestry5.ioc.AnnotationProvider)}.
     *
     * @param resources     of component who owns the parameter
     * @param parameterName name of parameter used to determine the property type
     * @return field translator, or null
     */
    FieldTranslator createDefaultTranslator(ComponentResources resources, String parameterName);

    /**
     * Creates a {@link org.apache.tapestry5.FieldTranslator} for the given property, or returns null if one can't be
     * constructed. The return value is null if the property type is not known, or if there is no {@link
     * org.apache.tapestry5.Translator} available from the {@link org.apache.tapestry5.services.TranslatorSource} that
     * is appropriate for the property type.
     *
     * @param field               for which a translator is needed
     * @param overrideId          id used when looking in the overrideMessages for a message override
     * @param overrideMessages    location to look for overriding messages
     * @param locale              to localize validation messages to
     * @param propertyType        type of property editted by the field, used to select the Translator
     * @param propertyAnnotations annotations on the property (not currently used)
     * @return the field translator, or null
     */
    FieldTranslator createDefaultTranslator(Field field, String overrideId, Messages overrideMessages, Locale locale,
                                            Class propertyType, AnnotationProvider propertyAnnotations);

    /**
     * Wraps a {@link org.apache.tapestry5.Translator} as a FieldTranslator.
     */
    FieldTranslator createTranslator(Field field, String overrideId, Messages overrideMessages, Locale locale,
                                     Translator translator);

    /**
     * Creates a translator (used by the {@link org.apache.tapestry5.BindingConstants#TRANSLATE translate: binding
     * prefix}).
     */
    FieldTranslator createTranslator(ComponentResources componentResources, String translatorName);
}
