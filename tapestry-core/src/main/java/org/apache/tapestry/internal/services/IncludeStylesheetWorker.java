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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Asset;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.IncludeStylesheet;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

import java.util.List;
import java.util.Locale;

public class IncludeStylesheetWorker implements ComponentClassTransformWorker
{
    private final AssetSource _assetSource;

    private final PageRenderSupport _pageRenderSupport;

    private final SymbolSource _symbolSource;

    public IncludeStylesheetWorker(AssetSource assetSource, PageRenderSupport pageRenderSupport,
                                   SymbolSource symbolSource)
    {
        _assetSource = assetSource;
        _pageRenderSupport = pageRenderSupport;
        _symbolSource = symbolSource;
    }

    public void transform(ClassTransformation transformation, final MutableComponentModel model)
    {
        IncludeStylesheet annotation = transformation.getAnnotation(IncludeStylesheet.class);

        if (annotation == null) return;

        final List<String> paths = CollectionFactory.newList();

        for (String value : annotation.value())
        {
            String expanded = _symbolSource.expandSymbols(value);

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
                    Asset asset = _assetSource.findAsset(model.getBaseResource(), assetPath, locale);

                    _pageRenderSupport.addStylesheetLink(asset, null);
                }
            }
        };

        String opFieldName = transformation.addInjectedField(ComponentResourcesOperation.class, "includeCSSOperation",
                                                             op);

        String resourcesName = transformation.getResourcesFieldName();

        String body = String.format("%s.perform(%s);", opFieldName, resourcesName);

        // This is what I like about this approach; the injected body is tiny.  The downside is that
        // the object that gets injected is hard to test, hard enough that we'll just concentrate on
        // the integration test, thank you.

        transformation.extendMethod(TransformConstants.SETUP_RENDER_SIGNATURE, body);
    }
}
