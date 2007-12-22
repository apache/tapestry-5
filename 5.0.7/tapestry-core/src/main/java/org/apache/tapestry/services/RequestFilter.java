// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.io.IOException;

/**
 * Filter interface for {@link org.apache.tapestry.services.RequestHandler}. Implementations of
 * this interface may be contributed into the RequestHandler service configuration.
 */
public interface RequestFilter
{
    /**
     * Returns true if the request has been handled, false otherwise.
     */
    boolean service(Request request, Response response, RequestHandler handler) throws IOException;

}
