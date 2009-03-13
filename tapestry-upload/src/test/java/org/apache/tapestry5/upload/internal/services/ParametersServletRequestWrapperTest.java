// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.upload.internal.services;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParametersServletRequestWrapperTest
{
    @Test
    public void getParameterNamesIsNotDelegated() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        replay(request);

        Enumeration names = wrapper.getParameterNames();

        verify(request);

        assertNotNull(names);
        assertFalse(names.hasMoreElements());
    }

    @Test
    public void getParameterMapIsNotDelegated() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        replay(request);

        Map parameters = wrapper.getParameterMap();

        verify(request);
        assertNotNull(parameters);
        assertTrue(parameters.isEmpty());
    }

    @Test
    public void getParameterIsNotDelegated() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        replay(request);

        String value = wrapper.getParameter("foo");

        verify(request);
        assertNull(value);
    }

    @Test
    public void getParameterValuesIsNotDelegated() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);

        replay(request);

        String[] values = wrapper.getParameterValues("foo");

        verify(request);
        assertNull(values);
    }

    @Test
    public void getParameterForSingleValue() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);
        replay(request);

        wrapper.addParameter("foo", "blah");

        assertEquals(wrapper.getParameter("foo"), "blah");
        verify(request);
    }

    @Test
    public void getParameterForMultiValueGivesFirstValue() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);
        replay(request);

        wrapper.addParameter("foo", "blah");
        wrapper.addParameter("foo", "another");

        assertEquals(wrapper.getParameter("foo"), "blah");
        verify(request);
    }

    @Test
    public void getParameterValuesForMultiValueGivesAll() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);
        replay(request);

        wrapper.addParameter("foo", "blah");
        wrapper.addParameter("foo", "another");

        assertEquals(wrapper.getParameterValues("foo"), new String[] { "blah", "another" });
        verify(request);
    }

    @Test
    public void getParameterNamesHasAllNames() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);
        replay(request);

        wrapper.addParameter("one", "blah");
        wrapper.addParameter("two", "another");

        Enumeration nameEnumerator = wrapper.getParameterNames();
        Set<String> names = new HashSet<String>();
        assertTrue(nameEnumerator.hasMoreElements());
        names.add((String) nameEnumerator.nextElement());
        assertTrue(nameEnumerator.hasMoreElements());
        names.add((String) nameEnumerator.nextElement());

        assertTrue(names.contains("one"));
        assertTrue(names.contains("two"));
        verify(request);
    }

    @Test
    public void getParameterMapHasAllValues() throws Exception
    {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        ParametersServletRequestWrapper wrapper = new ParametersServletRequestWrapper(request);
        replay(request);

        wrapper.addParameter("single", "blah");
        wrapper.addParameter("multi", "one");
        wrapper.addParameter("multi", "two");

        Map parameters = wrapper.getParameterMap();
        assertEquals(parameters.size(), 2);
        assertEquals(parameters.get("single"), "blah");
        assertEquals((String[]) parameters.get("multi"), new String[] { "one", "two" });

        verify(request);
    }
}
