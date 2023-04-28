// Copyright 2023 The Apache Software Foundation
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
package org.apache.tapestry5.services.pageload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.internal.plastic.PlasticClassLoader;
import org.apache.tapestry5.internal.plastic.PlasticClassPool;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that encapsulates a classloader context for Tapestry's live class reloading.
 * Each instance contains basically a classloader, a set of classnames, a parent
 * context (possibly null) and child contexts (possibly empty).
 */
public class PageClassloaderContext 
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PageClassloaderContext.class);
    
    private final String name;
    
    private final PageClassloaderContext parent;
    
    private final Set<String> classNames = new HashSet<>();
    
    private final Set<PageClassloaderContext> children;
    
    private final PlasticManager plasticManager;
    
    private final PlasticProxyFactory proxyFactory;
    
    private PageClassloaderContext root;

    /**
     * Name of the <code>unknown</code> context (i.e. the one for controlled classes
     * without dependency information at the moment).
     */
    public static final String UNKOWN_CONTEXT_NAME = "unknown";

    public PageClassloaderContext(String name, 
            PageClassloaderContext parent, 
            Set<String> classNames, 
            PlasticProxyFactory plasticProxyFactory) 
    {
        super();
        this.name = name;
        this.parent = parent;
        this.classNames.addAll(classNames);
        this.plasticManager = plasticProxyFactory.getPlasticManager();
        this.proxyFactory = plasticProxyFactory;
        children = new HashSet<>();
        if (plasticProxyFactory.getClassLoader() instanceof PlasticClassLoader)
        {
           final PlasticClassLoader plasticClassLoader = (PlasticClassLoader) plasticManager.getClassLoader();
           plasticClassLoader.setTag(name);
           plasticClassLoader.setFilter(this::filter);
           plasticClassLoader.setAlternativeClassloading(this::alternativeClassLoading);
           if (parent != null)
           {
               getPlasticManager().getPool().setParent(parent.getPlasticManager().getPool());
           }
        }
    }

    private Class<?> alternativeClassLoading(String className) 
    {
        Class<?> clasz = null;
        setRootFieldIfNeeded();
        PageClassloaderContext context = root.findByClassName(
                PlasticUtils.getEnclosingClassName(className));
        if (isRoot() && context == null)
        {
            context = this;
        }
        if (context != null)
        {
            try 
            {
                final PlasticClassLoader classLoader = (PlasticClassLoader) context.getClassLoader();
                // Avoiding infinite recursion
                synchronized (classLoader) 
                {
                    classLoader.setAlternativeClassloading(null);
                    clasz = classLoader.loadClass(className);
                    classLoader.setAlternativeClassloading(this::alternativeClassLoading);
                }
            } catch (ClassNotFoundException e) 
            {
                throw new RuntimeException(e);
            }
        }
        return clasz;
    }
    
    private void setRootFieldIfNeeded()
    {
        if (root == null)
        {
            if (isRoot())
            {
                root = this;
            }
            else
            {
                root = this;
                while (!root.isRoot())
                {
                    root = root.getParent();
                }
            }
        }
    }

    /**
     * Returns the name of this context.
     */
    public String getName() 
    {
        return name;
    }
    
    /**
     * Returns the parent of this context.
     */
    public PageClassloaderContext getParent() 
    {
        return parent;
    }

    /**
     * Returns the set of classes that belong in this context.
     */
    public Set<String> getClassNames() 
    {
        return classNames;
    }
    
    /**
     * Returns the children of this context.
     */
    public Set<PageClassloaderContext> getChildren() 
    {
        return children;
    }

    /**
     * Returns this context's {@linkplain PlasticManager} instance.
     */
    public PlasticManager getPlasticManager() 
    {
        return plasticManager;
    }
    
    /**
     * Returns this context's {@linkplain PlasticProxyFactory} instance.
     */
    public PlasticProxyFactory getProxyFactory() 
    {
        return proxyFactory;
    }
    
    /**
     * Adds a class to this context.
     */
    public void addClass(String className)
    {
        classNames.add(className);
    }
    
    /**
     * Adds a child context.
     */
    public void addChild(PageClassloaderContext context)
    {
        children.add(context);
    }

    /**
     * Removes a child context.
     */
    public void removeChildren(PageClassloaderContext context)
    {
        children.remove(context);
    }

    /**
     * Searches for the context that contains the given class in itself and recursivel in its children.
     */
    public PageClassloaderContext findByClassName(String className)
    {
        PageClassloaderContext context = null;
        if (classNames.contains(className))
        {
            context = this;
        }
        else
        {
            for (PageClassloaderContext child : children) {
                context = child.findByClassName(className);
                if (context != null)
                {
                    break;
                }
            }
        }
        return context;
    }
    
    /**
     * Returns the {@linkplain ClassLoader} associated with this context.
     */
    public ClassLoader getClassLoader()
    {
        return proxyFactory.getClassLoader();
    }

    /**
     * Invalidates this context and its children recursively. This shouldn't
     * be called directly, just through {@link PageClassloaderContextManager#invalidate(PageClassloaderContext)}.
     */
    public void invalidate() 
    {
        for (PageClassloaderContext child : new ArrayList<>(children)) 
        {
            child.invalidate();
        }
        LOGGER.debug("Invalidating page classloader context '{}' (class loader {}, classes : {})", 
                name, proxyFactory.getClassLoader(), classNames);
//        classNames.clear();
        parent.getChildren().remove(this);
        proxyFactory.clearCache();
    }

    /**
     * Returns whether this is the root context.
     */
    public boolean isRoot()
    {
        return parent == null;
    }

    /**
     * Returns whether this is the <code>unknwon</code> context.
     * @see #UNKOWN_CONTEXT_NAME
     */
    public boolean isUnknown()
    {
        return name.equals(UNKOWN_CONTEXT_NAME);
    }
    
    /**
     * Returns the set of descendents (children and their children recursively
     * of this context.
     */
    public Set<PageClassloaderContext> getDescendents()
    {
        Set<PageClassloaderContext> descendents;
        if (children.isEmpty())
        {
            descendents = Collections.emptySet();
        }
        else
        {
            descendents = new HashSet<>(children);
            for (PageClassloaderContext child : children) 
            {
                descendents.addAll(child.getDescendents());
            }
        }
        return descendents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PageClassloaderContext)) {
            return false;
        }
        PageClassloaderContext other = (PageClassloaderContext) obj;
        return Objects.equals(name, other.name);
    }

    @Override
    public String toString() 
    {
        return "PageClassloaderContext [name=" + name + 
                ", parent=" + (parent != null ? parent.getName() : "null" ) + 
                ", classLoader=" + afterAt(proxyFactory.getClassLoader().toString()) +
                (isRoot() ? ""  : ", classNames=" + classNames) + 
                "]";
    }
    
    public String toRecursiveString()
    {
        StringBuilder builder = new StringBuilder();
        toRecursiveString(builder, "");
        return builder.toString();
    }
    
    private void toRecursiveString(StringBuilder builder, String tabs)
    {
        builder.append(tabs);
        builder.append(name);
        builder.append(" : ");
        builder.append(afterAt(proxyFactory.getClassLoader().toString()));
        builder.append(" : ");
        builder.append(classNames);
        builder.append("\n");
        for (PageClassloaderContext child : children) {
            child.toRecursiveString(builder, tabs + "\t");
        }
    }

    private static String afterAt(String string) 
    {
        int index = string.indexOf('@');
        if (index > 0)
        {
            string = string.substring(index + 1);
        }
        return string;
    }
    
    private boolean filter(String className)
    {
        final int index = className.indexOf("$");
        if (index > 0)
        {
            className = className.substring(0, index);
        }
        // TODO: do we really need the className.contains(".base.") part?
        return classNames.contains(className) || className.contains(".base.") || isUnknown();
    }

}
