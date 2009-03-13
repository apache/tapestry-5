// Copyright 2006, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

import java.io.IOException;

/**
 * Service invoked when an uncaught exception occurs. The error handler is responsible for providing a response to the
 * user to describe the error.
 */
public interface RequestExceptionHandler
{
    /**
     * Reponsible for handling the error <em>in some way</em> and providing <em>some response</em> to the client. A
     * default implementation may render an error response page.
     * <p/>
     * <p/>
     * The handler is also responsible for setting the response status and the X-Tapestry-ErrorMessage response header.
     * These are very important in Ajax requests to allow the client-side logic to detect the error and present it to
     * the user.
     *
     * @param exception uncaught exception to be reported
     * @throws IOException
     */
    void handleRequestException(Throwable exception) throws IOException;
}
