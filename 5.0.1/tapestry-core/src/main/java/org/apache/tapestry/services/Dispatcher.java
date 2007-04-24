// Copyright 2006 The Apache Software Foundation
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
 * A dispatcher is responsible for recognizing an incoming request.  Dispatchers form a chain of command.
 */
public interface Dispatcher
{
    /**
     * Analyzes the incoming request and performs an appropriate operation for each.
     * 
     * @return true if a response was delivered
     */
    boolean dispatch(Request request, Response response) throws IOException;
}
