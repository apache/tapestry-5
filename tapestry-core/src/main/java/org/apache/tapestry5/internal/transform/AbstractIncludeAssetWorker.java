// Copyright 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformField;
import org.apache.tapestry5.services.javascript.JavascriptSupport;

/**
 * Base class for workers that automatically include assets in the page (via methods on
 * {@link org.apache.tapestry5.RenderSupport}).
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
     * Expands symbols in the path, then adds an operation into the setup render phase of the
     * component. Ultimately, {@link #handleAsset(org.apache.tapestry5.Asset)} will be invoked for
     * each asset (dervied from assetPaths).
     * 
     * @param transformation
     *            transformation process for component
     * @param model
     *            component model for component
     * @param assetPaths
     *            raw paths to be converted to assets
     */
    protected final void addOperationForAssetPaths(ClassTransformation transformation, MutableComponentModel model,
            String[] assetPaths)
    {
        List<String> expandedPaths = expandSymbolsInPaths(assetPaths);

        // Since every instance of the component may be in a different locale, every instance
        // will have a field to store its localized list of assets. There's room to do some
        // sharing/caching perhaps.

        FieldAccess access = createFieldForAssets(transformation);

        // Inside the component's page loaded callback, convert the asset paths to assets

        storeLocalizedAssetsAtPageLoad(transformation, expandedPaths, access);

        handleAssetsDuringSetupRenderPhase(transformation, model, access);
    }

    private FieldAccess createFieldForAssets(ClassTransformation transformation)
    {
        String fieldName = transformation.addField(Modifier.PRIVATE, List.class.getName(), "includedAssets");

        return transformation.getField(fieldName).getAccess();
    }

    private void handleAssetsDuringSetupRenderPhase(ClassTransformation transformation, MutableComponentModel model,
            final FieldAccess access)
    {
        ComponentMethodAdvice advice = new ComponentMethodAdvice()
        {

            @SuppressWarnings("unchecked")
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                List<Asset> assets = (List<Asset>) access.read(invocation.getInstance());

                handleAssets(assets);
            }
        };

        transformation.getOrCreateMethod(TransformConstants.SETUP_RENDER_SIGNATURE).addAdvice(advice);

        model.addRenderPhase(SetupRender.class);
    }

    private void storeLocalizedAssetsAtPageLoad(ClassTransformation transformation, final List<String> expandedPaths,
            final FieldAccess access)
    {
        ComponentMethodAdvice advice = new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                ComponentResources resources = invocation.getComponentResources();

                List<Asset> assets = convertPathsToAssets(resources, expandedPaths);

                access.write(invocation.getInstance(), assets);
            }
        };

        transformation.getOrCreateMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE).addAdvice(advice);
    }

    private List<String> expandSymbolsInPaths(String[] paths)
    {
        List<String> result = CollectionFactory.newList();

        for (String path : paths)
        {
            String expanded = symbolSource.expandSymbols(path);

            result.add(expanded);
        }

        return result;
    }

    private List<Asset> convertPathsToAssets(ComponentResources resources, List<String> assetPaths)
    {
        Resource baseResource = resources.getComponentModel().getBaseResource();

        List<Asset> result = CollectionFactory.newList();

        Locale locale = resources.getLocale();

        for (String assetPath : assetPaths)
        {
            Asset asset = assetSource.getAsset(baseResource, assetPath, locale);

            result.add(asset);
        }

        return result;
    }

    private void handleAssets(List<Asset> assets)
    {
        for (Asset asset : assets)
        {
            handleAsset(asset);
        }
    }

    /**
     * Invoked, from the component's setup render phase, for each asset. This method must be
     * threadsafe. Most
     * implementations pass the asset to a particular method of {@link org.apache.tapestry5.RenderSupport} or
     * {@link JavascriptSupport}.
     * 
     * @param asset
     *            to be processed
     */
    protected abstract void handleAsset(Asset asset);
}
