// Copyright 2008, 2010, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.Request;

public class BaseURLSourceImpl implements BaseURLSource
{
    private final Request request;
    
    private String hostname;
    private int hostPort;
    private int secureHostPort;

    public BaseURLSourceImpl(Request request, @Inject @Symbol(SymbolConstants.HOSTNAME) String hostname,
	    @Symbol(SymbolConstants.HOSTPORT) int hostPort, @Symbol(SymbolConstants.HOSTPORT_SECURE) int secureHostPort)
    {
        this.request = request;
        this.hostname = hostname;
        this.hostPort = hostPort;
        this.secureHostPort = secureHostPort;
    }

    public String getBaseURL(boolean secure)
    {
        int port = secure ? secureHostPort : hostPort;
        String portSuffix = "";

        if (port <= 0) { 
            port = request.getServerPort();
            int schemeDefaultPort = request.isSecure() ? 443 : 80;
            portSuffix = port == schemeDefaultPort ? "" : ":" + port;
        }
        else if (secure && port != 443) portSuffix = ":" + port;
        else if (port != 80) portSuffix = ":" + port;
        
        String hostname = "".equals(this.hostname) ? request.getServerName() : this.hostname.startsWith("$") ? System.getenv(this.hostname.substring(1)) : this.hostname;
        
        return String.format("%s://%s%s", secure ? "https" : "http", hostname, portSuffix);
    }
}
