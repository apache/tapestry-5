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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.ioc.Resource;

/**
 * Used by some {@link ResourceTransformer} implementations to track additional dependencies that can arise
 * when the underlying resource being transformed can be dependent on other resources (for instance, if it has the
 * notion of "including" or "importing" content).
 *
 * @since 5.3
 */
public interface ResourceDependencies
{
    /**
     * Marks the dependency as an additional resource. A change to the dependency is considered the same as a change to
     * the resource being transformed.
     *
     * @param dependency
     */
    void addDependency(Resource dependency);
}
