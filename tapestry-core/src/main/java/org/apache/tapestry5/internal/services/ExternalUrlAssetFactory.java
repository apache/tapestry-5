// Copyright 2014 The Apache Software Foundation
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.AssetConstants;
import org.apache.tapestry5.services.AssetFactory;

public class ExternalUrlAssetFactory implements AssetFactory {
    
    final private String protocol;
    
    final private UrlResource rootResource;
    
    final private Map<URL, Asset> cache = CollectionFactory.newMap();

    public ExternalUrlAssetFactory(String protocol)
    {
        super();
        this.protocol = protocol;
//        try
//        {
            if (protocol.equals(AssetConstants.PROTOCOL_RELATIVE)) {
                protocol = "http";
            }
            this.rootResource = new UrlResource();
//        }
//        catch (MalformedURLException e)
//        {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public Resource getRootResource()
    {
        return rootResource;
    }

    @Override
    public Asset createAsset(Resource resource)
    {
        final URL url = resource.toURL();
        Asset asset = cache.get(url);
        if (asset == null)
        {
            asset = new UrlAsset(url.toExternalForm(), resource);
            cache.put(url, asset);
        }
        return asset;
    }
    
}
