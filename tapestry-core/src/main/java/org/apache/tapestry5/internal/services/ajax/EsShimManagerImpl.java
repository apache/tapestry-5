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

package org.apache.tapestry5.internal.services.ajax;

import java.util.Map;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PathConstructor;
import org.apache.tapestry5.services.javascript.EsShimManager;

public class EsShimManagerImpl implements EsShimManager
{
    
    private static final String ES_SUBPATH = "es-shims";
    
    private final Map<String, Resource> shims;
    
    private final PathConstructor pathConstructor;
    
    private final String assetPrefix;
    
    private final BaseURLSource baseURLSource;
    
    private final Request request;
    
    private final String compressedRequestPrefix;
    
    private final String uncompressedRequestPrefix;
    
    public EsShimManagerImpl(Map<String, Resource> shims,
            PathConstructor pathConstructor,
            BaseURLSource baseURLSource,
            Request request,
            @Symbol(SymbolConstants.ASSET_PATH_PREFIX)
            String assetPrefix) 
    {
        super();
        this.shims = shims;
        this.assetPrefix = assetPrefix;
        this.pathConstructor = pathConstructor;
        this.baseURLSource = baseURLSource;
        this.request = request;
        this.compressedRequestPrefix = getRequestPrefixUncached(true);
        this.uncompressedRequestPrefix = getRequestPrefixUncached(false);
    }

    /**
     * Returns the shims as a (module name, module resource) map.
     * @return a {@code Map<String, Resource>}
     */
    @Override
    public Map<String, Resource> getShims()
    {
        return shims;
    }
    
    @Override
    public String getRequestPrefix(boolean compress) 
    {
        return compress ? compressedRequestPrefix : uncompressedRequestPrefix;
    }
    
    public String getRequestPrefixUncached(boolean compress)
    {
        return pathConstructor.constructDispatchPath(assetPrefix, (compress ? ES_SUBPATH + ".gz" : ES_SUBPATH)) + "/";
    }

    @Override
    public String getUrl(String moduleName) {
        return baseURLSource.getBaseURL(request.isSecure()) + 
                getRequestPrefix(true) + moduleName + ".js";
    }
    
}
