// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.easymock.EasyMock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BeanModelUtilsTest extends InternalBaseTestCase
{
    @Test(dataProvider = "split_inputs")
    public void split(String propertyNames, String[] expected)
    {
        assertEquals(BeanModelUtils.split(propertyNames), expected);
    }

    private Object[] build(String propertyNames, String... expected)
    {
        return new Object[]
                { propertyNames, expected };
    }

    @DataProvider
    public Object[][] split_inputs()
    {
        return new Object[][]
                { build("fred", "fred"), build("fred,barney", "fred", "barney"),
                        build(" fred, barney, wilma, betty ", "fred", "barney", "wilma", "betty"),
                        new Object[]
                                { "   ", new String[0] } };
    }

    @Test
    public void exclude()
    {
        BeanModel model = mockBeanModel();

        expect(model.exclude("fred", "barney")).andReturn(model);

        replay();

        BeanModelUtils.exclude(model, "fred,barney");

        verify();
    }

    @Test
    public void reorder()
    {
        BeanModel model = mockBeanModel();

        expect(model.reorder("fred", "barney")).andReturn(model);

        replay();

        BeanModelUtils.reorder(model, "fred,barney");

        verify();
    }

    @Test
    public void add()
    {
        BeanModel model = mockBeanModel();
        PropertyModel fred = mockPropertyModel();
        PropertyModel barney = mockPropertyModel();

        expect(model.add("fred", null)).andReturn(fred);
        expect(model.add("barney", null)).andReturn(barney);

        replay();

        BeanModelUtils.add(model, "fred,barney");

        verify();
    }

    /**
     * TAP5-478
     */
    @Test
    public void include_before_add()
    {
        BeanModel model = mockBeanModel();
        PropertyModel fred = mockPropertyModel();

        EasyMock.checkOrder(model, true);

        expect(model.add("fred", null)).andReturn(fred);

        expect(model.include("sam", "fred")).andReturn(model);

        replay();

        BeanModelUtils.modify(model, "fred", "sam", null, null);

        verify();
    }

    @Test
    public void modify_no_work()
    {
        BeanModel model = mockBeanModel();

        replay();

        BeanModelUtils.modify(model, null, null, null, null);

        verify();
    }

    @Test
    public void modify_full()
    {
        BeanModel model = mockBeanModel();
        PropertyModel fred = mockPropertyModel();
        PropertyModel barney = mockPropertyModel();

        expect(model.add("fred", null)).andReturn(fred);
        expect(model.add("barney", null)).andReturn(barney);

        expect(model.exclude("pebbles", "bambam")).andReturn(model);

        expect(model.reorder("wilma", "betty")).andReturn(model);

        replay();

        BeanModelUtils.modify(model, "fred,barney", null, "pebbles,bambam", "wilma,betty");

        verify();
    }

    @Test
    public void modify_include()
    {
        BeanModel model = mockBeanModel();

        expect(model.include("fred", "wilma")).andReturn(model);

        replay();

        BeanModelUtils.modify(model, null, "fred,wilma", null, null);

        verify();
    }
}
