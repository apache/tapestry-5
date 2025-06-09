// Copyright 2024 The Apache Software Foundation
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

package org.apache.tapestry5.internal;


import static org.testng.Assert.assertNotEquals;

import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;

public class ThrowawayClassLoader extends ClassLoader
{

    final private ClassLoader parent;
    
    public ThrowawayClassLoader(ClassLoader parent) 
    {
        super(parent);
        this.parent = parent;
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException 
    {
        synchronized (getClassLoadingLock(name)) 
        
        {
            // First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                if (name.contains(".base.") || name.contains(".pages.") ||
                        name.contains(".components.") || name.contains(".mixins."))
                {
                    final byte[] bytes = PlasticInternalUtils.readBytecodeForClass(parent, name, true);
                    c = defineClass(name, bytes, 0, bytes.length);
                    if (resolve) 
                    {
                        resolveClass(c);
                    }
                }
                else
                {
                    c = parent.loadClass(name);
                }
            }
            return c;
        }    
    }

    public static void main(String[] args) throws Exception 
    {
        
        final String className = "org.apache.tapestry5.corelib.components.BeanEditor";
        
        final ClassLoader parentClassLoader = ThrowawayClassLoader.class.getClassLoader();
        ClassLoader classLoader1 = create(parentClassLoader);
        ClassLoader classLoader2 = create(parentClassLoader);
        
        System.out.println("Parent class loader 1: " + parentClassLoader);
        System.out.println("Class loader 1       : " + classLoader1);
        System.out.println("Class loader 2       : " + classLoader2);
        
        Class class1 = classLoader1.loadClass(className);
        Class class2 = classLoader2.loadClass(className);
        Class class3 = parentClassLoader.loadClass(className);

        System.out.println("Class 1 : " + class1.getClassLoader());
        System.out.println("Class 2 : " + class2.getClassLoader());
        System.out.println("Class 3 : " + class3.getClassLoader());        
        
        assertNotEquals(class1, class2);
        assertNotEquals(class1, class3);
        assertNotEquals(class2, class3);
        
    }
    
    public static Class<?> load(final String className)
    {
        ThrowawayClassLoader loader = new ThrowawayClassLoader(
                ThrowawayClassLoader.class.getClassLoader());
        try 
        {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) 
        {
            throw new RuntimeException(e);
        }
    }

    private static ClassLoader create(final ClassLoader parentClassLoader) {
//        return TapestryInternalUtils.createThrowawayClassloader(parentClassLoader);
        return new ThrowawayClassLoader(parentClassLoader);
    }
    
}
