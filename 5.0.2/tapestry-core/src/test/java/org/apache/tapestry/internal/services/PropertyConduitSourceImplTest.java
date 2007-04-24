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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.internal.bindings.PropBindingFactoryTest;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.PropertyConduitSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Most of the testing occurs inside {@link PropBindingFactoryTest} (due to historical reasons).
 */
public class PropertyConduitSourceImplTest extends InternalBaseTestCase
{
    private PropertyConduitSource _source;

    @BeforeClass
    public void setup()
    {
        _source = getObject("infrastructure:PropertyConduitSource", PropertyConduitSource.class);
    }

    @AfterClass
    public void cleanup()
    {
        _source = null;
    }

    @Test
    public void question_dot_operator_for_object_type()
    {
        PropertyConduit normal = _source.create(CompositeBean.class, "simple.firstName");
        PropertyConduit smart = _source.create(CompositeBean.class, "simple?.firstName");

        CompositeBean bean = new CompositeBean();
        bean.setSimple(null);

        try
        {
            normal.get(bean);
            unreachable();
        }
        catch (NullPointerException ex)
        {
            // Expected.
        }

        assertNull(smart.get(bean));

        try
        {
            normal.set(bean, "Howard");
            unreachable();
        }
        catch (NullPointerException ex)
        {
            // Expected.
        }

        // This will be a no-op due to the null property in the expression

        smart.set(bean, "Howard");
    }
}
