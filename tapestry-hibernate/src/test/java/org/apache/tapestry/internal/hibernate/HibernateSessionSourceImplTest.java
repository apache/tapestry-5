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

package org.apache.tapestry.internal.hibernate;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.hibernate.HibernateSessionSource;
import org.apache.tapestry.internal.services.ClassNameLocatorImpl;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class HibernateSessionSourceImplTest extends TapestryTestCase
{
    private final Log _log = LogFactory.getLog("tapestry.hibernate.HibernateSessionSourceTest");

    @Test
    public void startup_without_packages()
    {
        Collection<String> packageNames = CollectionFactory.newList(
                "org.example.myapp.entities",
                "org.example.app0.entities");

        HibernateSessionSource source = new HibernateSessionSourceImpl(_log, packageNames,
                new ClassNameLocatorImpl());

        assertNotNull(source.create());
    }
}
