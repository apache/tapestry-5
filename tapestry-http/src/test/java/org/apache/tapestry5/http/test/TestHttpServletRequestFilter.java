package org.apache.tapestry5.http.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.services.HttpServletRequestFilter;
import org.apache.tapestry5.http.services.HttpServletRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHttpServletRequestFilter implements HttpServletRequestFilter {

    final private static Logger LOGGER = LoggerFactory.getLogger(TestHttpServletRequestFilter.class);
    
    @Override
    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler) throws IOException {
        LOGGER.info("Before: " + request.getServerName() + " : " + new java.util.Date());
        boolean serviced = handler.service(request, response);
        LOGGER.info("After: " + request.getServerName() + " : " + new java.util.Date());
        return serviced;
    }

}
