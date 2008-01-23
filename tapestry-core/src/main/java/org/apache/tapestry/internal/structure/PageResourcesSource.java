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

package org.apache.tapestry.internal.structure;

import java.util.Locale;

/**
 * Provides access to the {@link PageResources} facade.
 */
public interface PageResourcesSource
{
    /**
     * Gets (or creates) an instance of {@link PageResources} for the indicated locale.
     *
     * @param locale to create the resources for
     * @return the resources
     */
    PageResources get(Locale locale);
}
