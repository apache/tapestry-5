package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.*;

import java.io.IOException;

/**
 * Filter for the {@link org.apache.tapestry5.services.RequestHandler} pipeline used to intercept and report
 * exceptions.
 */
public class RequestErrorFilter implements RequestFilter
{
    private final InternalRequestGlobals internalRequestGlobals;
    private final RequestExceptionHandler exceptionHandler;

    public RequestErrorFilter(InternalRequestGlobals internalRequestGlobals, RequestExceptionHandler exceptionHandler)
    {
        this.internalRequestGlobals = internalRequestGlobals;
        this.exceptionHandler = exceptionHandler;
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
