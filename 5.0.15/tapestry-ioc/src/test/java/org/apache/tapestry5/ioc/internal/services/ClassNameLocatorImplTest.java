// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Set;

/**
 * Tricky to test, since the code is literally hunting around inside its own brain. There's a lot of room for unintended
 * consequences here.
 */
public class ClassNameLocatorImplTest extends Assert
{
    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();

    /**
     * Use various packages in javassist to test this, as those don't change unexpectedly(-ish) and we know they are in
     * a JAR on the classpath.
     */
    @Test
    public void classes_in_jar_file()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl(converter);

        Collection<String> names = locator
                .locateClassNames("javassist.util");

        assertInList(
                names,
                "javassist.util",
                "HotSwapper",
                "Trigger");
        assertNotInList(
                names,
                "javassist.util",
                "Orderer$1");
    }

    @Test
    public void classes_in_subpackage_in_jar_file()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl(converter);

        Collection<String> names = locator.locateClassNames("javassist.util");

        assertInList(
                names,
                "javassist.util",
                "proxy.ProxyFactory");

    }

    /**
     * This time, we use a selection of classes from tapestry-ioc, since those will never be packaged inside a JAR at
     * this time.
     */

    @Test
    public void classes_in_local_folders()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl(converter);

        Collection<String> names = locator
                .locateClassNames("org.apache.tapestry5.ioc.services");

        assertInList(names, "org.apache.tapestry5.ioc.services", "SymbolSource", "TapestryIOCModule");

        assertNotInList(names, "org.apache.tapestry5.ioc.services", "TapestryIOCModule$1");
    }

    @Test
    public void classes_in_subpackages_in_local_folders()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl(converter);

        Collection<String> names = locator.locateClassNames("org.apache.tapestry5");

        assertInList(
                names,
                "org.apache.tapestry5",
                "ioc.Orderable",
                "ioc.services.ChainBuilder");

        assertNotInList(names, "org.apache.tapestry5.ioc", "services.TapestryIOCModule$1");
    }

    void assertInList(Collection<String> names, String packageName, String... classNames)
    {
        Set<String> classNameSet = CollectionFactory.newSet(names);

        for (String className : classNames)
        {
            String fullName = packageName + "." + className;

            if (classNameSet.contains(fullName))
                continue;

            String message = String.format("%s not found in %s.", fullName, InternalUtils
                    .joinSorted(names));

            throw new AssertionError(message);
        }
    }

    void assertNotInList(Collection<String> names, String packageName, String... classNames)
    {
        Set<String> classNameSet = CollectionFactory.newSet(names);

        for (String className : classNames)
        {
            String fullName = packageName + "." + className;

            if (!classNameSet.contains(fullName))
                continue;

            String message = String.format("%s found in %s.", fullName, InternalUtils
                    .joinSorted(names));

            throw new AssertionError(message);
        }
    }
}
