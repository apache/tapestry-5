// Copyright 2011, 2023, 2024 The Apache Software Foundation
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

package org.apache.tapestry5.internal.plastic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.tapestry5.plastic.PlasticUtils;

public class PlasticClassLoader extends ClassLoader
{
    static
    {
        // TAP5-2546
        ClassLoader.registerAsParallelCapable();
    }

    private final ClassLoaderDelegate delegate;
    
    private Predicate<String> filter;
    
    private Function<String, Class<?>> alternativeClassloading;
    
    private Consumer<String> beforeLoadClass;
    
    private Set<String> loadedClasses;
    
    private String tag;
    
    private Map<String, Class<?>> cache;
    
    private static List<String> log = new ArrayList<>();
    
    public PlasticClassLoader(ClassLoader parent, ClassLoaderDelegate delegate) 
    {
        super(parent);
        this.delegate = delegate;
        loadedClasses = new HashSet<>();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        synchronized(getClassLoadingLock(name))
        {
            Class<?> loadedClass = findLoadedClass(name);

            if (loadedClass != null)
                return loadedClass;
            
            if (shouldInterceptClassLoading(name))
            {
                
                if (name.equals("org.apache.tapestry5.integration.app1.pages.GridDemo")) {
                    System.out.println();
                }
                
                Class<?> c = getFromCache(name);
                
                if (c == null)
                {
                
                    if ((filter != null && filter.test(name)) || (filter == null && delegate.shouldInterceptClassLoading(name)))
                    {
    
                        try {
                            log.add(String.format("Loading %s in %s", name, this));
                            c = delegate.loadAndTransformClass(name);
                        }
                        catch (LinkageError e) {
                            e.printStackTrace();
                            for (String entry : log) {
                                System.out.println(entry);
                            }
                            System.out.println();
                            throw e;
                        }
                    }
                    else if (alternativeClassloading != null)
                    {
                        c = alternativeClassloading.apply(name);
                    }
                    
                    if (cache != null && c != null)
                    {
                        cache.put(name, c);
                    }
                    
                }
                
                if (c == null)
                {
                    return super.loadClass(name, resolve);                    
                }
                
                
//                loadedClasses.add(name + " : " + System.currentTimeMillis());

                if (resolve)
                    resolveClass(c);

                return c;
            } else
            {
                return super.loadClass(name, resolve);
            }
        }
    }

    private boolean shouldInterceptClassLoading(String name) {
        return delegate.shouldInterceptClassLoading(
                PlasticUtils.getEnclosingClassName(name));
    }

    public synchronized Class<?> defineClassWithBytecode(String className, byte[] bytecode)
    {
        synchronized(getClassLoadingLock(className))
        {
            if (className.equals("org.apache.tapestry5.integration.app1.pages.GridDemo"))
            {
                System.out.println();
            }
            return defineClass(className, bytecode, 0, bytecode.length);
        }
    }

    /**
     * When alternatingClassloader is set, this classloader delegates to it the 
     * call to {@linkplain ClassLoader#loadClass(String)}. If it returns a non-null object,
     * it's returned by <code>loadClass(String)</code>. Otherwise, it returns 
     * <code>super.loadClass(name)</code>.
     * @since 5.8.3
     */
    public void setAlternativeClassloading(Function<String, Class<?>> alternateClassloading) 
    {
        this.alternativeClassloading = alternateClassloading;
    }
    
    /**
     * @since 5.8.3
     */
    public void setTag(String tag) 
    {
        this.tag = tag;
        if (cache == null)
        {
            cache = Collections.synchronizedMap(new HashMap<>());
        }
    }
    
    /**
     * When a filter is set, only classes accepted by it will be loaded by this classloader.
     * Instead, it will be delegated to alternate classloading first and the parent classloader
     * in case the alternate doesn't handle it.
     * @since 5.8.3
     */
    public void setFilter(Predicate<String> filter) 
    {
        this.filter = filter;
    }

    @Override
    public String toString()
    {
        final String id = getClassLoaderId();
        return String.format("PlasticClassLoader[%s, tag=%s, parent=%s]", id, tag, getParent());
    }

    public String getClassLoaderId() {
        final String superToString = super.toString();
        return superToString.substring(superToString.indexOf('@')).trim();
    }
    
    private Class<?> getFromCache(String name)
    {
        Class<?> c = null;
        if (cache != null && cache.containsKey(name))
        {
            c = cache.get(name);
        }
        if (c == null && getParent() instanceof PlasticClassLoader)
        {
            c = ((PlasticClassLoader) getParent()).getFromCache(name);
        }
        return c;
    }
    
//    /**
//     * Sets a {@linkplain Consumer} that will be called before a transformed
//     * class is loaded.
//     * @param beforeLoadClass the <code>Consumer&lt;String&gt;</code>.
//     * @since 5.8.7
//     */
//    public void setBeforeLoadClass(Consumer<String> beforeLoadClass) 
//    {
//        this.beforeLoadClass = beforeLoadClass;
//    }

}
