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

import java.lang.annotation.Annotation;

import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.annotations.Value;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class ValueObjectProviderTest extends IOCTestCase
{
    @Test
    public void no_value_annotation()
    {
        SymbolSource symbolSource = mockSymbolSource();
        TypeCoercer coercer = mockTypeCoercer();
        AnnotationProvider annotationProvider = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();

        train_getAnnotation(annotationProvider, Value.class, null);

        replay();

        ValueObjectProvider provider = new ValueObjectProvider(symbolSource, coercer);

        assertNull(provider.provide(Runnable.class, annotationProvider, locator));

        verify();
    }

    @Test
    public void value_annotation_present()
    {
        SymbolSource symbolSource = mockSymbolSource();
        TypeCoercer coercer = mockTypeCoercer();
        AnnotationProvider annotationProvider = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();
        String annotationValue = "${foo}";
        String expanded = "Foo";
        Runnable coerced = mockRunnable();
        Value annotation = newMock(Value.class);

        train_getAnnotation(annotationProvider, Value.class, annotation);

        expect(annotation.value()).andReturn(annotationValue);

        train_expandSymbols(symbolSource, annotationValue, expanded);
        train_coerce(coercer, expanded, Runnable.class, coerced);

        replay();

        ValueObjectProvider provider = new ValueObjectProvider(symbolSource, coercer);

        assertSame(provider.provide(Runnable.class, annotationProvider, locator), coerced);

        verify();
    }

    protected final <T extends Annotation> void train_getAnnotation(
            AnnotationProvider annotationProvider, Class<T> annotationClass, T annotation)
    {
        expect(annotationProvider.getAnnotation(annotationClass)).andReturn(annotation)
                .atLeastOnce();
    }
}
