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
import org.apache.tapestry5.ioc.annotations.IntermediateType;
import org.apache.tapestry5.ioc.annotations.Value;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.math.BigDecimal;

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
        Value annotation = newValue(annotationValue);

        train_getAnnotation(annotationProvider, Value.class, annotation);

        train_getAnnotation(annotationProvider, IntermediateType.class, null);

        train_expandSymbols(symbolSource, annotationValue, expanded);
        train_coerce(coercer, expanded, Runnable.class, coerced);

        replay();

        ValueObjectProvider provider = new ValueObjectProvider(symbolSource, coercer);

        assertSame(provider.provide(Runnable.class, annotationProvider, locator), coerced);

        verify();
    }

    @Test
    public void intermediate_type()
    {
        SymbolSource symbolSource = mockSymbolSource();
        TypeCoercer coercer = mockTypeCoercer();
        AnnotationProvider annotationProvider = mockAnnotationProvider();
        ObjectLocator locator = mockObjectLocator();
        String annotationValue = "${foo}";
        String expanded = "Foo";
        Runnable coerced = mockRunnable();
        Value annotation = newValue(annotationValue);
        IntermediateType it = newIntermediateType();
        BigDecimal intervalue = new BigDecimal("1234");

        train_getAnnotation(annotationProvider, Value.class, annotation);

        train_getAnnotation(annotationProvider, IntermediateType.class, it);

        train_value(it, BigDecimal.class);

        train_expandSymbols(symbolSource, annotationValue, expanded);
        train_coerce(coercer, expanded, BigDecimal.class, intervalue);
        train_coerce(coercer, intervalue, Runnable.class, coerced);

        replay();

        ValueObjectProvider provider = new ValueObjectProvider(symbolSource, coercer);

        assertSame(provider.provide(Runnable.class, annotationProvider, locator), coerced);

        verify();
    }

    private Value newValue(String value)
    {
        Value annotation = newMock(Value.class);

        expect(annotation.value()).andReturn(value);

        return annotation;
    }


}
