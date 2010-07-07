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

package org.apache.tapestry5.internal.transform;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.javascript.JavascriptSupport;

/**
 * Implements the {@link Import} annotation, both at the class and at the method level.
 * 
 * @since 5.2.0
 */
public class ImportWorker implements ComponentClassTransformWorker
{
    private final JavascriptSupport javascriptSupport;

    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    private final Worker<Asset> importLibrary = new Worker<Asset>()
    {
        public void work(Asset asset)
        {
            javascriptSupport.importJavascriptLibrary(asset);
        }
    };

    private final Worker<Asset> importStylesheet = new Worker<Asset>()
    {
        public void work(Asset asset)
        {
            javascriptSupport.importStylesheet(asset);
        };
    };

    public ImportWorker(JavascriptSupport javascriptSupport, SymbolSource symbolSource, AssetSource assetSource)
    {
        this.javascriptSupport = javascriptSupport;
        this.symbolSource = symbolSource;
        this.assetSource = assetSource;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        processClassAnnotationAtSetupRenderPhase(transformation, model);

        for (TransformMethod m : transformation.matchMethodsWithAnnotation(Import.class))
        {
            decorateMethod(transformation, model, m);
        }
    }

    private void processClassAnnotationAtSetupRenderPhase(ClassTransformation transformation,
            MutableComponentModel model)
    {
        Import annotation = transformation.getAnnotation(Import.class);

        if (annotation == null)
            return;

        TransformMethod setupRender = transformation.getOrCreateMethod(TransformConstants.SETUP_RENDER_SIGNATURE);

        decorateMethod(transformation, model, setupRender, annotation);

        model.addRenderPhase(SetupRender.class);
    }

    private void decorateMethod(ClassTransformation transformation, MutableComponentModel model, TransformMethod method)
    {
        Import annotation = method.getAnnotation(Import.class);

        decorateMethod(transformation, model, method, annotation);
    }

    private void decorateMethod(ClassTransformation transformation, MutableComponentModel model,
            TransformMethod method, Import annotation)
    {
        importStacks(method, annotation.stack());

        importLibraries(transformation, model, method, annotation.library());

        importStylesheets(transformation, model, method, annotation.stylesheet());
    }

    private void importStacks(TransformMethod method, String[] stacks)
    {
        if (stacks.length != 0)
            method.addAdvice(createImportStackAdvice(stacks));
    }

    private ComponentMethodAdvice createImportStackAdvice(final String[] stacks)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                for (String stack : stacks)
                {
                    javascriptSupport.importStack(stack);

                    invocation.proceed();
                }
            }
        };
    }

    private void importLibraries(ClassTransformation transformation, MutableComponentModel model,
            TransformMethod method, String[] paths)
    {
        decorateMethodWithOperation(transformation, model, method, paths, importLibrary);
    }

    private void importStylesheets(ClassTransformation transformation, MutableComponentModel model,
            TransformMethod method, String[] paths)
    {
        decorateMethodWithOperation(transformation, model, method, paths, importStylesheet);
    }

    private void decorateMethodWithOperation(ClassTransformation transformation, MutableComponentModel model,
            TransformMethod method, String[] paths, Worker<Asset> operation)
    {
        if (paths.length == 0)
            return;

        String[] expandedPaths = expandPaths(paths);

        FieldAccess access = createFieldForAssets(transformation);

        storeLocalizedAssetsAtPageLoad(transformation, model.getBaseResource(), expandedPaths, access);

        addMethodAssetOperationAdvice(method, access, operation);
    }

    private String[] expandPaths(String[] paths)
    {
        String[] result = new String[paths.length];

        for (int i = 0; i < paths.length; i++)
            result[i] = symbolSource.expandSymbols(paths[i]);

        return result;
    }

    private FieldAccess createFieldForAssets(ClassTransformation transformation)
    {
        TransformField field = transformation.createField(Modifier.PRIVATE, List.class.getName(), "includedAssets");

        return field.getAccess();
    }

    private void storeLocalizedAssetsAtPageLoad(ClassTransformation transformation, final Resource baseResource,
            final String[] expandedPaths, final FieldAccess access)
    {
        ComponentMethodAdvice advice = new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                ComponentResources resources = invocation.getComponentResources();

                List<Asset> assets = convertPathsToAssets(baseResource, resources.getLocale(), expandedPaths);

                access.write(invocation.getInstance(), assets);
            }
        };

        transformation.getOrCreateMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE).addAdvice(advice);
    }

    private List<Asset> convertPathsToAssets(final Resource baseResource, final Locale locale, String[] assetPaths)
    {

        return F.flow(assetPaths).map(new Mapper<String, Asset>()
        {
            public Asset map(String assetPath)
            {
                return assetSource.getAsset(baseResource, assetPath, locale);
            }
        }).toList();
    }

    private void addMethodAssetOperationAdvice(TransformMethod method, final FieldAccess access,
            final Worker<Asset> operation)
    {
        ComponentInstanceOperation advice = new ComponentInstanceOperation()
        {

            public void invoke(Component instance)
            {
                List<Asset> assets = (List<Asset>) access.read(instance);

                F.flow(assets).each(operation);
            }
        };

        method.addOperationBefore(advice);
    }
}
