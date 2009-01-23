// Copyright 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.internal.services.ComponentResourcesOperation;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;

import java.util.List;
import java.util.Locale;

/**
 * Base class for workers that automatically inlcude assets in the page (via methods on {@link
 * org.apache.tapestry5.RenderSupport}).
 */
public abstract class AbstractIncludeAssetWorker implements ComponentClassTransformWorker
{
    private final AssetSource assetSource;

    private final SymbolSource symbolSource;

    public AbstractIncludeAssetWorker(AssetSource assetSource, SymbolSource symbolSource)
    {
        this.assetSource = assetSource;
        this.symbolSource = symbolSource;
    }

    /**
     * Expands symbols in the path, then adds an operation into the setup render phase of the component. Ultimately,
     * {@link #handleAsset(org.apache.tapestry5.Asset)} will be invoked for each asset (dervied from assetPaths).
     *
     * @param transformation transformation process for component
     * @param model          component model for component
     * @param assetPaths     raw paths to be converted to assets
     */
    protected final void addOperationForAssetPaths(ClassTransformation transformation,
                                                   MutableComponentModel model, String[] assetPaths)
    {
        final Resource baseResource = model.getBaseResource();
        final List<String> paths = CollectionFactory.newList();

        for (String value : assetPaths)
        {
            String expanded = symbolSource.expandSymbols(value);

            paths.add(expanded);
        }

        ComponentResourcesOperation op = new ComponentResourcesOperation()
        {
            // Remember that ONE instances of this op will be injected into EVERY instance
            // of the component ... that means that we can't do any aggresive caching
            // inside the operation (the operation must be threadsafe).

            public void perform(ComponentResources resources)
            {
                Locale locale = resources.getLocale();

                for (String assetPath : paths)
                {
                    Asset asset = assetSource.getAsset(baseResource, assetPath, locale);

                    handleAsset(asset);
                }
            }
        };

        String opFieldName = transformation.addInjectedField(ComponentResourcesOperation.class, "operation", op);

        String resourcesName = transformation.getResourcesFieldName();

        String body = String.format("%s.perform(%s);", opFieldName, resourcesName);

        // This is what I like about this approach; the injected body is tiny.  The downside is that
        // the object that gets injected is hard to test, hard enough that we'll just concentrate on
        // the integration test, thank you.

        transformation.extendMethod(TransformConstants.SETUP_RENDER_SIGNATURE, body);

        model.addRenderPhase(SetupRender.class);
    }

    /**
     * Invoked, from the component's setup render phase, for each asset. This method must be threadsafe.  Most
     * implementation pass the asset to a particular method of {@link org.apache.tapestry5.RenderSupport}.
     *
     * @param asset to be processed
     */
    protected abstract void handleAsset(Asset asset);
}
