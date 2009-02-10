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

package org.apache.tapestry5;

/**
 * Extension to {@link org.apache.tapestry5.Asset} that adds a method to determine if the asset is invariant or not.
 * {@link org.apache.tapestry5.services.AssetFactory} instances should ideally return Asset2 objects, not Asset. This is
 * only of primary interest to the {@link org.apache.tapestry5.internal.bindings.AssetBindingFactory}, as it determines
 * the invariance of the binding from the asset (and assumes variant unless the asset object implements this
 * interface).
 *
 * @since 5.1.0.0
 */
public interface Asset2 extends Asset
{
    /**
     * Returns true if the Asset is invariant (meaning that it returns the same value from {@link Asset#toClientURL()}
     * at all times}. Assets that are used as binding values will be cached more aggresively by Tapestry as they are
     * invariant.
     *
     * @return true if invariant
     * @see org.apache.tapestry5.services.AssetPathConverter#isInvariant()
     * @see Binding#isInvariant()
     */
    boolean isInvariant();
}
