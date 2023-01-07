package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.util.Arrays;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.DifferentClassVersionsException;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.RequestExceptionHandler;

/**
 * Filter for the {@link org.apache.tapestry5.http.services.RequestHandler} pipeline used to intercept and report
 * exceptions.
 */
public class RequestErrorFilter implements RequestFilter
{
    private final InternalRequestGlobals internalRequestGlobals;
    private final RequestExceptionHandler exceptionHandler;
    private final InvalidationEventHub classesInvalidationHub;
    private final ComponentEventLinkEncoder componentEventLinkEncoder;
    private final static String QUERY_PARAMETER = "RequestErrorFilterRedirected";

    public RequestErrorFilter(InternalRequestGlobals internalRequestGlobals, RequestExceptionHandler exceptionHandler,
            @ComponentClasses InvalidationEventHub classesInvalidationHub, ComponentEventLinkEncoder componentEventLinkEncoder)
    {
        this.internalRequestGlobals = internalRequestGlobals;
        this.exceptionHandler = exceptionHandler;
        this.classesInvalidationHub = classesInvalidationHub;
        this.componentEventLinkEncoder = componentEventLinkEncoder;
    }

    public boolean service(Request request, Response response, RequestHandler handler) throws IOException
    {
        try
        {
            return handler.service(request, response);
        }
        catch (IOException ex)
        {
            // Pass it through.
            throw ex;
        }
        catch (Throwable ex)
        {
            
            Throwable rootCause = ex.getCause();
            while (rootCause != null && rootCause.getCause() != null)
            {
                rootCause = rootCause.getCause();
            }
            if (rootCause instanceof DifferentClassVersionsException)
            {
                DifferentClassVersionsException dcve = (DifferentClassVersionsException) rootCause;
                classesInvalidationHub.fireInvalidationEvent(Arrays.asList(dcve.getClassName()));
                final PageRenderRequestParameters pageRenderParameters = componentEventLinkEncoder.decodePageRenderRequest(request);
                if (request.getParameter(QUERY_PARAMETER) == null && pageRenderParameters != null)
                {
                    final Link link = componentEventLinkEncoder.createPageRenderLink(pageRenderParameters);
                    link.addParameter(QUERY_PARAMETER, "tue");
                    response.sendRedirect(link);
                    return true;
                }
//                final ComponentEventRequestParameters componentEventParameters = componentEventLinkEncoder.decodeComponentEventRequest(request);
//                if (componentEventParameters != null)
//                {
//                    response.sendRedirect(componentEventLinkEncoder.createComponentEventLink(componentEventParameters, false));
//                    return true;
//                }
            }
            
            // Most of the time, we've got exception linked up the kazoo ... but when ClassLoaders
            // get involved, things go screwy.  Exceptions when transforming classes can cause
            // a NoClassDefFoundError with no cause; here we're trying to link the cause back in.
            // TAPESTRY-2078

            Throwable exceptionToReport = attachNewCause(ex, internalRequestGlobals.getClassLoaderException());

            exceptionHandler.handleRequestException(exceptionToReport);

            // We assume a reponse has been sent and there's no need to handle the request
            // further.

            return true;
        }
    }

    private Throwable attachNewCause(Throwable exception, Throwable underlyingCause)
    {
        if (underlyingCause == null) return exception;

        Throwable current = exception;

        while (current != null)
        {

            if (current == underlyingCause) return exception;

            Throwable cause = current.getCause();

            // Often, exceptions report themselves as their own cause.

            if (current == cause) break;

            if (cause == null)
            {

                try
                {
                    current.initCause(underlyingCause);

                    return exception;
                }
                catch (IllegalStateException ex)
                {
                    // TAPESTRY-2284: sometimes you just can't init the cause, and there's no way to
                    // find out without trying.

                }
            }

            // Otherwise, continue working down the chain until we find a place where we can attach

            current = cause;
        }

        // Found no place to report the exeption, so report the underlying cause (and lose out
        // on all the other context).

        return underlyingCause;
    }
}
