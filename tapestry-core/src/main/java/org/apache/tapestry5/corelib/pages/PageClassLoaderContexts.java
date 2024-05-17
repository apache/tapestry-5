// Copyright 2024 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.internal.plastic.PlasticClassLoader;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.pageload.PageClassLoaderContext;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;

/**
 * Shows information about the page classloader contexts.
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class PageClassLoaderContexts
{

    @Inject
    private PageClassLoaderContextManager pageClassLoaderContextManager;
    
    void onRender(MarkupWriter writer)
    {
        final PageClassLoaderContext root = pageClassLoaderContextManager.getRoot();
        render(root, writer);
    }

    private void render(PageClassLoaderContext context, MarkupWriter writer) 
    {
        
        final int classes = context.getClassNames().size();
        writer.element("li");
        writer.element("details");
        writer.element("summary");
        writer.element("span", "class", "glyphicon glyphicon-zoom-in");
        writer.end(); // span
        writer.write(context.getName());
        writer.write(" (");
        writer.write(((PlasticClassLoader) context.getClassLoader()).getClassLoaderId());
        writer.write(", ");
        writer.write(String.valueOf(classes));
        if (classes > 1)
        {
            writer.write(" classes)");
        }
        else
        {
            writer.write(" class)");
        }
        writer.end(); // summary

        if (!context.isRoot() && !context.getClassNames().isEmpty())
        {
            final List<String> classNames = new ArrayList<>(context.getClassNames());
            Collections.sort(classNames);
            writer.element("ul");
            for (String className : classNames) 
            {
                writer.element("li").text(className);
                writer.end(); // li
            }
            writer.end(); // ul
        }
        
        writer.end(); // details
        
        final List<PageClassLoaderContext> children = new ArrayList<>(context.getChildren());
        
        if (!children.isEmpty())
        {
            children.sort(Comparator.comparing(PageClassLoaderContext::getName));
            writer.element("ul");
            for (PageClassLoaderContext child : children) 
            {
                writer.element("li");
                render(child, writer);
                writer.end(); // li
            }
            writer.end(); // ul
        }
        
        writer.end(); //li
    }
    
}
