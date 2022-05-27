package org.apache.tapestry5.http.internal.services;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.CorsHandlerResult;
import org.apache.tapestry5.http.services.CorsHandler;
import org.apache.tapestry5.http.services.CorsHttpServletRequestFilter;
import org.apache.tapestry5.http.services.HttpServletRequestHandler;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Test suite for {@link CorsHttpServletRequestFilter}.
 */
public class CorsHttpServletRequestFilterTest {
    
    @Test
    public void service() throws IOException
    {
        
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
        HttpServletRequestHandler httpServletRequestHandler = EasyMock.createMock(HttpServletRequestHandler.class);
        
        CorsHandler stopRequestProcessing = (req, res) -> CorsHandlerResult.STOP_REQUEST_PROCESSING;
        CorsHandler continueRequestProcessing = (req, res) -> CorsHandlerResult.CONTINUE_REQUEST_PROCESSING;
        CorsHandler throwException = (req, res) -> { throw new RuntimeException("Shouldn't have been called"); };
        TestCorsHandler testCorsHandler = new TestCorsHandler();
        TestCorsHandler testCorsHandler2 = new TestCorsHandler();
        
        // Scenario 1: all handlers return continue CORS processing.
        CorsHttpServletRequestFilter filter = 
                new CorsHttpServletRequestFilter(Arrays.asList(testCorsHandler, testCorsHandler2));
        EasyMock.expect(httpServletRequestHandler.service(request, response)).andReturn(true);
        EasyMock.replay(httpServletRequestHandler);
        filter.service(request, response, httpServletRequestHandler);
        EasyMock.verify(httpServletRequestHandler);
        Assert.assertTrue(testCorsHandler.isCalled());
        Assert.assertTrue(testCorsHandler2.isCalled());

        // Scenario 2: first handler stops CORS processing
        EasyMock.reset(httpServletRequestHandler);
        filter = new CorsHttpServletRequestFilter(Arrays.asList(continueRequestProcessing, throwException));
        EasyMock.expect(httpServletRequestHandler.service(request, response)).andReturn(true);
        EasyMock.replay(httpServletRequestHandler);
        filter.service(request, response, httpServletRequestHandler);
        EasyMock.verify(httpServletRequestHandler);

        // Scenario 3: first handler stops request processing
        EasyMock.reset(httpServletRequestHandler);
        filter = new CorsHttpServletRequestFilter(Arrays.asList(stopRequestProcessing, throwException));
        EasyMock.replay(httpServletRequestHandler);
        filter.service(request, response, httpServletRequestHandler);
        EasyMock.verify(httpServletRequestHandler);


    }
    
    private static final class TestCorsHandler implements CorsHandler
    {
        private boolean called;

        public boolean isCalled() {
            return called;
        }
        
        @Override
        public CorsHandlerResult handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
            called = true;
            return CorsHandlerResult.CONTINUE_CORS_PROCESSING;
        }
        
    }
    
}
