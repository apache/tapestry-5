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

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.PageRenderRequestParameters;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.URLRewriter;
import org.apache.tapestry5.urlrewriter.URLRewriteContext;
import org.apache.tapestry5.urlrewriter.URLRewriterRule;

import java.util.ArrayList;
import java.util.List;


/**
 * Default {@linkplain org.apache.tapestry5.services.URLRewriter} implementation.
 *
 * @since 5.1.0.2
 */
@UsesOrderedConfiguration(URLRewriterRule.class)
public class URLRewriterImpl implements URLRewriter
{

    final private List<URLRewriterRule> incomingRules;
    final private List<URLRewriterRule> outgoingRules;

    /**
     * Single constructor of this class.
     * 
     * @param rules
     *            a <code>List</code> of <code>URLRewriterRule</code>. It cannot be null.
     */
    public URLRewriterImpl(List<URLRewriterRule> rules)
    {
        Defense.notNull(rules, "rules");
        this.incomingRules = new ArrayList<URLRewriterRule>();
        this.outgoingRules = new ArrayList<URLRewriterRule>();
        for(URLRewriterRule rule : rules)
        {
            switch(rule.applicability())
            {
                case INBOUND:
                    incomingRules.add(rule);
                    break;
                case OUTBOUND:
                    outgoingRules.add(rule);
                    break;
                default:
                    incomingRules.add(rule);
                    outgoingRules.add(rule);
            }
        }
    }

    public Request processRequest(Request request)
    {
        request = process(request,incomingRules,new URLRewriteContext(){
            public boolean isIncoming()
            {
                return true;
            }

            public PageRenderRequestParameters getPageParameters()
            {
                return null;
            }

            public ComponentEventRequestParameters getComponentEventParameters()
            {
                return null;
            }
        });
        if (request == null)
        {
            throw new NullPointerException(
                ServicesMessages.requestRewriteReturnedNull());
        }
        return request;
    }

    private Request process(Request request, List<URLRewriterRule> rules, URLRewriteContext context)
    {

        for (URLRewriterRule rule : rules)
        {

            request = rule.process(request,context);
            if (request == null)
            {
                return null;
            }

        }

        return request;

    }

    public Request processLink(Request request,URLRewriteContext context) {
        request = process(request,outgoingRules,context);
        if (request == null)
        {
            throw new NullPointerException(
                    ServicesMessages.linkRewriteReturnedNull());
        }
        return request;
    }

    public boolean hasRequestRules() {
        return !incomingRules.isEmpty();
    }

    public boolean hasLinkRules() {
        return !outgoingRules.isEmpty();
    }


}
