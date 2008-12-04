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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;

/**
 * Performs an injection based on a {@link Symbol} annotation.
 */
public class SymbolObjectProvider implements ObjectProvider
{
    private final SymbolSource symbolSource;

    private final TypeCoercer typeCoercer;

    public SymbolObjectProvider(@Builtin SymbolSource symbolSource,

                                @Builtin TypeCoercer typeCoercer)
    {
        this.symbolSource = symbolSource;
        this.typeCoercer = typeCoercer;
    }

    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
    {
        Symbol annotation = annotationProvider.getAnnotation(Symbol.class);

        if (annotation == null) return null;

        Object value = symbolSource.valueForSymbol(annotation.value());

        IntermediateType it = annotationProvider.getAnnotation(IntermediateType.class);

        if (it != null) value = typeCoercer.coerce(value, it.value());

        return typeCoercer.coerce(value, objectType);
    }

}
