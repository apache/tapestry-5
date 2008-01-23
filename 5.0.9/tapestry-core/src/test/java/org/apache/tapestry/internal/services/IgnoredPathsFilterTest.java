// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class IgnoredPathsFilterTest extends TapestryTestCase
{
    @Test
    public void no_match() throws IOException
    {
        Request request = mockRequest();
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();

        train_getPath(request, "/barney");

        train_service(handler, request, response, true);

        List<String> configuration = CollectionFactory.newList("/fred");


        replay();

        RequestFilter filter = new IgnoredPathsFilter(configuration);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void path_excluded() throws Exception
    {
        Request request = mockRequest();
        Response response = mockResponse();
        RequestHandler handler = mockRequestHandler();

        train_getPath(request, "/barney/rubble");

        List<String> configuration = CollectionFactory.newList("/barney.*");

        replay();

        RequestFilter filter = new IgnoredPathsFilter(configuration);

        assertFalse(filter.service(request, response, handler));

        verify();
    }

}
