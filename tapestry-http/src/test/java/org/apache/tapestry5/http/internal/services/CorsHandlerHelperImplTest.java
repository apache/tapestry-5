package org.apache.tapestry5.http.internal.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.services.CorsHandlerHelper;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for {@link CorsHandlerHelperImpl.
 */
public class CorsHandlerHelperImplTest {

    
    @Test
    public void get_origin()
    {
        
        final String[] allowedOrigins = new String[] {"http://tapestry.apache.org", "https://apache.org"};
        
        // Scenario 1: explicit origins
        
        CorsHandlerHelper helper = create(String.join("  ,  ", allowedOrigins));
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        
        for (String origin : allowedOrigins)
        {

            // Allowed origins
            EasyMock.reset(request);
            EasyMock.expect(request.getHeader(CorsHandlerHelper.ORIGIN_HEADER)).andReturn(origin);
            EasyMock.replay(request);
            Assert.assertEquals("Allowed origin", origin, helper.getAllowedOrigin(request).get());
            EasyMock.verify(request);
            
            // Non-allowed origins
            EasyMock.reset(request);
            EasyMock.expect(request.getHeader(CorsHandlerHelper.ORIGIN_HEADER)).andReturn(origin + "baaaa");
            EasyMock.replay(request);
            Assert.assertFalse("Non-allowed origins", helper.getAllowedOrigin(request).isPresent());
            EasyMock.verify(request);
            
        }
        
        // Scenario 2: allow all origins
        
        helper = create(CorsHandlerHelper.ORIGIN_WILDCARD);
        for (int i = 0; i < 10; i++)
        {
            final String origin = "https://web" + i + ".tapestry.apache.org";
            EasyMock.reset(request);
            EasyMock.expect(request.getHeader(CorsHandlerHelper.ORIGIN_HEADER)).andReturn(origin);
            EasyMock.replay(request);
            Assert.assertEquals("All origins should be accepted", CorsHandlerHelper.ORIGIN_WILDCARD, helper.getAllowedOrigin(request).get());
            EasyMock.verify(request);
        }
        
    }

    @Test
    public void is_preflight()
    {
        
        // Scenario 1: not OPTIONS
        final String origin = "https://tapestry.apache.org";
        CorsHandlerHelper helper = create(origin);
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getMethod()).andReturn("PUT");
        EasyMock.replay(request);
        Assert.assertFalse("Not OPTIONS", helper.isPreflight(request));
        EasyMock.verify(request);

        // Scenario 2: no Origin
        EasyMock.reset(request);
        EasyMock.expect(request.getMethod()).andReturn(CorsHandlerHelper.OPTIONS_METHOD);
        EasyMock.expect(request.getHeader(CorsHandlerHelper.ORIGIN_HEADER)).andReturn(null);
        EasyMock.replay(request);
        Assert.assertFalse("No Origin", helper.isPreflight(request));
        EasyMock.verify(request);

        // Scenario 3: accepted origin
        EasyMock.reset(request);
        EasyMock.expect(request.getMethod()).andReturn(CorsHandlerHelper.OPTIONS_METHOD);
        EasyMock.expect(request.getHeader(CorsHandlerHelper.ORIGIN_HEADER)).andReturn(origin);
        EasyMock.replay(request);
        Assert.assertTrue("Preflight indeed", helper.isPreflight(request));
        EasyMock.verify(request);

        // Scenario 4: non-accepted origin
        EasyMock.reset(request);
        EasyMock.expect(request.getMethod()).andReturn(CorsHandlerHelper.OPTIONS_METHOD);
        EasyMock.expect(request.getHeader(CorsHandlerHelper.ORIGIN_HEADER)).andReturn(origin + "baaah");
        EasyMock.replay(request);
        Assert.assertFalse("Non-accepted origin", helper.isPreflight(request));
        EasyMock.verify(request);

    }
    
    @Test
    public void get_path()
    {

        CorsHandlerHelper helper = create("*");
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        
        final String pathInfo = "/blah/foo/";
        EasyMock.expect(request.getPathInfo()).andReturn(pathInfo);
        EasyMock.replay(request);
        Assert.assertEquals(pathInfo, helper.getPath(request));
        EasyMock.verify(request);
        
        EasyMock.reset(request);
        EasyMock.expect(request.getPathInfo()).andReturn("");
        EasyMock.replay(request);
        Assert.assertEquals("/", helper.getPath(request));
        EasyMock.verify(request);

        final String servletPath = "/servlet";
        EasyMock.reset(request);
        EasyMock.expect(request.getPathInfo()).andReturn(null);
        EasyMock.expect(request.getServletPath()).andReturn(servletPath);
        EasyMock.replay(request);
        Assert.assertEquals(servletPath, helper.getPath(request));
        EasyMock.verify(request);

    }
    
    @Test
    public void configure_origin()
    {
        CorsHandlerHelper helper = create("*");
        HttpServletResponse response = new TestableHttpServletResponse();;
        
        final String origin = "https://apache.org";
        helper.configureOrigin(response, origin);
        Assert.assertEquals(origin, response.getHeader(CorsHandlerHelper.ALLOW_ORIGIN_HEADER));
        Assert.assertEquals(CorsHandlerHelper.ORIGIN_HEADER, response.getHeader(CorsHandlerHelper.VARY_HEADER));
    }
    
    @Test
    public void add_value_to_vary_header()
    {
        String[] values = new String[] {"Something", "else", "entirely"};
        HttpServletResponse response = new TestableHttpServletResponse();
        CorsHandlerHelper helper = create(CorsHandlerHelper.ORIGIN_WILDCARD);
        for (String value : values) {
            helper.addValueToVaryHeader(response, value);
        }
        Assert.assertEquals(String.join(", ", values), response.getHeader(CorsHandlerHelper.VARY_HEADER));
    }
    
    @Test
    public void configure_credentials()
    {
        HttpServletResponse response = new TestableHttpServletResponse();
        
        CorsHandlerHelper helper = create(CorsHandlerHelper.ORIGIN_WILDCARD, true);
        helper.configureCredentials(response);
        Assert.assertEquals("true", response.getHeader(CorsHandlerHelper.ALLOW_CREDENTIALS_HEADER));
        
        response = new TestableHttpServletResponse();

        helper = create(CorsHandlerHelper.ORIGIN_WILDCARD, false);
        helper.configureCredentials(response);
        Assert.assertNull("Header shouldn't be set", 
                response.getHeader(CorsHandlerHelper.ALLOW_CREDENTIALS_HEADER));

    }
    
    @Test
    public void configure_methods()
    {
        HttpServletResponse response = new TestableHttpServletResponse();
        
        final String allowedMethods = "GET,PUT";
        CorsHandlerHelper helper = new CorsHandlerHelperImpl("", false, allowedMethods, "", "", "");
        response = new TestableHttpServletResponse();
        helper.configureMethods(response);
        Assert.assertEquals(allowedMethods, response.getHeader(CorsHandlerHelper.ALLOW_METHODS_HEADER));
    }

    @Test
    public void configure_allowed_headers()
    {
        HttpServletResponse response = new TestableHttpServletResponse();
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
     
        // Scenario 1: configuration isn't empty
        final String configuredHeaders = "Blah,Foo";
        CorsHandlerHelper helper = new CorsHandlerHelperImpl("", false, "", configuredHeaders, "", "");
        EasyMock.replay(request);
        helper.configureAllowedHeaders(response, request);
        EasyMock.verify(request);
        Assert.assertEquals(configuredHeaders, response.getHeader(CorsHandlerHelper.ALLOW_HEADERS_HEADER));
        Assert.assertNull(response.getHeader(CorsHandlerHelper.VARY_HEADER));

        // Scenario 2: configuration empty, request's Access-Control-Request-Headers not empty
        response = new TestableHttpServletResponse();
        final String requestHeaders = "Foo,Blah";
        helper = new CorsHandlerHelperImpl("", false, "", "", "", "");
        EasyMock.reset(request);
        EasyMock.expect(request.getHeader(CorsHandlerHelper.REQUEST_HEADERS_HEADER)).andReturn(requestHeaders);
        EasyMock.replay(request);
        helper.configureAllowedHeaders(response, request);
        EasyMock.verify(request);
        Assert.assertEquals(requestHeaders, response.getHeader(CorsHandlerHelper.ALLOW_HEADERS_HEADER));
        Assert.assertEquals(CorsHandlerHelper.REQUEST_HEADERS_HEADER, response.getHeader(CorsHandlerHelper.VARY_HEADER));

        // Scenario 3: configuration empty, request's Access-Control-Request-Headers also empty
        response = new TestableHttpServletResponse();
        EasyMock.reset(request);
        EasyMock.expect(request.getHeader(CorsHandlerHelper.REQUEST_HEADERS_HEADER)).andReturn(null);
        EasyMock.replay(request);
        helper.configureAllowedHeaders(response, request);
        EasyMock.verify(request);
        Assert.assertNull(response.getHeader(CorsHandlerHelper.ALLOW_HEADERS_HEADER));
        Assert.assertEquals(CorsHandlerHelper.REQUEST_HEADERS_HEADER, response.getHeader(CorsHandlerHelper.VARY_HEADER));

    }

    @Test
    public void configure_expose_headers()
    {
        HttpServletResponse response = new TestableHttpServletResponse();
        
        final String exposeHeaders = "Blah,Foo,Bar";
        CorsHandlerHelper helper = new CorsHandlerHelperImpl("", false, "", "", exposeHeaders, "");
        helper.configureExposeHeaders(response);
        Assert.assertEquals(exposeHeaders, response.getHeader(CorsHandlerHelper.EXPOSE_HEADERS_HEADER));
        
        response = new TestableHttpServletResponse();

        helper = new CorsHandlerHelperImpl("", false, "", "", "", "");
        helper.configureExposeHeaders(response);
        Assert.assertNull("Header shouldn't be set", 
                response.getHeader(CorsHandlerHelper.EXPOSE_HEADERS_HEADER));

    }

    @Test
    public void configure_max_age()
    {
        HttpServletResponse response = new TestableHttpServletResponse();
        
        final String maxAge = "1234567";
        CorsHandlerHelper helper = new CorsHandlerHelperImpl("", false, "", "", "", maxAge);
        helper.configureMaxAge(response);
        Assert.assertEquals(maxAge, response.getHeader(CorsHandlerHelper.MAX_AGE_HEADER));
        
        response = new TestableHttpServletResponse();

        helper = new CorsHandlerHelperImpl("", false, "", "", "", "");
        helper.configureExposeHeaders(response);
        Assert.assertNull("Header shouldn't be set", 
                response.getHeader(CorsHandlerHelper.MAX_AGE_HEADER));

    }

    private CorsHandlerHelper create(String allowedOrigins)
    {
        return create(allowedOrigins, false);
    }
    
    private CorsHandlerHelper create(String allowedOrigins, boolean allowCredentials)
    {
        return new CorsHandlerHelperImpl(allowedOrigins, allowCredentials, "", "", "", "");
    }
    
    private final class TestableHttpServletResponse implements HttpServletResponse
    {
        
        private final Map<String, String> headers = new HashMap<>();

        @Override
        public String getCharacterEncoding() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContentType() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrintWriter getWriter() throws IOException 
        {
            return null;
        }

        @Override
        public void setCharacterEncoding(String charset) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setContentLength(int len) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setContentType(String type) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBufferSize(int size) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getBufferSize() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flushBuffer() throws IOException 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resetBuffer() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCommitted() 
        {
            return false;
        }

        @Override
        public void reset() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLocale(Locale loc) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale getLocale() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addCookie(Cookie cookie) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsHeader(String name) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeURL(String url) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeRedirectURL(String url) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeUrl(String url) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeRedirectUrl(String url) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendError(int sc, String msg) throws IOException 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendError(int sc) throws IOException 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendRedirect(String location) throws IOException 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDateHeader(String name, long date) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addDateHeader(String name, long date) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHeader(String name, String value) 
        {
            headers.put(name, value);
        }

        @Override
        public void addHeader(String name, String value) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setIntHeader(String name, int value) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addIntHeader(String name, int value) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStatus(int sc) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStatus(int sc, String sm) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getStatus() 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String name) {
            return headers.get(name);
        }

        @Override
        public Collection<String> getHeaders(String name) 
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getHeaderNames() 
        {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
