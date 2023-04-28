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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.javascript.Initialization;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the {@link Import} annotation, both at the class and at the method level.
 *
 * @since 5.2.0
 */
public class ImportWorker implements ComponentClassTransformWorker2
{
    private final JavaScriptSupport javascriptSupport;

    private final SymbolSource symbolSource;

    private final AssetSource assetSource;

    private final Worker<Asset> importLibrary = new Worker<Asset>()
    {
        public void work(Asset asset)
        {
            javascriptSupport.importJavaScriptLibrary(asset);
        }
    };

    private final Worker<Asset> importStylesheet = new Worker<Asset>()
    {
        public void work(Asset asset)
        {
            javascriptSupport.importStylesheet(asset);
        }
    };

    private final Mapper<String, String> expandSymbols = new Mapper<String, String>()
    {
        public String map(String element)
        {
            return symbolSource.expandSymbols(element);
        }
    };

    public ImportWorker(JavaScriptSupport javascriptSupport, SymbolSource symbolSource, AssetSource assetSource)
    {
        this.javascriptSupport = javascriptSupport;
        this.symbolSource = symbolSource;
        this.assetSource = assetSource;
    }

    public void transform(PlasticClass componentClass, TransformationSupport support, MutableComponentModel model)
    {
        processClassAnnotationAtSetupRenderPhase(componentClass, model);

        for (PlasticMethod m : componentClass.getMethodsWithAnnotation(Import.class))
        {
            decorateMethod(componentClass, model, m);
        }
    }

    private void processClassAnnotationAtSetupRenderPhase(PlasticClass componentClass, MutableComponentModel model)
    {
        Import annotation = componentClass.getAnnotation(Import.class);

        if (annotation != null)
        {
            PlasticMethod setupRender = componentClass.introduceMethod(TransformConstants.SETUP_RENDER_DESCRIPTION);

            decorateMethod(componentClass, model, setupRender, annotation);

            model.addRenderPhase(SetupRender.class);
        }
    }

    private void decorateMethod(PlasticClass componentClass, MutableComponentModel model, PlasticMethod method)
    {
        Import annotation = method.getAnnotation(Import.class);

        decorateMethod(componentClass, model, method, annotation);
    }

    private void decorateMethod(PlasticClass componentClass, MutableComponentModel model, PlasticMethod method,
                                Import annotation)
    {
        importStacks(method, annotation.stack());

        String libraryName = model.getLibraryName();

        importLibraries(componentClass, model, method, annotation.library());

        importStylesheets(componentClass, model, method, annotation.stylesheet());

        importModules(method, annotation.module());
    }

    private void importStacks(PlasticMethod method, String[] stacks)
    {
        if (stacks.length != 0)
        {
            method.addAdvice(createImportStackAdvice(stacks));
        }
    }

    private MethodAdvice createImportStackAdvice(final String[] stacks)
    {
        return new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                for (String stack : stacks)
                {
                    javascriptSupport.importStack(stack);
                }

                invocation.proceed();
            }
        };
    }

    private void importModules(PlasticMethod method, String[] moduleNames)
    {
        if (moduleNames.length != 0)
        {
            method.addAdvice(createImportModulesAdvice(moduleNames));
        }
    }

    class ModuleImport
    {
        final String moduleName, functionName;

        ModuleImport(String moduleName, String functionName)
        {
            this.moduleName = moduleName;
            this.functionName = functionName;
        }

        void apply(JavaScriptSupport javaScriptSupport)
        {
            Initialization initialization = javaScriptSupport.require(moduleName);

            if (functionName != null)
            {
                initialization.invoke(functionName);
            }
        }
    }

    private MethodAdvice createImportModulesAdvice(final String[] moduleNames)
    {
        final List<ModuleImport> moduleImports = new ArrayList<ModuleImport>(moduleNames.length);

        for (String name : moduleNames)
        {
            int colonx = name.indexOf(':');

            String moduleName = colonx < 0 ? name : name.substring(0, colonx);
            String functionName = colonx < 0 ? null : name.substring(colonx + 1);

            moduleImports.add(new ModuleImport(moduleName, functionName));
        }

        return new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                for (ModuleImport moduleImport : moduleImports)
                {
                    moduleImport.apply(javascriptSupport);
                }

                invocation.proceed();
            }
        };
    }

    private void importLibraries(PlasticClass plasticClass, MutableComponentModel model, PlasticMethod method,
                                 String[] paths)
    {
        decorateMethodWithOperation(plasticClass, model, method, paths, importLibrary);
    }

    private void importStylesheets(PlasticClass plasticClass, MutableComponentModel model, PlasticMethod method,
                                   String[] paths)
    {
        decorateMethodWithOperation(plasticClass, model, method, paths, importStylesheet);
    }

    private void decorateMethodWithOperation(PlasticClass componentClass, MutableComponentModel model,
                                             PlasticMethod method, String[] paths, Worker<Asset> operation)
    {
        if (paths.length == 0)
        {
            return;
        }

        String[] expandedPaths = expandPaths(paths);

        PlasticField assetListField = componentClass.introduceField(Asset[].class,
                "importedAssets_" + method.getDescription().methodName);

        initializeAssetsFromPaths(expandedPaths, assetListField, model.getLibraryName());

        addMethodAssetOperationAdvice(method, assetListField.getHandle(), operation);
    }

    private String[] expandPaths(String[] paths)
    {
        return F.flow(paths).map(expandSymbols).toArray(String.class);
    }

    private void initializeAssetsFromPaths(final String[] expandedPaths, PlasticField assetsField, final String libraryName)
    {
        assetsField.injectComputed(new ComputedValue<Asset[]>()
        {
            public Asset[] get(InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);

                return convertPathsToAssetArray(resources, expandedPaths, libraryName);
            }
        });
    }

    private Asset[] convertPathsToAssetArray(final ComponentResources resources, String[] assetPaths, final String libraryName)
    {
        return F.flow(assetPaths).map(new Mapper<String, Asset>()
        {
            public Asset map(String assetPath)
            {
                return assetSource.getComponentAsset(resources, assetPath, libraryName);
            }
        }).toArray(Asset.class);
    }

    private void addMethodAssetOperationAdvice(PlasticMethod method, final FieldHandle access,
                                               final Worker<Asset> operation)
    {
        method.addAdvice(new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                invocation.proceed();

                Asset[] assets = (Asset[]) access.get(invocation.getInstance());

                F.flow(assets).each(operation);
            }
        });
    }
}
