// Copyright 2011 The Apache Software Foundation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.http.internal.services.RestSupportImpl;
import org.apache.tapestry5.http.services.HttpRequestBodyConverter;
import org.apache.tapestry5.http.services.RestSupport;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

/**
 * Tests {@link RestSupportImpl}.
 */
public class RestSupportImplTest extends InternalBaseTestCase
{
    final private static byte[] EMPTY_ARRAY = new byte[0];
    
    @Test
    public void is_get() throws IOException
    {
        test("GET", EMPTY_ARRAY, (rs) -> assertTrue(rs.isHttpGet()));
        test("notGET", EMPTY_ARRAY, (rs) -> assertFalse(rs.isHttpGet()));
    }

    @Test
    public void is_post() throws IOException
    {
        test("POST", EMPTY_ARRAY, (rs) -> assertTrue(rs.isHttpPost()));
        test("notPOST", EMPTY_ARRAY, (rs) -> assertFalse(rs.isHttpPost()));        
    }

    @Test
    public void is_head() throws IOException
    {
        test("HEAD", EMPTY_ARRAY, (rs) -> assertTrue(rs.isHttpHead()));
        test("notHEAD", EMPTY_ARRAY, (rs) -> assertFalse(rs.isHttpHead()));
    }
    
    @Test
    public void is_put() throws IOException
    {
        test("PUT", EMPTY_ARRAY, (rs) -> assertTrue(rs.isHttpPut()));
        test("notPUT", EMPTY_ARRAY, (rs) -> assertFalse(rs.isHttpPut()));        
    }

    @Test
    public void is_delete() throws IOException
    {
        test("DELETE", EMPTY_ARRAY, (rs) -> assertTrue(rs.isHttpDelete()));
        test("notDELETE", EMPTY_ARRAY, (rs) -> assertFalse(rs.isHttpDelete()));
    }

    @Test
    public void is_patch() throws IOException
    {
        test("PATCH", EMPTY_ARRAY, (rs) -> assertTrue(rs.isHttpPatch()));
        test("notPATCH", EMPTY_ARRAY, (rs) -> assertFalse(rs.isHttpPatch()));
    }
    
    @Test
    public void get_request_body_as_result_provided()
    {
        final String TEXT_CONTENT = "asdfadfasdfs";
        HttpRequestBodyConverter converter = new TestHttpRequestBodyConverter(TEXT_CONTENT);
        RestSupport restSupport = new RestSupportImpl(null, converter);
        Optional<String> result = restSupport.getRequestBodyAs(String.class);
        assertTrue(result.isPresent());
        assertEquals(result.get(), TEXT_CONTENT);
    }

    @Test
    public void get_request_body_as_null_result()
    {
        HttpRequestBodyConverter converter = new TestHttpRequestBodyConverter(null);
        RestSupport restSupport = new RestSupportImpl(null, converter);
        Optional<String> result = restSupport.getRequestBodyAs(String.class);
        assertFalse(result.isPresent());
    }
    
    final private static class TestHttpRequestBodyConverter implements HttpRequestBodyConverter
    {
        final private String value;

        public TestHttpRequestBodyConverter(String value) 
        {
            super();
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T convert(HttpServletRequest request, Class<T> type) {
            return (T) value;
        }
        
    }

    private void test(String method, byte[] body, Consumer<RestSupport> testCode) throws IOException
    {
        HttpServletRequest request = createRequest(method, body);
        RestSupport restSupport = new RestSupportImpl(request, null);
        testCode.accept(restSupport);
        EasyMock.verify(request);
    }

    private HttpServletRequest createRequest(String method, byte[] body) throws IOException
    {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getMethod()).andReturn(method).anyTimes();
        EasyMock.expect(request.getInputStream()).andReturn(new TestServletInputStream(body)).anyTimes();
        EasyMock.replay(request);
        return request;
    }
    
    final private static class TestServletInputStream extends ServletInputStream
    {
        
        final private InputStream inputStream;
        
        public TestServletInputStream(byte[] bytes) 
        {
            inputStream = new ByteArrayInputStream(bytes);
        }

        @Override
        public int read() throws IOException 
        {
            return inputStream.read();
        }
        
    }
    
}
