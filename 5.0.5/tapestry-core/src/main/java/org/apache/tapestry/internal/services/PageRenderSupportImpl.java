// Copyright 2006, 2007 The Apache Software Foundation
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

import static java.lang.String.format;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import org.apache.tapestry.Asset;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.services.AssetSource;

public class PageRenderSupportImpl implements PageRenderSupport
{
    private final IdAllocator _idAllocator = new IdAllocator();

    private final DocumentScriptBuilder _builder;

    private final SymbolSource _symbolSource;

    private final AssetSource _assetSource;

    public PageRenderSupportImpl(DocumentScriptBuilder builder, SymbolSource symbolSource,
            AssetSource assetSource)
    {
        _builder = builder;
        _symbolSource = symbolSource;
        _assetSource = assetSource;
    }

    public String allocateClientId(String id)
    {
        return _idAllocator.allocateId(id);
    }

    public void addScriptLink(Asset... scriptAssets)
    {
        for (Asset asset : scriptAssets)
        {
            notNull(asset, "scriptAsset");

            _builder.addScriptLink(asset.toClientURL());
        }
    }

    public void addClasspathScriptLink(String... classpaths)
    {
        for (String path : classpaths)
        {
            String expanded = _symbolSource.expandSymbols(path);

            Asset asset = _assetSource.findAsset(null, expanded, null);

            _builder.addScriptLink(asset.toClientURL());
        }
    }

    public void addScript(String format, Object... arguments)
    {
        String script = format(format, arguments);

        _builder.addScript(script);
    }

}
