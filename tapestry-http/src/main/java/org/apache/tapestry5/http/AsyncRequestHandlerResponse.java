// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.http;

import java.util.Objects;
import java.util.concurrent.Executor;

import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class used by {@linkplain AsyncRequestHandler} to return information on how to handle
 * a request.
 * @see AsyncRequestHandler
 */
public class AsyncRequestHandlerResponse 
{
    
    private static final AsyncRequestHandlerResponse NOT_HANDLED = 
            new AsyncRequestHandlerResponse(false);
    
    final private boolean async;
    
    final private Executor executor;
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private AsyncListener listener;
    
    private long timeout;
    
    /**
     * Creates an instance with a given {@link Executor}. It cannot be null.
     * If you want an instance with a non-async response, use {@link #notHandled()} instead.
     * @param executor a non-null {@link Executor}.
     */
    public AsyncRequestHandlerResponse(Executor executor)
    {
        this(true, executor);
    }
    
    private AsyncRequestHandlerResponse(boolean async, Executor executor)
    {
        Objects.requireNonNull(executor, "Parameter executor cannot be null");
        this.async = async;
        this.executor = executor;
    }
    
    private AsyncRequestHandlerResponse(boolean async)
    {
        this.async = async;
        executor = null;
    }

    /**
     * Defines a different request and response to be passed to {@link HttpServletRequest#startAsync(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}.
     * Both cannot be null.
     */
    public AsyncRequestHandlerResponse with(HttpServletRequest request, HttpServletResponse response)
    {
        Objects.requireNonNull(request, "Parameter request cannot be null");
        Objects.requireNonNull(response, "Parameter response cannot be null");
        this.request = request;
        this.response = response;
        return this;
    }

    /**
     * Defines a listener to be added to the asynchronous request. It cannot be null.
     */
    public AsyncRequestHandlerResponse with(AsyncListener listener)
    {
        Objects.requireNonNull(listener, "Parameter listener cannot be null");
        this.listener = listener;
        return this;
    }
    
    /**
     * Sets the timeout for this asynchronous request in milliseconds.
     */
    public AsyncRequestHandlerResponse withTimeout(long timeout)
    {
        this.timeout = timeout;
        return this;
    }
    
    /**
     * Returns a response saying this {@linkplain AsyncRequestHandler} doesn't handle this request.
     * @return an {@link AsyncRequestHandlerResponse}.
     */
    public static AsyncRequestHandlerResponse notHandled()
    {
        return NOT_HANDLED;
    }

    /**
     * Returns whether the request should be processed asynchronously or not.
     */
    public boolean isAsync() 
    {
        return async;
    }

    /**    
     * Returns the {@link Executor} to be used to process the request.
     */
    public Executor getExecutor() 
    {
        return executor;
    }

    /**
     * Returns the request to be used with {@link HttpServletRequest#startAsync()} or null.
     */
    public HttpServletRequest getRequest() 
    {
        return request;
    }

    /**
     * Returns the response to be used with {@link HttpServletRequest#startAsync()} or null.
     */
    public HttpServletResponse getResponse() 
    {
        return response;
    }

    /**
     * Returns the listener to be added to the asynchronous request or null.
     */
    public AsyncListener getListener() 
    {
        return listener;
    }
    
    /**
     * Returns whether a request and a response were set in this object.
     */
    public boolean isHasRequestAndResponse()
    {
        return request != null && response != null;
    }

    /**
     * Returns the timeout, in milliseconds, for the asynchronous request. Any value
     * less than or equal zero is considered not having set a timeout.
     */
    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() 
    {
        return "AsyncRequestHandlerResponse [async=" + async + ", executor=" + executor + ", request=" + request + ", response=" + response + ", listener="
                + listener + "]";
    }
    
}
