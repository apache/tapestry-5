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

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry.Asset;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.ioc.internal.util.IdAllocator;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.services.AssetSource;

public class PageRenderSupportImpl implements PageRenderSupport
{
    private final IdAllocator _idAllocator = new IdAllocator();

    private final DocumentHeadBuilder _builder;

    private final SymbolSource _symbolSource;

    private final AssetSource _assetSource;

    private final List<String> _coreScripts;

    private boolean _coreAssetsAdded;

    /**
     * @param builder
     *            Used to assemble JavaScript includes and snippets
     * @param symbolSource
     *            Used to example symbols (in
     *            {@linkplain #addClasspathScriptLink(String...) in classpath scripts)
     * @param assetSource
     *            Used to convert classpath scripts to {@link Asset}s
     * @param coreScripts
     *            core scripts (evaluated as classpaths scripts) that are added to any page that
     *            includes a script link or script block
     */
    public PageRenderSupportImpl(DocumentHeadBuilder builder, SymbolSource symbolSource,
            AssetSource assetSource, String... coreScripts)
    {
        _builder = builder;
        _symbolSource = symbolSource;
        _assetSource = assetSource;

        _coreScripts = Arrays.asList(coreScripts);
    }

    public String allocateClientId(String id)
    {
        return _idAllocator.allocateId(id);
    }

    public void addScriptLink(Asset... scriptAssets)
    {
        addCore();

        for (Asset asset : scriptAssets)
        {
            notNull(asset, "scriptAsset");

            _builder.addScriptLink(asset.toClientURL());
        }
    }

    public void addClasspathScriptLink(String... classpaths)
    {
        addCore();

        for (String path : classpaths)
            addScriptLinkFromClasspath(path);
    }

    private void addScriptLinkFromClasspath(String path)
    {
        String expanded = _symbolSource.expandSymbols(path);

        Asset asset = _assetSource.findAsset(null, expanded, null);

        _builder.addScriptLink(asset.toClientURL());
    }

    public void addScript(String format, Object... arguments)
    {
        notNull(format, "format");

        addCore();

        String script = format(format, arguments);

        _builder.addScript(script);
    }

    public void addStylesheetLink(Asset stylesheet, String media)
    {
        notNull(stylesheet, "stylesheet");

        _builder.addStylesheetLink(stylesheet.toClientURL(), media);
    }

    private void addCore()
    {
        if (!_coreAssetsAdded)
        {
            for (String path : _coreScripts)
                addScriptLinkFromClasspath(path);

            _coreAssetsAdded = true;
        }
    }
}
