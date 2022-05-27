package org.apache.tapestry5.http.internal.services;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.CorsHandlerResult;
import org.apache.tapestry5.http.services.CorsHandlerHelper;
import org.apache.tapestry5.http.services.CorsHttpServletRequestFilter;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for {@link CorsHttpServletRequestFilter}.
 */
public class DefaultCorsHandlerTest {
    
    @Test
    public void handle() throws IOException
    {
        
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        CorsHandlerHelper helper = EasyMock.createMock(CorsHandlerHelper.class);
        DefaultCorsHandler handler = new DefaultCorsHandler(helper);
        
        // Scenario 1: no preflight, no origin, not CORS
        EasyMock.reset(helper, request, response);
        EasyMock.expect(helper.isPreflight(request)).andReturn(false);
        EasyMock.expect(helper.getAllowedOrigin(request)).andReturn(Optional.empty());
        EasyMock.replay(helper, request, response);
        Assert.assertEquals(CorsHandlerResult.CONTINUE_CORS_PROCESSING, handler.handle(request, response));
        EasyMock.verify(helper, request, response);
        
        // Scenario 2: preflight, no allowed Origin, not CORS
        EasyMock.reset(helper, request, response);
        EasyMock.expect(helper.isPreflight(request)).andReturn(true);
        EasyMock.expect(helper.getAllowedOrigin(request)).andReturn(Optional.empty());
        EasyMock.replay(helper, request, response);
        Assert.assertEquals(CorsHandlerResult.CONTINUE_CORS_PROCESSING, handler.handle(request, response));
        EasyMock.verify(helper, request, response);

        // Scenario 3: preflight, allowed Origin, preflight CORS
        final String origin = "https://tapestry.apache.org";
        EasyMock.reset(helper, request, response);
        
        EasyMock.expect(helper.isPreflight(request)).andReturn(true);
        EasyMock.expect(helper.getAllowedOrigin(request)).andReturn(Optional.of(origin));
        helper.configureOrigin(response, origin);
        helper.configureCredentials(response);
        helper.configureExposeHeaders(response);
        helper.configureMethods(response);
        helper.configureAllowedHeaders(response, request);
        helper.configureMaxAge(response);
        response.setContentLength(0);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        EasyMock.replay(helper, request, response);
        Assert.assertEquals(CorsHandlerResult.STOP_REQUEST_PROCESSING, handler.handle(request, response));
        EasyMock.verify(helper, request, response);
        
        // Scenario 4: no preflight, allowed Origin, non-preflight CORS
        EasyMock.reset(helper, request, response);
        EasyMock.expect(helper.isPreflight(request)).andReturn(false);
        EasyMock.expect(helper.getAllowedOrigin(request)).andReturn(Optional.of(origin));
        helper.configureOrigin(response, origin);
        helper.configureCredentials(response);
        helper.configureExposeHeaders(response);
        EasyMock.replay(helper, request, response);
        Assert.assertEquals(CorsHandlerResult.CONTINUE_CORS_PROCESSING, handler.handle(request, response));
        EasyMock.verify(helper, request, response);
        
    }
    
}
