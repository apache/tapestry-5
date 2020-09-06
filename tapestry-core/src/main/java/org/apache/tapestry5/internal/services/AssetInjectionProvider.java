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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.transform.InjectionProvider2;

/**
 * Performs injection of assets, based on the presence of the {@link Path} annotation. This is more
 * useful than the
 * general {@link AssetObjectProvider}, because relative assets are supported.
 */
public class AssetInjectionProvider implements InjectionProvider2
{
    private final AssetSource assetSource;

    public AssetInjectionProvider(AssetSource assetSource)
    {
        this.assetSource = assetSource;
    }

    public boolean provideInjection(PlasticField field, ObjectLocator locator, MutableComponentModel componentModel)
    {
        Path path = field.getAnnotation(Path.class);

        if (path == null)
        {
            return false;
        }

        final String assetPath = path.value();
        final String libraryName = componentModel.getLibraryName();

        ComputedValue<Asset> computedAsset = new ComputedValue<Asset>()
        {
            public Asset get(InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);

                // Note how this works: the resources represents the actual instantiated class, and the libraryName
                // comes from the componentModel, potentially, the componentModel of a base class (which may have
                // a different library name than the subclass).
                return assetSource.getComponentAsset(resources, assetPath, libraryName);
            }
        };

        field.injectComputed(computedAsset);

        return true;
    }

}
