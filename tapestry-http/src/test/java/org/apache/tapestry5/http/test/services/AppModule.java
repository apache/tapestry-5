package org.apache.tapestry5.http.test.services;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.HttpServletRequestFilter;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.test.TestDispatcher;
import org.apache.tapestry5.http.test.TestHttpServletRequestFilter;
import org.apache.tapestry5.http.test.TestRequestFilter;

public class AppModule {

    public static void contributeMasterDispatcher(OrderedConfiguration<Dispatcher> configuration)
    {
        configuration.addInstance("Hello", TestDispatcher.class);
    }
    
    public static void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration)
    {
        configuration.addInstance("Test", TestRequestFilter.class);
    }
    
    public static void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration)
    {
        configuration.addInstance("Test", TestHttpServletRequestFilter.class);
    }
    
    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED, true);
    }

}
