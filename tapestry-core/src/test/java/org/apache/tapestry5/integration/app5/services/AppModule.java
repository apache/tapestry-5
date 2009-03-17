// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app5.services;

import org.apache.tapestry5.integration.app5.pages.URLRewriteSuccess;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.IntegrationTests;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;

public class AppModule
{
    private static final String SUCCESS_PAGE_NAME = URLRewriteSuccess.class.getSimpleName().toLowerCase();

    public static void contributeURLRewriterService(OrderedConfiguration<URLRewriterRule> configuration) {
        
        URLRewriterRule rule1 = new URLRewriterRule() {

            public Request process(Request request)
            {
                final String path = request.getPath();
                if (path.equals("/struts")) 
                {
                    request = new SimpleRequestWrapper(request, "/jsf");
                }
                return request;
                
            }
            
        };
        
        URLRewriterRule rule2 = new URLRewriterRule() {

            public Request process(Request request)
            {
                final String path = request.getPath();
                if (path.equals("/jsf")) 
                {
                    request = new SimpleRequestWrapper(request, "/tapestry");
                }
                return request;
                
            }
            
        };
        
        URLRewriterRule rule3 = new URLRewriterRule() {

            public Request process(Request request)
            {
                String path = request.getPath();
                if (path.equals("/tapestry")) 
                {
                    path = "/" + SUCCESS_PAGE_NAME;
                    request = new SimpleRequestWrapper(request, path);
                }
                return request;
                
            }
            
        };
        
        URLRewriterRule rule4 = new URLRewriterRule() {

            public Request process(Request request)
            {
                String serverName = request.getServerName();
                String path = request.getPath();
                if (serverName.equals(IntegrationTests.SUBDOMAIN) && path.equals("/")) 
                {
                    path = String.format("/%s/%s", SUCCESS_PAGE_NAME, IntegrationTests.LOGIN);
                    request = new SimpleRequestWrapper(request, path);
                }
                return request;
                
            }
            
        };
        
        URLRewriterRule rule5 = new URLRewriterRule() {

            public Request process(Request request)
            {
                String serverName = request.getServerName();
                String path = request.getPath();
                final String pathToRewrite = "/" + SUCCESS_PAGE_NAME + "/login";
                if (serverName.equals("localhost") && path.equalsIgnoreCase(pathToRewrite)) 
                {
                    request = new SimpleRequestWrapper(request, IntegrationTests.SUBDOMAIN, "/");
                }
                return request;
                
            }
            
        };
        
        URLRewriterRule rule6 = new URLRewriterRule() {

            public Request process(Request request)
            {
                String serverName = request.getServerName();
                String path = request.getPath().toLowerCase();
                if (serverName.equals("localhost") && path.equals("/dummy"))
                {
                    request = new SimpleRequestWrapper(request, "/notdummy");
                }
                return request;
                
            }
            
        };
        
        configuration.add("rule1", rule1);
        configuration.add("rule2", rule2, "after:rule1");
        configuration.add("rule3", rule3, "after:rule2");
        configuration.add("rule4", rule4);
        configuration.add("rule5", rule5);
        configuration.add("rule6", rule6);
        
    }
}
