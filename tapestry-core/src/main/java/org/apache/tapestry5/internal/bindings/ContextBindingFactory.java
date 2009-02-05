// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.BindingFactory;

/**
 * Specialization of {@link org.apache.tapestry5.internal.bindings.AssetBindingFactory} that is explicitly limited to
 * context assets.
 *
 * @since 5.1.0.0
 */
public class ContextBindingFactory implements BindingFactory
{
    private final AssetSource source;

    public ContextBindingFactory(AssetSource source)
    {
        this.source = source;
    }

    public Binding newBinding(String description, ComponentResources container, ComponentResources component,
                              String expression, Location location)
    {
        Asset asset = source.getContextAsset(expression, container.getLocale());

        return new AssetBinding(location, description, asset);
    }
}
