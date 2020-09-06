// Copyright 2007, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.EnvironmentalShadowBuilder;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.testng.annotations.Test;

public class EnvironmentalShadowBuilderImplTest extends InternalBaseTestCase
{
    @Test
    public void proxy_class()
    {
        JavaScriptSupport delegate = newMock(JavaScriptSupport.class);
        Environment env = mockEnvironment();

        train_peekRequired(env, JavaScriptSupport.class, delegate);

        expect(delegate.allocateClientId("fred")).andReturn("barney");

        replay();

        EnvironmentalShadowBuilder builder = new EnvironmentalShadowBuilderImpl(getService("PlasticProxyFactory",
                PlasticProxyFactory.class), env);

        JavaScriptSupport proxy = builder.build(JavaScriptSupport.class);

        assertEquals(proxy.allocateClientId("fred"), "barney");

        verify();
    }
}
