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

package org.apache.tapestry5.internal.beaneditor;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.ValidationConstraintGenerator;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ValidateAnnotationConstraintGeneratorTest extends InternalBaseTestCase
{
    @Test
    public void no_annotation()
    {
        PropertyConduit conduit = mockPropertyConduit();

        train_getAnnotation(conduit, Validate.class, null);

        replay();

        ValidationConstraintGenerator gen = new ValidateAnnotationConstraintGenerator();

        assertNull(gen.buildConstraints(Object.class, conduit));

        verify();
    }

    @Test
    public void single_constraint()
    {
        PropertyConduit conduit = mockPropertyConduit();
        Validate validate = newValidate("required");

        train_getAnnotation(conduit, Validate.class, validate);

        replay();

        ValidationConstraintGenerator gen = new ValidateAnnotationConstraintGenerator();

        assertEquals(gen.buildConstraints(Object.class, conduit), Arrays.asList("required"));

        verify();
    }

    @Test
    public void multiple_constraints()
    {
        PropertyConduit conduit = mockPropertyConduit();
        Validate validate = newValidate("required,minlength=3,regexp=^([a-zA-Z0-9]{2,4})+$");

        train_getAnnotation(conduit, Validate.class, validate);

        replay();

        ValidationConstraintGenerator gen = new ValidateAnnotationConstraintGenerator();

        assertEquals(gen.buildConstraints(null, conduit), Arrays.asList("required", "minlength=3", "regexp=^([a-zA-Z0-9]{2,4})+$"));

        verify();
    }
    

    @Test
    public void regex_ranges_constraints()
    {
        PropertyConduit conduit = mockPropertyConduit();
        Validate validate = newValidate("regexp=^([a]{50,125}[0-9]{2,4})+$,required,567matcher,regexp=a\\,b,regexp=a{1,}");

        train_getAnnotation(conduit, Validate.class, validate);

        replay();

        ValidationConstraintGenerator gen = new ValidateAnnotationConstraintGenerator();

        assertEquals(gen.buildConstraints(null, conduit), 
                Arrays.asList("regexp=^([a]{50,125}[0-9]{2,4})+$","required", "567matcher", "regexp=a\\,b", "regexp=a{1,}"));

        verify();
    }

    private Validate newValidate(String value)
    {
        Validate annotation = newMock(Validate.class);

        expect(annotation.value()).andReturn(value).atLeastOnce();

        return annotation;
    }
}
