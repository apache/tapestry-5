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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class PageActivationContextCollectorImplTest extends InternalBaseTestCase
{
    private TypeCoercer coercer;

    @BeforeClass
    public void setup()
    {
        coercer = getService(TypeCoercer.class);
    }

    @Test
    public void page_with_array_activation_context()
    {
        tryWithContext(new Object[] {3, "four", null, "$100"},
                       3, "four", null, "$100");
    }

    @Test
    public void page_with_single_value_context()
    {
        Object value = 99;

        tryWithContext(value, value);
    }

    @Test
    public void page_with_list_context()
    {
        tryWithContext(Arrays.asList(1, 2, 3),
                       1, 2, 3);
    }

    @Test
    public void page_with_empty_context()
    {
        tryWithContext(new String[0]);
    }

    @Test
    public void page_with_no_context()
    {
        Page page = mockPage();
        ComponentPageElement element = mockComponentPageElement();

        train_getRootElement(page, element);

        expect(element.triggerEvent(EasyMock.eq(EventConstants.PASSIVATE),
                                    (Object[]) EasyMock.isNull(),
                                    EasyMock.isA(ComponentEventCallback.class))).andReturn(false);

        replay();

        PageActivationContextCollector collector
                = new PageActivationContextCollectorImpl(coercer);

        Object[] actual = collector.collectPageActivationContext(page);

        assertEquals(actual.length, 0);
    }

    private void tryWithContext(final Object context, Object... expected)
    {
        Page page = mockPage();
        ComponentPageElement element = mockComponentPageElement();

        train_getRootElement(page, element);

        IAnswer answer = new IAnswer()
        {
            public Object answer() throws Throwable
            {
                Object[] args = EasyMock.getCurrentArguments();

                ComponentEventCallback callback = (ComponentEventCallback) args[2];

                return callback.handleResult(context);
            }
        };

        expect(element.triggerEvent(EasyMock.eq(EventConstants.PASSIVATE),
                                    (Object[]) EasyMock.isNull(),
                                    EasyMock.isA(ComponentEventCallback.class))).andAnswer(answer);

        replay();

        PageActivationContextCollector collector
                = new PageActivationContextCollectorImpl(coercer);

        Object[] actual = collector.collectPageActivationContext(page);

        assertArraysEqual(actual, expected);
    }
}
