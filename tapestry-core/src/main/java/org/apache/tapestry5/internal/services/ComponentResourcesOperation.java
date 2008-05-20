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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;

/**
 * A kind of callback that can easily be injected into a transformed class to perform complex work.
 * <p/>
 * These callbacks are often injected into a transformed component class via {@link
 * org.apache.tapestry5.services.ClassTransformation#addInjectedField(Class, String, Object)}. Bear in mind that such
 * callbacks must be <em>threadsafe</em>, since every instance of such a class will share a single instance of the
 * operation.
 */
public interface ComponentResourcesOperation
{
    /**
     * Perform some operation that requires the components' resources.
     */
    void perform(ComponentResources resources);
}
