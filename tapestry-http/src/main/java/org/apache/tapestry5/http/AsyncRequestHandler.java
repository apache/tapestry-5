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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.internal.AsyncRequestService;

/**
 * <p>
 * Service whose implementations define whether a given request should be handled
 * asynchronously or not and, if yes, which {@link Executor} (usually, a thread pool,
 * but not necessarily) should handle it, possibly different {@link HttpServletRequest}
 * and {@link HttpServletResponse} objects to be used when calling 
 * {@linkplain} HttpServletRequest#startAsync()} and an optional {@linkplain AsyncListener}.
 * <p>
 * <p>
 * If one {@link AsyncRequestHandler} doesn't tells the request should be asynchronous,
 * the next one contributed to {@link AsyncRequestService} will be called
 * and so on until one says the request should be asynchronous or all of them
 * were called and the request will be synchronous.
 * </p>
 * @see AsyncRequestService
 * @see Executor
 * @see ExecutorService
 * @see Executors
 */
public interface AsyncRequestHandler 
{
    
    /**
     * Returns whether this request is handled by this handler. If not,
     * it should return {@link AsyncRequestHandlerResponse#notHandled()}. 
     * @return a non-null {@linkplain AsyncRequestHandlerResponse}.
     */
    AsyncRequestHandlerResponse handle(HttpServletRequest request, HttpServletResponse response);

}
