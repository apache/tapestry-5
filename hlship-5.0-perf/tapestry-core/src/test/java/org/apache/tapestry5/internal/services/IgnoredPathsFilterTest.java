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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestHandler;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class IgnoredPathsFilterTest extends TapestryTestCase
{
    @Test
    public void no_match() throws IOException
    {
        HttpServletRequest request = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();
        HttpServletRequestHandler handler = mockHttpServletRequestHandler();

        train_getServletPath(request, "/");
        train_getPathInfo(request, "barney");

        train_service(handler, request, response, true);

        List<String> configuration = CollectionFactory.newList("/fred");


        replay();

        HttpServletRequestFilter filter = new IgnoredPathsFilter(configuration);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void no_path_info() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();
        HttpServletRequestHandler handler = mockHttpServletRequestHandler();

        train_getServletPath(request, "/");
        train_getPathInfo(request, null);

        train_service(handler, request, response, true);

        List<String> configuration = CollectionFactory.newList("/fred");


        replay();

        HttpServletRequestFilter filter = new IgnoredPathsFilter(configuration);

        assertTrue(filter.service(request, response, handler));

        verify();
    }

    @Test
    public void path_excluded() throws Exception
    {
        HttpServletRequest request = mockHttpServletRequest();
        HttpServletResponse response = mockHttpServletResponse();
        HttpServletRequestHandler handler = mockHttpServletRequestHandler();

        train_getServletPath(request, "/");
        train_getPathInfo(request, "barney/rubble");

        List<String> configuration = CollectionFactory.newList("/barney.*");

        replay();

        HttpServletRequestFilter filter = new IgnoredPathsFilter(configuration);

        assertFalse(filter.service(request, response, handler));

        verify();
    }

}
