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
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.AssetPathConverter;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.StreamableResource;

import java.io.IOException;

public class AssetPathConstructorImpl implements AssetPathConstructor
{
    private final Request request;

    private final String uncompressedPrefix, compressedPrefix;

    private final BaseURLSource baseURLSource;

    private final AssetPathConverter pathConverter;

    private final boolean fullyQualified;

    public AssetPathConstructorImpl(Request request,
                                    BaseURLSource baseURLSource,

                                    @Symbol(SymbolConstants.ASSET_URL_FULL_QUALIFIED)
                                    boolean fullyQualified,

                                    @Symbol(SymbolConstants.ASSET_PATH_PREFIX)
                                    String uncompressedAssetPrefix,

                                    @Symbol(SymbolConstants.COMPRESSED_ASSET_PATH_PREFIX)
                                    String compressedAssetPrefix,

                                    PathConstructor pathConstructor,

                                    AssetPathConverter pathConverter)
    {
        this.request = request;
        this.baseURLSource = baseURLSource;

        this.fullyQualified = fullyQualified;
        this.pathConverter = pathConverter;

        uncompressedPrefix = pathConstructor.constructClientPath(uncompressedAssetPrefix, "");
        compressedPrefix = pathConstructor.constructClientPath(compressedAssetPrefix, "");
    }

    public String constructAssetPath(String virtualFolder, String path, StreamableResource resource) throws IOException
    {
        assert InternalUtils.isNonBlank(path);

        StringBuilder builder = create(resource.getCompression() == CompressionStatus.COMPRESSED, virtualFolder);
        builder.append("/");

        builder.append(resource.getChecksum());

        builder.append('/');
        builder.append(path);

        return finish(builder);
    }

    public String constructAssetPath(String virtualFolder, boolean compressed)
    {
        return finish(create(compressed, virtualFolder));
    }

    private String finish(StringBuilder builder)
    {
        return pathConverter.convertAssetPath(builder.toString());
    }

    private StringBuilder create(boolean compress, String virtualFolder)
    {
        assert InternalUtils.isNonBlank(virtualFolder);

        StringBuilder builder = new StringBuilder();

        if (fullyQualified)
        {
            builder.append(baseURLSource.getBaseURL(request.isSecure()));
        }

        builder.append(compress ? compressedPrefix : uncompressedPrefix);

        return builder.append(virtualFolder);
    }
}
