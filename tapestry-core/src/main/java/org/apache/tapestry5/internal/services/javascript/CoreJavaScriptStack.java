// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;

/**
 * JavaScriptStack for core components.
 * 
 * @since 5.2.0
 */
public class CoreJavaScriptStack implements JavaScriptStack
{
    private final boolean productionMode;

    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    private final ThreadLocale threadLocale;

    private final List<Asset> javaScriptStack, stylesheetStack;

    private final Asset consoleJavaScript, consoleStylesheet;

    private final boolean isBlackbirdEnabled;

    private static final String ROOT = "org/apache/tapestry5";

    private static final String[] CORE_JAVASCRIPT = new String[]
    {
            // Core scripts added to any page that uses scripting

            "${tapestry.scriptaculous}/prototype.js",

            "${tapestry.scriptaculous}/scriptaculous.js",

            "${tapestry.scriptaculous}/effects.js",

            // Uses functions defined by the prior three.
            // Order is important, there are some dependencies
            // going on here.

            ROOT + "/t5-core.js",

            ROOT + "/t5-func.js",

            ROOT + "/t5-spi.js",

            ROOT + "/t5-prototype.js",

            ROOT + "/t5-arrays.js",

            ROOT + "/t5-init.js",

            ROOT + "/t5-pubsub.js",

            ROOT + "/t5-dom.js",

            ROOT + "/t5-ajax.js",

            ROOT + "/tapestry.js",

            ROOT + "/tree.js" };

    // Because of changes to the logic of how stylesheets get incorporated, the default stylesheet
    // was removed, the logic for it is now in TapestryModule.contributeMarkupRenderer().

    private static final String[] CORE_STYLESHEET = new String[]
    { ROOT + "/tree.css" };

    public CoreJavaScriptStack(@Symbol(SymbolConstants.PRODUCTION_MODE)
    boolean productionMode,

    SymbolSource symbolSource,

    AssetSource assetSource,

    ThreadLocale threadLocale,

    @Symbol(SymbolConstants.BLACKBIRD_ENABLED)
    boolean isBlackbirdEnabled)
    {
        this.symbolSource = symbolSource;
        this.productionMode = productionMode;
        this.assetSource = assetSource;
        this.threadLocale = threadLocale;
        this.isBlackbirdEnabled = isBlackbirdEnabled;

        javaScriptStack = convertToAssets(CORE_JAVASCRIPT);
        stylesheetStack = convertToAssets(CORE_STYLESHEET);

        consoleJavaScript = expand("${tapestry.blackbird}/blackbird.js", ROOT + "/tapestry-console.js", null);
        consoleStylesheet = expand("${tapestry.blackbird}/blackbird.css", ROOT + "/tapestry-console.css", null);
    }

    public String getInitialization()
    {
        return productionMode ? null : "Tapestry.DEBUG_ENABLED = true;";
    }

    public List<String> getStacks()
    {
        return Collections.emptyList();
    }

    private List<Asset> convertToAssets(String[] paths)
    {
        List<Asset> assets = CollectionFactory.newList();

        for (String path : paths)
        {
            assets.add(expand(path, null));
        }

        return Collections.unmodifiableList(assets);
    }

    private Asset expand(String path, Locale locale)
    {
        String expanded = symbolSource.expandSymbols(path);

        return assetSource.getAsset(null, expanded, locale);
    }

    private Asset expand(String blackbirdPath, String consolePath, Locale locale)
    {
        String path = isBlackbirdEnabled ? blackbirdPath : consolePath;

        return expand(path, locale);
    }

    public List<Asset> getJavaScriptLibraries()
    {
        Asset messages = assetSource.getAsset(null, ROOT + "/tapestry-messages.js", threadLocale.getLocale());

        return createStack(javaScriptStack, messages, consoleJavaScript).toList();
    }

    public List<StylesheetLink> getStylesheets()
    {
        return createStack(stylesheetStack, consoleStylesheet).map(TapestryInternalUtils.assetToStylesheetLink)
                .toList();
    }

    private Flow<Asset> createStack(List<Asset> stack, Asset... assets)
    {
        return F.flow(stack).append(assets);
    }
}
