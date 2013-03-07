// Copyright 2010, 2011, 2012, 2013 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.AssetPathConstructor;

import java.io.IOException;

public class AssetPathConstructorImpl implements AssetPathConstructor
{
    private final Request request;

    private final String prefix;

    private final BaseURLSource baseURLSource;

    private final AssetChecksumGenerator assetChecksumGenerator;

    private final boolean fullyQualified;

    public AssetPathConstructorImpl(Request request,
                                    BaseURLSource baseURLSource,

                                    @Symbol(SymbolConstants.ASSET_URL_FULL_QUALIFIED)
                                    boolean fullyQualified,

                                    @Symbol(SymbolConstants.ASSET_PATH_PREFIX)
                                    String assetPathPrefix,

                                    PathConstructor pathConstructor,

                                    AssetChecksumGenerator assetChecksumGenerator)
    {
        this.request = request;
        this.baseURLSource = baseURLSource;

        this.fullyQualified = fullyQualified;
        this.assetChecksumGenerator = assetChecksumGenerator;

        prefix = pathConstructor.constructClientPath(assetPathPrefix, "");
    }

    public String constructAssetPath(String virtualFolder, String path, Resource resource)
    {
        assert InternalUtils.isNonBlank(virtualFolder);
        assert path != null;

        StringBuilder builder = new StringBuilder();

        if (fullyQualified)
        {
            builder.append(baseURLSource.getBaseURL(request.isSecure()));
        }

        builder.append(prefix);
        builder.append(virtualFolder);
        builder.append("/");

        try
        {
            builder.append(assetChecksumGenerator.generateChecksum(resource));
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        if (InternalUtils.isNonBlank(path))
        {
            builder.append('/');
            builder.append(path);
        }

        return builder.toString();
    }
}
