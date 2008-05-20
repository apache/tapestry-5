// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Locale;

public class LocalizationFilterTest extends TapestryTestCase
{
    @Test
    public void set_locale_and_service() throws IOException
    {
        RequestHandler handler = mockRequestHandler();
        Request request = mockRequest();
        Response response = mockResponse();
        LocalizationSetter setter = newMock(LocalizationSetter.class);

        train_getLocale(request, Locale.CANADA_FRENCH);

        // We don't actually verify that setThreadLocale() occurs before service(),
        // but sometimes you just have to trust that the code executes in the
        // order its written.
        setter.setThreadLocale(Locale.CANADA_FRENCH);

        train_service(handler, request, response, true);

        replay();

        RequestFilter filter = new LocalizationFilter(setter);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

}
