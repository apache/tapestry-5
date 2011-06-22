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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.CookieSink;
import org.apache.tapestry5.internal.services.CookieSource;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.PageTester;

/**
 * Used in conjuction with {@link PageTester} to mock up and/or stub out portions of Tapestry that need to be handled
 * differently when testing.
 */
public class PageTesterModule
{
    public static final String TEST_MODE = "test";

    public static void bind(ServiceBinder binder)
    {
        binder.bind(TestableRequest.class, TestableRequestImpl.class);
        binder.bind(TestableResponse.class, TestableResponseImpl.class);
    }

    public static void contributeAlias(Configuration<AliasContribution> configuration, ObjectLocator locator)
    {
        alias(configuration, locator, Request.class, "TestableRequest");
        alias(configuration, locator, Response.class, "TestableResponse");

        TestableCookieSinkSource cookies = new TestableCookieSinkSource();

        alias(configuration, CookieSink.class, cookies);
        alias(configuration, CookieSource.class, cookies);
    }

    private static <T> void alias(Configuration<AliasContribution> configuration, ObjectLocator locator,
                                  Class<T> serviceClass, String serviceId)
    {
        T service = locator.getService(serviceId, serviceClass);

        alias(configuration, serviceClass, service);
    }

    private static <T> void alias(Configuration<AliasContribution> configuration, Class<T> serviceClass, T service)
    {
        AliasContribution<T> contribution = AliasContribution.create(serviceClass, TEST_MODE, service);

        configuration.add(contribution);
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.override(SymbolConstants.FORCE_ABSOLUTE_URIS, "true");
    }

    public static void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration)
    {
        configuration.addInstance("EndOfRequestCleanup", EndOfRequestCleanupFilter.class, "before:StaticFiles");
    }

    public static void contributeMarkupRenderer(OrderedConfiguration<MarkupRendererFilter> configuration)
    {
        configuration.addInstance("CaptureRenderedDocument", CaptureRenderedDocument.class, "before:DocumentLinker");
    }
}
