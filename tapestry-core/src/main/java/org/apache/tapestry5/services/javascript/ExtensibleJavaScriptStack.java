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

import java.util.List;

/**
 * An extensible implementation of {@link JavaScriptStack} that can be used as the implementation of a service.
 * The contributions to the service are used to supply the libraries, stylesheets, and initialization for a
 * JavaScriptStack, allowing the stack to be more dynamically configured. In practice, one will use
 * {@link ServiceBinder#bind(Class, Class)} and {@link ServiceBindingOptions#withMarker(Class...)} to construct the
 * service, then use the marker annotation to inject the service when contributing the service into to the
 * {@link JavaScriptStackSource}.
 *
 * A limitation of this implementation is that the contributed assets are not localized at all.
 *
 * @see StackExtension
 * @since 5.3
 */
@UsesOrderedConfiguration(StackExtension.class)
public class ExtensibleJavaScriptStack implements JavaScriptStack
{
    private final AssetSource assetSource;

    private final List<Asset> libraries;

    private final List<StylesheetLink> stylesheets;

    private final List<String> stacks;

    private final List<String> modules;

    private final String initialization;

    private final JavaScriptAggregationStrategy strategy;

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
        }

        ;
    };

    private final Mapper<String, Asset> stringToAsset = new Mapper<String, Asset>()
    {
        public Asset map(String value)
        {
            return assetSource.getExpandedAsset(value);
        }

        ;
    };

    private final Mapper<Asset, StylesheetLink> assetToStylesheetLink = new Mapper<Asset, StylesheetLink>()
    {
        public StylesheetLink map(Asset asset)
        {
            return new StylesheetLink(asset);
        }

        ;
    };

    private final Mapper<String, JavaScriptAggregationStrategy> stringToStrategy = new Mapper<String, JavaScriptAggregationStrategy>()
    {
        @Override
        public JavaScriptAggregationStrategy map(String name)
        {
            return JavaScriptAggregationStrategy.valueOf(name);
        }
    };

    public ExtensibleJavaScriptStack(AssetSource assetSource, List<StackExtension> configuration)
    {
        this.assetSource = assetSource;

        Flow<StackExtension> extensions = F.flow(configuration);

        libraries = extensions.filter(by(StackExtensionType.LIBRARY)).map(extractValue).map(stringToAsset).toList();

        stacks = extensions.filter(by(StackExtensionType.STACK)).map(extractValue).toList();

        modules = extensions.filter(by(StackExtensionType.MODULE)).map(extractValue).toList();

        stylesheets = extensions.filter(by(StackExtensionType.STYLESHEET)).map(extractValue).map(stringToAsset)
                .map(assetToStylesheetLink).toList();

        List<String> initializations = extensions.filter(by(StackExtensionType.INITIALIZATION)).map(extractValue)
                .toList();

        initialization = initializations.isEmpty() ? null : InternalUtils.join(initializations, "\n");

        strategy = toStrategy(extensions);
    }

    private JavaScriptAggregationStrategy toStrategy(Flow<StackExtension> extensions)
    {
        List<JavaScriptAggregationStrategy> values = extensions.filter(by(StackExtensionType.AGGREGATION_STRATEGY)).map(extractValue).map(stringToStrategy).toList();

        switch (values.size())
        {
            case 0:
                return JavaScriptAggregationStrategy.COMBINE_AND_MINIMIZE;

            case 1:

                return values.get(0);

            default:
                throw new IllegalStateException(String.format("Could not handle %d contribution(s) of JavaScriptAggregation Strategy. There should be at most one.",
                        values.size()));
        }
    }

    public List<String> getStacks()
    {
        return stacks;
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

    public List<String> getModules()
    {
        return modules;
    }

    @Override
    public JavaScriptAggregationStrategy getJavaScriptAggregationStrategy()
    {
        return strategy;
    }
}
