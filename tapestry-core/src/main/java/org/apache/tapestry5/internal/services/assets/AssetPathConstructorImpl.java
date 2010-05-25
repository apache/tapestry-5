// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.assets.AssetPathConstructor;

public class AssetPathConstructorImpl implements AssetPathConstructor
{
    private final Request request;

    private final String prefix;

    public AssetPathConstructorImpl(Request request,

    @Symbol(SymbolConstants.APPLICATION_VERSION)
    String applicationVersion)
    {
        this.request = request;
        this.prefix = RequestConstants.ASSET_PATH_PREFIX + applicationVersion + "/";
    }

    public String constructAssetPath(String virtualFolder, String path)
    {
        StringBuilder builder = new StringBuilder(request.getContextPath());
        builder.append(prefix);
        builder.append(virtualFolder);
        builder.append('/');
        builder.append(path);

        return builder.toString();
    }
}
