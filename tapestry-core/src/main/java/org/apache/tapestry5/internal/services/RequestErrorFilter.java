package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.DifferentClassVersionsException;
import org.apache.tapestry5.corelib.pages.ExceptionReport;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RequestFilter;
import org.apache.tapestry5.http.services.RequestHandler;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
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
    private final ComponentInstantiatorSource componentInstantiatorSource;
    private final static String QUERY_PARAMETER = "RequestErrorFilterRedirected";
    private final static Pattern CCE_PATTERN = Pattern.compile("((.*)\\scannot be cast to (.*))(.*)");

    public RequestErrorFilter(InternalRequestGlobals internalRequestGlobals, RequestExceptionHandler exceptionHandler,
            @ComponentClasses InvalidationEventHub classesInvalidationHub, ComponentEventLinkEncoder componentEventLinkEncoder,
            ComponentInstantiatorSource componentInstantiatorSource)
    {
        this.internalRequestGlobals = internalRequestGlobals;
        this.exceptionHandler = exceptionHandler;
        this.classesInvalidationHub = classesInvalidationHub;
        this.componentEventLinkEncoder = componentEventLinkEncoder;
        this.componentInstantiatorSource = componentInstantiatorSource;
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
            
            if (request.getParameter(QUERY_PARAMETER) == null)
            {
            
                Throwable rootCause = ex.getCause();
                String classToInvalidate = getClassToInvalidate(rootCause);
                
                if (classToInvalidate != null)
                {
                    
                    final List<String> classesToInvalidate = 
                            Arrays.asList(classToInvalidate, ExceptionReport.class.getName());
                    componentInstantiatorSource.invalidate(classesToInvalidate);
                    classesInvalidationHub.fireInvalidationEvent(classesToInvalidate);

                    Link link = null;
                    
                    final ComponentEventRequestParameters componentEventParameters = componentEventLinkEncoder.decodeComponentEventRequest(request);
                    if (componentEventParameters != null)
                    {
                        link = componentEventLinkEncoder.createComponentEventLink(componentEventParameters, false);
                    }
                    
                    final PageRenderRequestParameters pageRenderParameters = componentEventLinkEncoder.decodePageRenderRequest(request);
                    if (pageRenderParameters != null)
                    {
                        link = componentEventLinkEncoder.createPageRenderLink(pageRenderParameters);
                    }
                    
                    if (link != null)
                    {
                        link.addParameter(QUERY_PARAMETER, "true");
                        response.sendRedirect(link);
                        return true;
                    }
                    
                }
                
            }
            
            // Most of the time, we've got exception linked up the kazoo ... but when ClassLoaders
            // get involved, things go screwy.  Exceptions when transforming classes can cause
            // a NoClassDefFoundError with no cause; here we're trying to link the cause back in.
            // TAPESTRY-2078

            Throwable exceptionToReport = attachNewCause(ex, internalRequestGlobals.getClassLoaderException());

            try
            {
                exceptionHandler.handleRequestException(exceptionToReport);
            }
            catch (Exception e)
            {
                classesInvalidationHub.fireInvalidationEvent(Collections.emptyList());
                exceptionHandler.handleRequestException(exceptionToReport);
            }

            // We assume a reponse has been sent and there's no need to handle the request
            // further.

            return true;
        }
    }

    private String getClassToInvalidate(Throwable rootCause) {
        String classToInvalidate = null;
        while (rootCause != null && rootCause.getCause() != null)
        {
            rootCause = rootCause.getCause();
        }
        if (rootCause instanceof DifferentClassVersionsException)
        {
            DifferentClassVersionsException dcve = (DifferentClassVersionsException) rootCause;
            classToInvalidate = dcve.getClassName();
        }
        else if (rootCause instanceof ClassCastException)
        {
            String message = rootCause.getMessage();
            if (message != null)
            {
            
                // Handling both Java 8 and Java 11 messages
                message = message.replace("class ", "");
                final int index = message.indexOf(" (");
                if (index > 0)
                {
                    message = message.substring(0, index);
                }
                
                final Matcher matcher = CCE_PATTERN.matcher(message);
                if (matcher.matches() && matcher.groupCount() >= 3)
                {
                    final String class1 = matcher.group(2);
                    final String class2 = matcher.group(3);
                    if (class1.equals(class2))
                    {
                        classToInvalidate = class1;
                    }
                }
            }
        }
        return classToInvalidate;
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
