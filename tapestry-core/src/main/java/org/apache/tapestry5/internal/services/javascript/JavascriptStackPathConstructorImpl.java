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
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.javascript.JavascriptStack;
import org.apache.tapestry5.services.javascript.JavascriptStackSource;

public class JavascriptStackPathConstructorImpl implements JavascriptStackPathConstructor
{
    private final ThreadLocale threadLocale;

    private final AssetPathConstructor assetPathConstructor;

    private final JavascriptStackSource javascriptStackSource;

    private final boolean combineScripts;

    private final Mapper<Asset, String> toPath = new Mapper<Asset, String>()
    {
        public String map(Asset input)
        {
            return input.toClientURL();
        }
    };

    public JavascriptStackPathConstructorImpl(ThreadLocale threadLocale, AssetPathConstructor assetPathConstructor,
            JavascriptStackSource javascriptStackSource,

            @Symbol(SymbolConstants.COMBINE_SCRIPTS)
            boolean combineScripts)
    {
        this.threadLocale = threadLocale;
        this.assetPathConstructor = assetPathConstructor;
        this.javascriptStackSource = javascriptStackSource;
        this.combineScripts = combineScripts;
    }

    public List<String> constructPathsForJavascriptStack(String stackName)
    {
        JavascriptStack stack = javascriptStackSource.getStack(stackName);

        List<Asset> assets = stack.getJavascriptLibraries();

        if (assets.size() > 1 && combineScripts)
            return combinedStackURL(stackName);

        return toPaths(assets);
    }

    private List<String> toPaths(List<Asset> assets)
    {
        return F.map(toPath, assets);
    }

    private List<String> combinedStackURL(String stackName)
    {
        String path = String.format("%s/%s.js", threadLocale.getLocale().toString(), stackName);

        String stackURL = assetPathConstructor.constructAssetPath(RequestConstants.STACK_FOLDER, path);

        return CollectionFactory.newList(stackURL);
    }

}
