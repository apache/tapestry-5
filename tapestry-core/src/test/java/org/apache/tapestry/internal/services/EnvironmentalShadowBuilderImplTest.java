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

import org.apache.tapestry.RenderSupport;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.EnvironmentalShadowBuilder;
import org.testng.annotations.Test;

public class EnvironmentalShadowBuilderImplTest extends InternalBaseTestCase
{
    @Test
    public void proxy_class()
    {
        RenderSupport delegate = newMock(RenderSupport.class);
        ClassFactory factory = new ClassFactoryImpl();
        Environment env = mockEnvironment();

        train_peekRequired(env, RenderSupport.class, delegate);

        expect(delegate.allocateClientId("fred")).andReturn("barney");

        replay();

        EnvironmentalShadowBuilder builder = new EnvironmentalShadowBuilderImpl(factory, env);

        RenderSupport proxy = builder.build(RenderSupport.class);

        assertEquals(proxy.allocateClientId("fred"), "barney");

        verify();
    }
}
