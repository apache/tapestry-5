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

package org.apache.tapestry.internal.beaneditor;

import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
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
                {propertyNames, expected};
    }

    @DataProvider(name = "split_inputs")
    public Object[][] split_inputs()
    {
        return new Object[][]
                {build("fred", "fred"), build("fred,barney", "fred", "barney"),
                 build(" fred, barney, wilma, betty ", "fred", "barney", "wilma", "betty"),
                 new Object[]
                         {"   ", new String[0]}};
    }

    @Test
    public void remove()
    {
        BeanModel model = mockBeanModel();

        expect(model.remove("fred", "barney")).andReturn(model);

        replay();

        BeanModelUtils.remove(model, "fred,barney");
    }

    @Test
    public void reorder()
    {
        BeanModel model = mockBeanModel();

        expect(model.reorder("fred", "barney")).andReturn(model);

        replay();

        BeanModelUtils.reorder(model, "fred,barney");
    }
}
