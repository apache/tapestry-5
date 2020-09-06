// Copyright 2011 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;

import java.util.List;

public class ClientWhitelistImpl implements ClientWhitelist
{
    private final Request request;

    private final WhitelistAnalyzer analyzer;

    public ClientWhitelistImpl(Request request, ChainBuilder chainBuilder, List<WhitelistAnalyzer> configuration)
    {
        this.request = request;

        analyzer = chainBuilder.build(WhitelistAnalyzer.class, configuration);
    }

    public boolean isClientRequestOnWhitelist()
    {
        return analyzer.isRequestOnWhitelist(request);
    }
}
