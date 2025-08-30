// Copyright 2025 The Apache Software Foundation
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

import org.testng.annotations.Test;

class ThrowawayClassLoaderTest
{

    @Test
    void testClassesArenEqualFromDifferentClassloaders() throws ClassNotFoundException
    {
        // ARRANGE

        String className = "org.apache.tapestry5.corelib.components.BeanEditor";

        ClassLoader parentClassLoader = ThrowawayClassLoader.class.getClassLoader();
        ClassLoader classLoader1 = ThrowawayClassLoader.create(parentClassLoader);
        ClassLoader classLoader2 = ThrowawayClassLoader.create(parentClassLoader);

        // ACT

        Class<?> class1 = classLoader1.loadClass(className);
        Class<?> class2 = classLoader2.loadClass(className);
        Class<?> class3 = parentClassLoader.loadClass(className);

        // ASSERT

        assertNotEquals(class1, class2);
        assertNotEquals(class1, class3);
        assertNotEquals(class2, class3);
    }
}
