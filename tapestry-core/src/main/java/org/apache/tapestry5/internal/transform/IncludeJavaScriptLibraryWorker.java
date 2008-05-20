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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClassTransformation;

/**
 * Recognizes the {@link org.apache.tapestry5.annotations.IncludeJavaScriptLibrary} annotation, and ensures that {@link
 * org.apache.tapestry5.RenderSupport#addScriptLink(org.apache.tapestry5.Asset[])} is invoked.
 */
public class IncludeJavaScriptLibraryWorker extends AbstractIncludeAssetWorker
{
    private final RenderSupport renderSupport;

    public IncludeJavaScriptLibraryWorker(AssetSource assetSource, RenderSupport renderSupport,
                                          SymbolSource symbolSource)
    {
        super(assetSource, symbolSource);

        this.renderSupport = renderSupport;
    }

    public void transform(ClassTransformation transformation, final MutableComponentModel model)
    {
        IncludeJavaScriptLibrary annotation = transformation.getAnnotation(IncludeJavaScriptLibrary.class);

        if (annotation != null) addOperationForAssetPaths(transformation, model, annotation.value());
    }

    protected void handleAsset(Asset asset)
    {
        renderSupport.addScriptLink(asset);
    }
}
