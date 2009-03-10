// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app3.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.integration.app3.pages.URLRewriteSuccess;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.SimpleRequestWrapper;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;

public class AppModule
{
    public static void contributeBeanBlockOverrideSource(Configuration<BeanBlockContribution> configuration)
    {
        configuration.add(new BeanBlockContribution("boolean", "PropertyDisplayBlockOverrides", "boolean", false));
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, "false");
    }
    
    public static void contributeURLRewriterRequestFilter(OrderedConfiguration<URLRewriterRule> configuration) {
        
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
                    path = "/" + URLRewriteSuccess.class.getSimpleName();
                    request = new SimpleRequestWrapper(request, path);
                }
                return request;
                
            }
            
        };
        
        configuration.add("rule1", rule1);
        configuration.add("rule2", rule2, "after:rule1");
        configuration.add("rule3", rule3, "after:rule2");
        
    }
    
}
