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
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.urlrewriter.*;

public class AppModule
{
    private static final String SUCCESS_PAGE_NAME = URLRewriteSuccess.class.getSimpleName().toLowerCase();

    public static void contributeURLRewriter(OrderedConfiguration<URLRewriterRule> configuration) {
        
        URLRewriterRule rule1 = new URLRewriterRule()
        {

            public Request process(Request request, URLRewriteContext context)
            {
                final String path = request.getPath();
                if (path.equals("/struts")) 
                {
                    request = new SimpleRequestWrapper(request, "/jsf");
                }
                return request;
                
            }

            public RewriteRuleApplicability applicability() {
                return RewriteRuleApplicability.INBOUND;
            }

        };
        
        URLRewriterRule rule2 = new URLRewriterRule()
        {

            public Request process(Request request, URLRewriteContext context)
            {
                final String path = request.getPath();
                if (path.equals("/jsf")) 
                {
                    request = new SimpleRequestWrapper(request, "/tapestry");
                }
                return request;
                
            }

            public RewriteRuleApplicability applicability() {
                return RewriteRuleApplicability.INBOUND;
            }

        };


        URLRewriterRule rule3 = new URLRewriterRule()
        {

            public Request process(Request request,URLRewriteContext context)
            {
                String path = request.getPath();
                if (path.equals("/tapestry")) 
                {
                    path = "/" + SUCCESS_PAGE_NAME;
                    request = new SimpleRequestWrapper(request, path);
                }
                return request;
                
            }

            public RewriteRuleApplicability applicability()
            {
                return RewriteRuleApplicability.INBOUND;
            }

        };
        
        URLRewriterRule rule4 = new URLRewriterRule()
        {

            public Request process(Request request, URLRewriteContext context)
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

            public RewriteRuleApplicability applicability()
            {
                return RewriteRuleApplicability.INBOUND;
            }

        };
        
        URLRewriterRule rule5 = new URLRewriterRule() 
        {

            public Request process(Request request, URLRewriteContext context)
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

            public RewriteRuleApplicability applicability()
            {
                return RewriteRuleApplicability.OUTBOUND;
            }

        };
        
        URLRewriterRule rule6 = new URLRewriterRule()
        {

            public Request process(Request request, URLRewriteContext context)
            {
                String serverName = request.getServerName();
                String path = request.getPath().toLowerCase();
                if (serverName.equals("localhost") && path.equals("/dummy"))
                {
                    request = new SimpleRequestWrapper(request, "/notdummy");
                }
                return request;
                
            }

            public RewriteRuleApplicability applicability()
            {
                return RewriteRuleApplicability.OUTBOUND;
            }

        };

        URLRewriterRule rule7 = new Rule7();


        configuration.add("rule1", rule1);
        configuration.add("rule2", rule2, "after:rule1");
        configuration.add("rule3", rule3, "after:rule2");
        configuration.add("rule4", rule4);
        configuration.add("rule5", rule5);
        configuration.add("rule6", rule6);
        configuration.add("rule7", rule7);
        
    }

    //note that as this is a test, there are a lot of shortcuts employed in the url processing.
    //and the example is entirely contrived.
    //But it does illustrate the sorts of things that are possible.
    static class Rule7 implements URLRewriterRule
    {

        private Request decodePage(Request request)
        {
            //want to skip first slash plus the slash trailing rpage.
            int idx =request.getPath().indexOf('/',7);

            String pageName;
            String pathRemainder;
            if (idx == -1) {
                pageName = request.getPath().substring(7);
                pathRemainder = "";
            } else {
                pageName = request.getPath().substring(7,idx);
                pathRemainder = request.getPath().substring(idx);
            }

            String newPath = "/" + reverse(pageName)
                    + pathRemainder;
            return new SimpleRequestWrapper(request,newPath);
        }

        private Request decodeEventLink(Request request, String path,int idx)
        {
            String event = null;
            //do we have a slash after?
            int slashIdx = path.indexOf('/',idx);
            if (slashIdx == -1) {
                event = reverse(path.substring(idx+1));
                path = path.substring(0,idx) ;
            } else {
                event = reverse(path.substring(idx+1,slashIdx));
                path = path.substring(0,idx) + path.substring(slashIdx);
            }
            return decodeComponentLink(request,path,event);
        }

        private Request decodeComponentLink(Request request, String path, String event)
        {
            int idx = path.indexOf('.');
            String pageName;
            String componentName=null;//idea complains about componentName might not be initialized otherwise.
            if (idx == -1)
            {
                idx = path.indexOf('/');
                if (idx < 1)
                {
                    pageName = reverse(path);
                    path = "";
                } else {
                    pageName = reverse(path.substring(0,idx));
                    path = path.substring(idx);

                }
            }
            else
            {
                int slashIdx = path.indexOf('/',idx);
                pageName = reverse(path.substring(0,idx));
                if (slashIdx < 1) {
                    componentName = reverse(path.substring(idx+1));
                    path = "";
                } else {
                    componentName = reverse(path.substring(idx+1,slashIdx));
                    path = path.substring(slashIdx);
                }
            }
            path = "/" + pageName + (componentName==null?"":"."+componentName)
                    + (event==null?"":":" + event) + path;
            return new SimpleRequestWrapper(request,path);
        }

        public Request process(Request request, URLRewriteContext context)
        {
            if (context.isIncoming()) {
                if (request.getPath().startsWith("/rpage/"))
                {
                    return decodePage(request);
                }
                else if (request.getPath().startsWith("/cevent/"))
                {
                    String path = request.getPath().substring(8);
                    //check for event presence first.
                    int idx = path.indexOf(':');
                    if (idx != -1)
                    {
                        return decodeEventLink(request,path,idx);
                    }
                    return decodeComponentLink(request,path,null);

                }
            }
            else if (context.getPageParameters() != null)
            {
                //page link reversing is just to illustrate the fact that we can manipulate paths without caring about
                //the precise details of the path. Except, we don't want to mess with index, URLRewriteSuccess,
                //or dummy because that messes up the rest of the tests.
                String pageName = context.getPageParameters().getLogicalPageName().toLowerCase();
                if (pageName.equals("index") || pageName.equals("urlrewritesuccess") || pageName.equals("dummy")) {
                    return request;
                }
                String newPath = "/rpage" + request.getPath().replaceAll(pageName,reverse(pageName));
                return new SimpleRequestWrapper(request,newPath);
            }
            else
            {
                //mangle the event details.
                String pageName = context.getComponentEventParameters().getActivePageName().toLowerCase();
                if (pageName.equals("index") || pageName.equals("urlrewritesuccess") || pageName.equals("dummy")) {
                    return request;
                }
                String eventName = context.getComponentEventParameters().getEventType().toLowerCase();
                String componentId = context.getComponentEventParameters().getNestedComponentId().toLowerCase();

                String newPath = "/cevent" +
                        request.getPath().replaceAll(pageName,reverse(pageName))
                                .replaceAll(eventName,reverse(eventName))
                                .replaceAll(componentId,reverse(componentId));
                return new SimpleRequestWrapper(request, newPath);
            }
            return request;
        }

        private String reverse(String input) {

            if (input == null) return null;

            StringBuilder rev = new StringBuilder(input.length());

            for(int i=input.length();i>0;i--)
            {
                rev.append(input.charAt(i-1));
            }

            return rev.toString();
        }

        public RewriteRuleApplicability applicability()
        {
            return RewriteRuleApplicability.BOTH;
        }
    };
}

