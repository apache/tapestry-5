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
import org.apache.tapestry5.ioc.Location;

public class AssetBinding extends AbstractBinding
{
    private final String description;

    private final Asset asset;

    private final boolean forceVariant;

    AssetBinding(Location location, String description, Asset asset, boolean forceVariant)
    {
        super(location);

        this.description = description;
        this.asset = asset;
        this.forceVariant = forceVariant;
    }

    @Override
    public Class getBindingType()
    {
        return Asset.class;
    }

    public Object get()
    {
        return asset;
    }

    /**
     * Queries the underlying Asset to determine if {@linkplain org.apache.tapestry5.Asset#isInvariant() it is
     * invariant}.
     *
     * @see org.apache.tapestry5.internal.InternalSymbols#FORCE_ASSET_BINDINGS_VARIANT
     */
    @Override
    public boolean isInvariant()
    {
        return !forceVariant && asset.isInvariant();
    }

    @Override
    public String toString()
    {
        return String.format("AssetBinding[%s: %s]", description, asset);
    }
}
