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

import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.PropertyEditContext;
import org.testng.annotations.Test;

import java.util.Arrays;

public class MessagesAnnotationConstraintGeneratorTest extends InternalBaseTestCase
{

    @Test
    public void no_environment()
    {
        Environment e = getService(Environment.class);
        MessagesConstraintGenerator gen = new MessagesConstraintGenerator(e);
        assertNull(gen.buildConstraints(null,null));
    }

    @Test
    public void no_property()
    {
        Environment e = getService(Environment.class);

        pushAndTrainEnvironmentalObjects(e,false,null);

        MessagesConstraintGenerator gen = new MessagesConstraintGenerator(e);
        assertNull(gen.buildConstraints(null,null));

        pop(e);
        verify();
    }

    @Test
    public void empty_message()
    {
        Environment e = getService(Environment.class);

        pushAndTrainEnvironmentalObjects(e,true,"");

        MessagesConstraintGenerator gen = new MessagesConstraintGenerator(e);
        assertNull(gen.buildConstraints(null,null));

        pop(e);
        verify();
    }

    @Test
    public void single_constraint()
    {
        Environment e = getService(Environment.class);

        pushAndTrainEnvironmentalObjects(e,true,"required");

        MessagesConstraintGenerator gen = new MessagesConstraintGenerator(e);

        assertEquals(gen.buildConstraints(null,null), Arrays.asList("required"));
    }

    @Test
    public void multiple_constraints()
    {
        Environment e = getService(Environment.class);

        pushAndTrainEnvironmentalObjects(e,true,"required,minlength=3,regexp=^([a-zA-Z0-9]{2,4})+@(\\p{Lower})*$");

        MessagesConstraintGenerator gen = new MessagesConstraintGenerator(e);

        assertEquals(gen.buildConstraints(null,null),
                Arrays.asList("required","minlength=3","regexp=^([a-zA-Z0-9]{2,4})+@(\\p{Lower})*$"));

    }

    private void pushAndTrainEnvironmentalObjects(Environment e, boolean hasProperty, String propertyValue) {
        Messages messages = mockMessages();
        train_contains(messages,"testProperty-validate",hasProperty);

        if (hasProperty) {
            train_get(messages,"testProperty-validate",propertyValue);
        }

        EnvironmentMessages em = new EnvironmentMessages(messages,"testProperty");
        e.push(EnvironmentMessages.class,em);
        replay();
    }

    private void pop(Environment e) {
        e.pop(EnvironmentMessages.class);
    }

}
