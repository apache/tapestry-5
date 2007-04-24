// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.testng.Assert;

/**
 * Used by {@link org.apache.tapestry.ioc.internal.ServiceBuilderMethodInvokerTest}.
 * 
 * 
 */
public class ServiceBuilderMethodFixture extends Assert
{
    FieService _fie;

    String _expectedServiceId;

    ServiceResources _expectedServiceResources;

    Class _expectedServiceInterface;

    Log _expectedLog;

    FoeService _expectedFoe;

    Object _expectedConfiguration;

    public FieService buildWithUnorderedConfiguration(Collection<Runnable> configuration)
    {
        assertSame(configuration, _expectedConfiguration);

        return _fie;
    }

    public FieService buildWithOrderedConfiguration(List<Runnable> configuration)
    {
        assertSame(configuration, _expectedConfiguration);

        return _fie;
    }

    public void methodWithParameterizedList(List<Runnable> list)
    {
    }

    public void methodWithList(List list)
    {
    }

    public void methodWithWildcardList(List<? super ArrayList> list)
    {

    }

    public FieService build_noargs()
    {
        return _fie;
    }

    public FieService build_injected(@InjectService("Foe")
    FoeService foe)
    {
        assertSame(_expectedFoe, foe);

        return _fie;
    }

    public FieService build_auto(FoeService foe)
    {
        assertSame(_expectedFoe, foe);

        return _fie;
    }

    public FieService build_fail()
    {
        throw new RuntimeException("Method failed.");
    }

    public FieService build_args(String serviceId, ServiceResources resources,
            Class serviceInterface, Log log)
    {
        assertEquals(serviceId, _expectedServiceId);
        assertSame(resources, _expectedServiceResources);
        assertSame(serviceInterface, _expectedServiceInterface);
        assertSame(log, _expectedLog);

        return _fie;
    }

}
