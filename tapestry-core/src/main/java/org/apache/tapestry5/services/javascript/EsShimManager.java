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

package org.apache.tapestry5.services.javascript;

import java.util.Map;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Service managing the ES shims.
 *
 * @since 5.10.0
 */
@UsesMappedConfiguration(Resource.class)
public interface EsShimManager
{
    
    /**
     * Returns the shims as a (module name, module resource) map.
     * @return a {@code Map<String, Resource>}
     */
    Map<String, Resource> getShims();

    /**
     * Returns the request prefix to be used for ES shim URLs.
     * @param compress a {@code boolean} to inform whether it's the compressed asset URL or not.
     * @return the request prefix.
     */
    String getRequestPrefix(boolean compress);
    
    /**
     * Returns the full URL of a module.
     * @param moduleName a module name.
     * @return its URL.
     */
    String getUrl(String moduleName);
    
}
