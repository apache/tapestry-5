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

package org.apache.tapestry5;

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.services.RequestExceptionHandler;

import java.io.IOException;
import java.util.List;

/**
 * A contribution to the default {@link RequestExceptionHandler} service, this is mapped to an exception class,
 * allowing class specific (based on an inheritance search) handling of an exception.
 *
 * @see ContextAwareException
 */
public interface ExceptionHandlerAssistant
{
    /**
     * Handles the exception, returning a page class or link to redirect to.
     *
     * @param exception
     *         the exception as thrown
     * @param exceptionContext
     *         a page activation context that is derived from the root-most exception
     * @return either a page class or a {@link Link}; a page will be redirected to, with the exception context
     * as the page activation context
     * @throws IOException
     */
    Object handleRequestException(Throwable exception, List<Object> exceptionContext) throws IOException;
}
