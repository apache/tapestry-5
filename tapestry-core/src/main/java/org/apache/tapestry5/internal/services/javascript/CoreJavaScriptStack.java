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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * JavaScriptStack for core components.
 *
 * @since 5.2.0
 */
public class CoreJavaScriptStack implements JavaScriptStack {
  private final boolean productionMode;

  private final SymbolSource symbolSource;

  private final AssetSource assetSource;

  private final ThreadLocale threadLocale;

  private final List<Asset> javaScriptStack, stylesheetStack;

  private static final String ROOT = "org/apache/tapestry5";

  private static final String[] CORE_JAVASCRIPT = new String[]
          {
                  // Core scripts added to any page that uses scripting

                  "${tapestry.underscore}",

                  "${tapestry.scriptaculous}/prototype.js",

                  "${tapestry.scriptaculous}/scriptaculous.js",

                  "${tapestry.scriptaculous}/effects.js",

                  // Below uses functions defined by the prior three.

                  // Order is important, there are some dependencies
                  // going on here. Switching over to a more managed module system
                  // is starting to look like a really nice idea!

                  ROOT + "/t5-core.js",

                  ROOT + "/t5-spi.js",

                  ROOT + "/t5-prototype.js",

                  ROOT + "/t5-init.js",

                  ROOT + "/t5-pubsub.js",

                  ROOT + "/t5-events.js",

                  ROOT + "/t5-dom.js",

                  ROOT + "/t5-console.js",

                  ROOT + "/t5-ajax.js",

                  ROOT + "/t5-formfragment.js",

                  ROOT + "/t5-alerts.js",

                  ROOT + "/tapestry.js",

                  ROOT + "/tapestry-console.js",

                  ROOT + "/tree.js",
          };

  // Because of changes to the logic of how stylesheets get incorporated, the default stylesheet
  // was removed, the logic for it is now in TapestryModule.contributeMarkupRenderer().

  private static final String[] CORE_STYLESHEET = new String[]
          {
                  ROOT + "/tapestry-console.css",

                  ROOT + "/t5-alerts.css",

                  ROOT + "/tree.css"
          };

  public CoreJavaScriptStack(
          @Symbol(SymbolConstants.PRODUCTION_MODE)
          boolean productionMode,

          SymbolSource symbolSource,

          AssetSource assetSource,

          ThreadLocale threadLocale) {
    this.symbolSource = symbolSource;
    this.productionMode = productionMode;
    this.assetSource = assetSource;
    this.threadLocale = threadLocale;

    javaScriptStack = convertToAssets(CORE_JAVASCRIPT);
    stylesheetStack = convertToAssets(CORE_STYLESHEET);
  }

  public String getInitialization() {
    return productionMode ? null : "Tapestry.DEBUG_ENABLED = true;";
  }

  public List<String> getStacks() {
    return Collections.emptyList();
  }

  private List<Asset> convertToAssets(String[] paths) {
    List<Asset> assets = CollectionFactory.newList();

    for (String path : paths) {
      assets.add(expand(path, null));
    }

    return Collections.unmodifiableList(assets);
  }

  private Asset expand(String path, Locale locale) {
    String expanded = symbolSource.expandSymbols(path);

    return assetSource.getAsset(null, expanded, locale);
  }

  public List<Asset> getJavaScriptLibraries() {
    Asset messages = assetSource.getAsset(null, ROOT + "/tapestry-messages.js", threadLocale.getLocale());

    return createStack(javaScriptStack, messages).toList();
  }

  public List<StylesheetLink> getStylesheets() {
    return createStack(stylesheetStack).map(TapestryInternalUtils.assetToStylesheetLink)
            .toList();
  }

  private Flow<Asset> createStack(List<Asset> stack, Asset... assets) {
    return F.flow(stack).append(assets);
  }
}
