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

package org.apache.tapestry.internal.test;

import org.apache.tapestry.internal.services.ComponentInvocationMap;
import org.apache.tapestry.internal.services.CookieSink;
import org.apache.tapestry.internal.services.CookieSource;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ServiceBinder;
import org.apache.tapestry.services.AliasContribution;
import org.apache.tapestry.services.MarkupWriterFactory;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;
import org.apache.tapestry.test.PageTester;

/**
 * Used in conjuction with {@link PageTester} to mock up and/or stub out portions of Tapestry that
 * need to be handled differently when testing.
 */
public class PageTesterModule
{
    public static final String TEST_MODE = "test";

    public static void bind(ServiceBinder binder)
    {
        binder.bind(TestableRequest.class, TestableRequestImpl.class);
        binder.bind(TestableMarkupWriterFactory.class, TestableMarkupWriterFactoryImpl.class);
    }

    public static void contributeAlias(Configuration<AliasContribution> configuration,
            ObjectLocator locator)
    {
        add(configuration, ComponentInvocationMap.class, new PageTesterComponentInvocationMap());
        add(configuration, Response.class, new TestableResponseImpl());

        add(configuration, locator, Request.class, "TestableRequest");
        add(configuration, locator, MarkupWriterFactory.class, "TestableMarkupWriterFactory");

        TestableCookieSinkSource cookies = new TestableCookieSinkSource();

        add(configuration, CookieSink.class, cookies);
        add(configuration, CookieSource.class, cookies);
    }

    private static <T> void add(Configuration<AliasContribution> configuration,
            ObjectLocator locator, Class<T> serviceClass, String serviceId)
    {
        T service = locator.getService(serviceId, serviceClass);

        add(configuration, serviceClass, service);
    }

    private static <T> void add(Configuration<AliasContribution> configuration,
            Class<T> serviceClass, T service)
    {
        AliasContribution<T> contribution = AliasContribution.create(
                serviceClass,
                TEST_MODE,
                service);

        configuration.add(contribution);
    }
}
