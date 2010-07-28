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

package org.apache.tapestry5.internal.services.javascript;

import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ClientInfrastructure;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StylesheetLink;

/**
 * JavascriptStack based on the information retrieved from {@link ClientInfrastructure}.
 * 
 * @since 5.2.0
 */
public class CoreJavascriptStack implements JavaScriptStack
{
    private final ClientInfrastructure clientInfrastructure;

    private final boolean productionMode;

    public CoreJavascriptStack(ClientInfrastructure clientInfrastructure,

    @Symbol(SymbolConstants.PRODUCTION_MODE)
    boolean productionMode)
    {
        this.clientInfrastructure = clientInfrastructure;
        this.productionMode = productionMode;
    }

    public String getInitialization()
    {
        return productionMode ? null : "Tapestry.DEBUG_ENABLED = true;";
    }

    public List<Asset> getJavaScriptLibraries()
    {
        return clientInfrastructure.getJavascriptStack();
    }

    public List<StylesheetLink> getStylesheets()
    {
        return F.flow(clientInfrastructure.getStylesheetStack()).map(TapestryInternalUtils.assetToStylesheetLink)
                .toList();
    }

}
