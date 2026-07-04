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
    
    private final String urlPrefix;
    
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
        this.compressedRequestPrefix = getRequestPrefixUncached(true, false);
        this.uncompressedRequestPrefix = getRequestPrefixUncached(false, false);
        this.urlPrefix = getRequestPrefixUncached(true, true);
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
    public String getDispatcherUrlPrefix(boolean compress) 
    {
        return compress ? compressedRequestPrefix : uncompressedRequestPrefix;
    }
    
    public String getRequestPrefixUncached(boolean compress, boolean clientPath)
    {
        final String suffix = compress ? ES_SUBPATH + ".gz" : ES_SUBPATH;
        return (clientPath ? pathConstructor.constructClientPath(assetPrefix, suffix) : 
                pathConstructor.constructDispatchPath(assetPrefix, suffix)) + "/";
    }

    @Override
    public String getUrl(String moduleName) {
        return baseURLSource.getBaseURL(request.isSecure()) + 
                urlPrefix + moduleName + ".js";
    }
    
}
