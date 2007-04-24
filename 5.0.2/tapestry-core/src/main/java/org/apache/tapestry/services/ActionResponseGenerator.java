// Copyright 2007 The Apache Software Foundation
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
 * An object that can generate the final response sent to the client as part of an action request.
 * This is almost always a redirect request.
 * 
 * @see ComponentEventResultProcessor
 */
public interface ActionResponseGenerator
{
    void sendClientResponse(Response response) throws IOException;
}
