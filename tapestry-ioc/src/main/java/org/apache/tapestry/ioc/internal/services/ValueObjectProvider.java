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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.annotations.Value;
import org.apache.tapestry.ioc.services.Builtin;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.TypeCoercer;

/**
 * Provides an object when the {@link Value} annotation is present. The string value has symbols
 * expanded, and then is {@link TypeCoercer coerced} to the associated type.
 */
public class ValueObjectProvider implements ObjectProvider
{
    private final SymbolSource _symbolSource;

    private final TypeCoercer _typeCoercer;

    public ValueObjectProvider(@Builtin SymbolSource symbolSource,

                               @Builtin TypeCoercer typeCoercer)
    {
        _symbolSource = symbolSource;
        _typeCoercer = typeCoercer;
    }

    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
    {
        Value annotation = annotationProvider.getAnnotation(Value.class);

        if (annotation == null) return null;

        String value = annotation.value();
        String expanded = _symbolSource.expandSymbols(value);

        return _typeCoercer.coerce(expanded, objectType);
    }
}
