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

@Id("app")
public class AppModule
{
    @Contribute("tapestry.ioc.ApplicationDefaults")
    public static void contributeApplicationDefaults(
            MappedConfiguration<String, String> configuration)
    {
        configuration.add("tapestry.supported-locales", "en");
    }
    
    // This may eventually be baked into tapestry-core:
    
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

    @Contribute("tapestry.RequestHandler")
    public void contributeRequestFilters(OrderedConfiguration<RequestFilter> configuration,
            @InjectService("TimingFilter")
            RequestFilter filter)
    {
        configuration.add("Timing", filter);
    }



}
