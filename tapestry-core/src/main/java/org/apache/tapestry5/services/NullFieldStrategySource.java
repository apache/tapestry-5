// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.NullFieldStrategy;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * A source for {@link org.apache.tapestry5.NullFieldStrategy} instances based on a logical name.
 */
@UsesMappedConfiguration(NullFieldStrategy.class)
public interface NullFieldStrategySource
{
    /**
     * Returns the instance based on the name.  Instances are expected to be stateless and therefore, shareable and
     * thread safe.
     *
     * @param name name of the strategy (case is ignored)
     * @return the strategy
     * @throws IllegalArgumentException if the name does not match a configured instance
     */
    NullFieldStrategy get(String name);
}
