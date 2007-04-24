package ${packageName}.services;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Contribute;
import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.annotations.InjectService;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.RequestFilter;
import org.apache.tapestry.services.RequestHandler;
import org.apache.tapestry.services.Response;

/**
 * This module is automatically included as part of the Tapestry IoC Registry, it's a good place to
 * configure and extend Tapestry, or to place your own services.
 */
@Id("app")
public class AppModule
{
    @Contribute("tapestry.ioc.ApplicationDefaults")
    public static void contributeApplicationDefaults(
            MappedConfiguration<String, String> configuration)
    {
        // Contributions to tapestry.ioc.ApplicationDefaults will override any contributes to
        // tapestry.io.FactoryDefaults (with the same key). Here we're restricting the supported locales
        // to just "en" (English). Tapestry will be most efficient with a finite number of supported locales.
        // As you add localised message catalogs and other assets, you can extend this list of locales (it's
        // a comma seperated series of locale name; the first locale name is the default when there's no
        // reasonable match).
        
        configuration.add("tapestry.supported-locales", "en");
    }
    

    /**
     * This is a service definition, the service will be named app.TimingFilter. The interface,
     * RequestFilter, is used within the tapestry.RequestHandler pipeline. Tapestry IoC is
     * responsible for passing in an appropriate Log instance. Requests for static resources are
     * handled at a higher level, so this filter (once contributed into the tapestry.RequestHandler
     * service) will only be invoked for Tapestry related requests.
     */    
    public RequestFilter buildTimingFilter(final Log log)
    {
        return new RequestFilter()
        {
            public boolean service(Request request, Response response, RequestHandler handler)
                    throws IOException
            {
                long startTime = System.currentTimeMillis();

                try
                {
                    return handler.service(request, response);
                }
                finally
                {
                    long elapsed = System.currentTimeMillis() - startTime;

                    log.info(String.format("Request time: %d ms", elapsed));
                }
            }
        };
    }

    /**
     * This is a contribution to the tapestry.RequestHandler's service configuration. This is how
     * we extend Tapestry using the timing filter. A common use for this kind of filter is transaction
     * management or auditting.
     */
    @Contribute("tapestry.RequestHandler")
    public void contributeRequestFilters(OrderedConfiguration<RequestFilter> configuration,
            @InjectService("TimingFilter")
            RequestFilter filter)
    {
        // Each contribution to an ordered configuration has a name, which will be qualified with
        // the module's id. Here, the fully qualified id will be "app.Timing".  When necessary, you may
        // set constraints to precisely control the invocation order of the contributed filter
        // within the pipeline.
        
        configuration.add("Timing", filter);
    }



}
