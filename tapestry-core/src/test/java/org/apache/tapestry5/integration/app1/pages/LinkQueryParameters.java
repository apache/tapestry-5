// Copyright 2011 The Apache Software Foundation
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
package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page for testing the query parameter component parameter on all of the framework-supplied link components
 * (page, action, event)
 */
public class LinkQueryParameters
{

    @Property
    private String paramName;

    @Inject
    private Request request;

    @Inject
    private PageRenderLinkSource linkSource;

    public Map<String,?> getEmptyParameters()
    {
        return Collections.emptyMap();
    }

    public Map<String, ?> getNonEmptyParameters()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("param1","value1");
        map.put("param2", 10);
        return map;
    }

    public Link onAction()
    {
        return buildLink();
    }

    public Link onParameterCheck()
    {
        return buildLink();
    }

    //small hack for test simplicity: we'll generate a new page link, add any parameters we find in the current
    //request, and redirect to that instead of the default.
    private Link buildLink()
    {
        Link l = linkSource.createPageRenderLink(LinkQueryParameters.class);
        for(String param : request.getParameterNames())
        {
            l.addParameter(param,request.getParameter(param));
        }

        return l;
    }

    public boolean isHasParameters()
    {
        return !request.getParameterNames().isEmpty();
    }

    public List<String> getParameters()
    {
        return request.getParameterNames();
    }

    public String getParamVal()
    {
        return request.getParameter(paramName);
    }

}
