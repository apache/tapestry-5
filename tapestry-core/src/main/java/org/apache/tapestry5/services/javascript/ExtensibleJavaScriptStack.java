// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.javascript;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.ServiceBindingOptions;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.AssetSource;

/**
 * An extensible implementation of {@link JavaScriptStack} that can be used as the implementation of a service.
 * The contributions to the service are used to supply the libraries, stylesheets, and initialization for a
 * JavaScriptStack, allowing the stack to be more dynamically configured. In practice, one will use
 * {@link ServiceBinder#bind(Class, Class)} and {@link ServiceBindingOptions#withMarker(Class...)} to construct the
 * service, then use the marker annotation to inject the service when contributing the service into to the
 * {@link JavaScriptStackSource}.
 * <p>
 * A limitation of this implementation is that the contributed assets are not localized at all.
 * 
 * @since 5.3
 * @see StackExtension
 */
@UsesOrderedConfiguration(StackExtension.class)
public class ExtensibleJavaScriptStack implements JavaScriptStack
{
    private final AssetSource assetSource;

    private final List<Asset> libraries;

    private final List<StylesheetLink> stylesheets;

    private final String initialization;

    private final Predicate<StackExtension> by(final StackExtensionType type)
    {
        return new Predicate<StackExtension>()
        {
            public boolean accept(StackExtension element)
            {
                return element.type == type;
            }
        };
    }

    private final Mapper<StackExtension, String> extractValue = new Mapper<StackExtension, String>()
    {
        public String map(StackExtension element)
        {
            return element.value;
        };
    };

    private final Mapper<String, Asset> stringToAsset = new Mapper<String, Asset>()
    {
        public Asset map(String value)
        {
            return assetSource.getExpandedAsset(value);
        };
    };

    private final Mapper<Asset, StylesheetLink> assetToStylesheetLink = new Mapper<Asset, StylesheetLink>()
    {
        public StylesheetLink map(Asset asset)
        {
            return new StylesheetLink(asset);
        };
    };

    public ExtensibleJavaScriptStack(AssetSource assetSource, List<StackExtension> configuration)
    {
        this.assetSource = assetSource;

        Flow<StackExtension> extensions = F.flow(configuration);

        libraries = extensions.filter(by(StackExtensionType.LIBRARY)).map(extractValue).map(stringToAsset).toList();

        stylesheets = extensions.filter(by(StackExtensionType.STYLESHEET)).map(extractValue).map(stringToAsset)
                .map(assetToStylesheetLink).toList();

        List<String> initializations = extensions.filter(by(StackExtensionType.INITIALIZATION)).map(extractValue)
                .toList();

        initialization = initializations.isEmpty() ? null : InternalUtils.join(initializations, "\n");
    }

    public List<String> getStacks()
    {
        return Collections.emptyList();
    }

    public List<Asset> getJavaScriptLibraries()
    {
        return libraries;
    }

    public List<StylesheetLink> getStylesheets()
    {
        return stylesheets;
    }

    public String getInitialization()
    {
        return initialization;
    }

}
