// Copyright 2024, 2025 The Apache Software Foundation
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

import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;

public class ThrowawayClassLoader extends ClassLoader
{
    private final ClassLoader parent;

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
            if (c != null)
                return c;

            if (name.contains(".base.") || name.contains(".pages.") || name.contains(".components.")
                    || name.contains(".mixins."))
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

            return c;
        }
    }

    public static Class<?> load(final String className)
    {
        ThrowawayClassLoader loader = new ThrowawayClassLoader(
                ThrowawayClassLoader.class.getClassLoader());
        try
        {
            return loader.loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    static ClassLoader create(final ClassLoader parentClassLoader)
    {
        return new ThrowawayClassLoader(parentClassLoader);
    }
}
