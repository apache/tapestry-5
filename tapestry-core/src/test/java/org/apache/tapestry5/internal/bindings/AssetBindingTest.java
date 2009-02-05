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
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class AssetBindingTest extends TapestryTestCase
{
    @Test
    public void asset_is_invariant()
    {
        Asset asset = mockAsset();

        train_isInvariant(asset, true);

        replay();

        Binding b = new AssetBinding(null, null, asset, false);

        assertEquals(b.getBindingType(), Asset.class);

        assertTrue(b.isInvariant());

        verify();
    }

    @Test
    public void asset_is_variant()
    {
        Asset asset = mockAsset();

        train_isInvariant(asset, false);

        replay();

        Binding b = new AssetBinding(null, null, asset, false);

        assertFalse(b.isInvariant());

        verify();
    }

    @Test
    public void force_variant()
    {
        Asset asset = mockAsset();

        replay();

        Binding b = new AssetBinding(null, null, asset, true);

        assertFalse(b.isInvariant());

        verify();

    }

    protected final void train_isInvariant(Asset asset, boolean invariant)
    {
        expect(asset.isInvariant()).andReturn(invariant).atLeastOnce();
    }
}
