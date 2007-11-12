// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Set;

/**
 * Tricky to test, since the code is literally hunting around inside its own brain. There's a lot of
 * room for unintended consequences here.
 */
public class ClassNameLocatorImplTest extends Assert
{
    /**
     * Use various packages in tapestry-ioc to test this, as those don't change unexpectedly(-ish)
     * and we know they are in a JAR on the classpath.
     */
    @Test
    public void classes_in_jar_file()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl();

        Collection<String> names = locator
                .locateClassNames("org.apache.tapestry.ioc.internal.util");

        assertInList(
                names,
                "org.apache.tapestry.ioc.internal.util",
                "MessagesImpl",
                "LocalizedNameGenerator",
                "IdAllocator");
        assertNotInList(
                names,
                "org.apache.tapestry.ioc.internal.util",
                "Orderer$1",
                "InheritanceSearch$State");
    }

    @Test
    public void classes_in_subpackage_in_jar_file()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl();

        Collection<String> names = locator.locateClassNames("org.apache.tapestry.ioc");

        assertInList(
                names,
                "org.apache.tapestry.ioc",
                "internal.Module",
                "internal.util.MessagesImpl");

    }

    /**
     * This time, we use a selection of classes from tapestry-core, since those will never be
     * packaged inside a JAR at this time.
     */

    @Test
    public void classes_in_local_folders()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl();

        Collection<String> names = locator
                .locateClassNames("org.apache.tapestry.corelib.components");

        assertInList(names, "org.apache.tapestry.corelib.components", "ActionLink", "Label");

        assertNotInList(names, "org.apache.tapestry.corelib", "Label$1", "Loop$1");
    }

    @Test
    public void classes_in_subpackages_in_local_folders()
    {
        ClassNameLocator locator = new ClassNameLocatorImpl();

        Collection<String> names = locator.locateClassNames("org.apache.tapestry.corelib");

        assertInList(
                names,
                "org.apache.tapestry.corelib",
                "components.ActionLink",
                "base.AbstractField",
                "mixins.RenderInformals");

        assertNotInList(names, "org.apache.tapestry.corelib", "components.Label$1");
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
