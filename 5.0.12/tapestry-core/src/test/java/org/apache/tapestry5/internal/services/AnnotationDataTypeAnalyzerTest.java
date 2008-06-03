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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.beaneditor.DataType;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class AnnotationDataTypeAnalyzerTest extends TapestryTestCase
{
    private DataType mockDataType(String annotationValue)
    {
        DataType annotation = newMock(DataType.class);

        expect(annotation.value()).andReturn(annotationValue).atLeastOnce();

        return annotation;
    }

    @Test
    public void annotation_absent()
    {
        PropertyAdapter adapter = mockPropertyAdapter();

        train_getAnnotation(adapter, DataType.class, null);

        replay();

        DataTypeAnalyzer analyzer = new AnnotationDataTypeAnalyzer();

        assertNull(analyzer.identifyDataType(adapter));

        verify();
    }

    @Test
    public void value_from_annotation()
    {
        String value = "password";
        PropertyAdapter adapter = mockPropertyAdapter();

        train_getAnnotation(adapter, DataType.class, mockDataType(value));

        replay();

        DataTypeAnalyzer analyzer = new AnnotationDataTypeAnalyzer();

        assertEquals(analyzer.identifyDataType(adapter), value);

        verify();

    }
}
