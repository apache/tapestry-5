// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
package org.apache.tapestry5.urlrewriter;

import java.io.IOException;
import java.util.List;

import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;

/**
 * <code>RequestFilter</code> that applies the URL rewriting rules to requests.
 * 
 * @see org.apache.tapestry5.services.RequestFilter
 */
public class URLRewriterRequestFilter implements RequestFilter
{

    final private List<URLRewriterRule> rules;

    /**
     * Single constructor of this class.
     * 
     * @param rules
     *            a <code>List</code> of <code>URLRewriterRule</code>. It cannot be null.
     */
    public URLRewriterRequestFilter(List<URLRewriterRule> rules)
    {
        Defense.notNull(rules, "rules");
        this.rules = rules;
    }

    public boolean service(Request request, Response response, RequestHandler handler)
            throws IOException
    {

        for (URLRewriterRule rule : rules)
        {

            request = rule.process(request);
            if (request == null) 
            { 
                throw new RuntimeException(
                    "URLRewriterRule.process() returned null"); 
            }

        }

        return handler.service(request, response);

    }

}
