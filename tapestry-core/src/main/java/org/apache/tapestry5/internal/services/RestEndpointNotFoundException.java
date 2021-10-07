// Copyright 2021 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * Exception used when a request is made to a page with REST endpoint event handlers
 * but doesn't match any of them.
 */
public class RestEndpointNotFoundException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of this class.
     * @param message A {@linkplain String} contaning an error message.
     */
    public RestEndpointNotFoundException(String message) 
    {
        super(message);
    }
    
}
