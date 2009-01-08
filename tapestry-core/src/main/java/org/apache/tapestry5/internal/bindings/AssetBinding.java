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

    private final boolean invariant;

    AssetBinding(Location location, String description, Asset asset, boolean invariant)
    {
        super(location);

        this.description = description;
        this.asset = asset;
        this.invariant = invariant;
    }

    public Object get()
    {
        return asset;
    }

    /**
     * Asset bindings are invariant only if full URIs are being used.  This is complicated ... basically, if the Asset
     * is invariant, then any value coerced from the Asset is also invariant (such as a String version of an Asset's
     * path).  Thus, the invariant String gets cached inside component parameter fields.  However, when the path is
     * dynamic (i.e., because of {@link org.apache.tapestry5.internal.services.RequestPathOptimizer}), we need to ensure
     * that the Assets aren't cached.
     *
     * @return true if full URIs are enabled, false otherwise
     */
    @Override
    public boolean isInvariant()
    {
        return invariant;
    }

    @Override
    public String toString()
    {
        return String.format("AssetBinding[%s: %s]", description, asset);
    }
}
