// Copyright 2021 The Apache Software Foundation
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
package org.apache.tapestry5.openapiviewer.pages;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

/**
 * Shows an OpenAPI definition viewer based on Swagger UI.
 */
public class Index {

    @Inject
    private BaseURLSource baseURLSource;
    
    @Inject
    @Symbol(SymbolConstants.OPENAPI_DESCRIPTION_PATH) 
    private String descriptionPath;
    
    @Inject
    private Request request;
    
    @Cached
    public String getEscapedDefinitionUrl() throws UnsupportedEncodingException
    {
        return URLEncoder.encode(baseURLSource.getBaseURL(request.isSecure()) + descriptionPath, "UTF-8");
    }
    
}
