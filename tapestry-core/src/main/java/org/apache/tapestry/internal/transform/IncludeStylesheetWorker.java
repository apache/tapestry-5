// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.Asset;
import org.apache.tapestry.RenderSupport;
import org.apache.tapestry.annotation.IncludeStylesheet;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.ClassTransformation;

/**
 * Recognizes the {@link org.apache.tapestry.annotation.IncludeStylesheet} annotation and ensures that {@link
 * org.apache.tapestry.RenderSupport#addStylesheetLink(org.apache.tapestry.Asset, String)} is invoked.
 */
public class IncludeStylesheetWorker extends AbstractIncludeAssetWorker
{
    private final RenderSupport renderSupport;

    public IncludeStylesheetWorker(AssetSource assetSource, RenderSupport renderSupport,
                                   SymbolSource symbolSource)
    {
        super(assetSource, symbolSource);

        this.renderSupport = renderSupport;
    }

    public void transform(ClassTransformation transformation, final MutableComponentModel model)
    {
        IncludeStylesheet annotation = transformation.getAnnotation(IncludeStylesheet.class);

        if (annotation != null) addOperationForAssetPaths(transformation, model, annotation.value());
    }


    protected void handleAsset(Asset asset)
    {
        renderSupport.addStylesheetLink(asset, null);
    }
}
