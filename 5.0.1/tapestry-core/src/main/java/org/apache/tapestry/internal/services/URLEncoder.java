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

package org.apache.tapestry.internal.services;

public interface URLEncoder
{
    /**
     * Encodes the URL, ensuring that a session id is included (if a session exists, and as
     * necessary depending on the client browser's use of cookies).
     * 
     * @param URL
     * @return the same URL or a different one with additional information to track the user session
     */
    String encodeURL(String URL);

    /**
     * Encodes the URL for use as a redirect, ensuring that a session id is included (if a session
     * exists, and as necessary depending on the client browser's use of cookies).
     * 
     * @param URL
     * @return the same URL or a different one with additional information to track the user session
     */
    String encodeRedirectURL(String URL);

}
