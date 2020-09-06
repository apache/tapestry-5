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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.commons.ObjectProvider;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used to override built in services. Simply contribute a mapping from a type to an instance of that type. Anywhere
 * that exact type is injected, without specifying markers or other annotations, the contributed instance will be
 * injected, even if there is already a service that implements the interface.
 *
 * In fact, this is <em>not</em> limited to overriding services; any object that can be injected based solely on type
 * can be contributed.
 * 
 * @since 5.1.0.0
 */
@UsesMappedConfiguration(key = Class.class, value = Object.class)
public interface ServiceOverride
{
    /**
     * Returns a provider based on the configuration; this is wired into the
     * {@link org.apache.tapestry5.ioc.services.MasterObjectProvider}'s configuration.
     */
    ObjectProvider getServiceOverrideProvider();
}
