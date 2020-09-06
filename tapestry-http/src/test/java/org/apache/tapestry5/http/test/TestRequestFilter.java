package org.apache.tapestry5.http.test;

import java.io.IOException;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRequestFilter implements RequestFilter {

    final private static Logger LOGGER = LoggerFactory.getLogger(TestRequestFilter.class);
    
    @Override
    public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
        LOGGER.info("Before: " + new java.util.Date());
        boolean serviced = handler.service(request, response);
        LOGGER.info("After: " + new java.util.Date());
        return serviced;
    }

}
