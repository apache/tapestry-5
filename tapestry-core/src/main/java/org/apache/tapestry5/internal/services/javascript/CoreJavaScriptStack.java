// Copyright 2010-2012 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * JavaScriptStack for core components.
 *
 * @since 5.2.0
 */
public class CoreJavaScriptStack implements JavaScriptStack
{
    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    private final Flow<Asset> javaScriptStack, stylesheetStack;

    private static final String ROOT = "org/apache/tapestry5";

    private static final String[] CORE_JAVASCRIPT = new String[]
            {
                    // Core scripts added to any page that uses scripting

                    // TODO: Only include these two when in compatibility mode ...
                    // after the t5-* and tapestry libraries have been stripped
                    // of Scriptaculous code.

                    "${tapestry.scriptaculous}/scriptaculous.js",

                    "${tapestry.scriptaculous}/effects.js",

                    // TODO: Include jQuery based on configuration
                    // .... probably be generally available as module "$"

                    // TODO: Possibly extract prototype/scriptaculous/jquery from the stack
                    // (as has been done with Underscore), and convert to a shimmed module.
            };

    // Because of changes to the logic of how stylesheets get incorporated, the default stylesheet
    // was removed, the logic for it is now in TapestryModule.contributeMarkupRenderer().

    private static final String[] CORE_STYLESHEET = new String[]
            {
                    "${tapestry.bootstrap-root}/css/bootstrap.css",

                    // The following are mostly temporary:

                    ROOT + "/tapestry-console.css",

                    ROOT + "/t5-alerts.css",

                    ROOT + "/tree.css"
            };

    public CoreJavaScriptStack(SymbolSource symbolSource, AssetSource assetSource)
    {
        this.symbolSource = symbolSource;
        this.assetSource = assetSource;

        Flow<String> coreJavascript = F.flow(CORE_JAVASCRIPT);

        javaScriptStack = convertToAssets(coreJavascript);
        stylesheetStack = convertToAssets(F.flow(CORE_STYLESHEET));

    }

    public String getInitialization()
    {
        return null;
    }

    public List<String> getStacks()
    {
        return Collections.emptyList();
    }

    private Flow<Asset> convertToAssets(Flow<String> paths)
    {
        return paths.map(new Mapper<String, Asset>()
        {
            @Override
            public Asset map(String element)
            {
                return expand(element, null);
            }
        });
    }

    private Asset expand(String path, Locale locale)
    {
        String expanded = symbolSource.expandSymbols(path);

        return assetSource.getAsset(null, expanded, locale);
    }

    public List<Asset> getJavaScriptLibraries()
    {
        return javaScriptStack.toList();
    }

    public List<StylesheetLink> getStylesheets()
    {
        return stylesheetStack.map(TapestryInternalUtils.assetToStylesheetLink).toList();
    }
}
