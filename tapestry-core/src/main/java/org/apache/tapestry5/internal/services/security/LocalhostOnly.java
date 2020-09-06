// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.security;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;

/**
 * Standard analyzer that places requests from the "localhost", "127.0.0.1", "0:0:0:0:0:0:0:1%0", or :"0:0:0:0:0:0:0:1" onto the white list.
 *
 * @since 5.3
 */
public class LocalhostOnly implements WhitelistAnalyzer
{
    public boolean isRequestOnWhitelist(Request request)
    {
        String remoteHost = request.getRemoteHost();

        return remoteHost.equals("localhost") || remoteHost.equals("127.0.0.1") || remoteHost.equals("0:0:0:0:0:0:0:1%0") || remoteHost.equals("0:0:0:0:0:0:0:1");
    }
}
