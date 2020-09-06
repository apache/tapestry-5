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

package org.apache.tapestry5.http.internal.services;

import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

public class BaseURLSourceImpl implements BaseURLSource
{
    private final Request request;

    private String hostname;
    private int hostPort;
    private int secureHostPort;

    public BaseURLSourceImpl(Request request, @Inject @Symbol(TapestryHttpSymbolConstants.HOSTNAME) String hostname,
                             @Symbol(TapestryHttpSymbolConstants.HOSTPORT) int hostPort, @Symbol(TapestryHttpSymbolConstants.HOSTPORT_SECURE) int secureHostPort)
    {
        this.request = request;
        this.hostname = hostname;
        this.hostPort = hostPort;
        this.secureHostPort = secureHostPort;
    }

    public String getBaseURL(boolean secure)
    {
        return String.format("%s://%s%s",
                secure ? "https" : "http",
                hostname(),
                portExtension(secure));
    }

    private String portExtension(boolean secure)
    {
        int configuredPort = secure ? secureHostPort : hostPort;

        // The default for the ports is 0, which means to use Request.serverPort. That's mostly
        // for development.
        if (configuredPort <= 0 && secure == request.isSecure())
        {
            configuredPort = request.getServerPort();
        }

        int expectedPort = secure ? 443 : 80;

        if (configuredPort == expectedPort || configuredPort <= 0)
        {
            return "";
        }

        return ":" + configuredPort;
    }

    private String hostname()
    {

        if (InternalUtils.isBlank(hostname))
        {
            return request.getServerName();
        }

        // This is common in some PaaS deployments, such as Heroku, where the port is passed in as
        // and environment variable.

        if (this.hostname.startsWith("$"))
        {
            return System.getenv(hostname.substring(1));
        }

        return hostname;
    }
}
