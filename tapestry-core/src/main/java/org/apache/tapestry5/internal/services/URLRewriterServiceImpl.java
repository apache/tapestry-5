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

package org.apache.tapestry5.internal.services;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;


/**
 * Default {@linkplain URLRewriterService} implementation.
 *
 * @since 5.1.0.2
 */
@UsesOrderedConfiguration(URLRewriterRule.class)
public class URLRewriterServiceImpl implements URLRewriterService
{

    final private List<URLRewriterRule> rules;

    /**
     * Single constructor of this class.
     * 
     * @param rules
     *            a <code>List</code> of <code>URLRewriterRule</code>. It cannot be null.
     */
    public URLRewriterServiceImpl(List<URLRewriterRule> rules)
    {
        Defense.notNull(rules, "rules");
        this.rules = Collections.unmodifiableList(rules);
    }

    public Request process(Request request)
    {
        for (URLRewriterRule rule : rules)
        {

            request = rule.process(request);
            if (request == null) 
            { 
                throw new NullPointerException(
                    "URLRewriterRule.process() returned null."); 
            }

        }
        
        return request;
        
    }
    
    public List<URLRewriterRule> getRules() {
        return rules;
    }
    
}
