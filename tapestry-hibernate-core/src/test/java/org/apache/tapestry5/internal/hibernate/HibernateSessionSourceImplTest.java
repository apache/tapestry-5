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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateConfigurer;
import org.apache.tapestry5.hibernate.HibernateEntityPackageManager;
import org.apache.tapestry5.hibernate.HibernateSessionSource;
import org.apache.tapestry5.ioc.internal.services.ClassNameLocatorImpl;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.ioc.test.TestBase;
import org.example.app0.entities.User;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HibernateSessionSourceImplTest extends IOCTestCase
{
    private final Logger log = LoggerFactory
            .getLogger("tapestry.hibernate.HibernateSessionSourceTest");

    @Test
    public void startup_without_packages()
    {
        Collection<String> packageNames = CollectionFactory.newList(
                "org.example.myapp.entities",
                "org.example.app0.entities");
        HibernateEntityPackageManager packageManager = newMock(HibernateEntityPackageManager.class);
        TestBase.expect(packageManager.getPackageNames()).andReturn(packageNames);

        List<HibernateConfigurer> filters = Arrays.asList(
                new DefaultHibernateConfigurer(true),
                new PackageNameHibernateConfigurer(packageManager,
                                                   new ClassNameLocatorImpl(new ClasspathURLConverterImpl())));

        replay();
        HibernateSessionSource source = new HibernateSessionSourceImpl(log, filters);

        Session session = source.create();
        Assert.assertNotNull(session);

        // make sure it found the entity in the package
        ClassMetadata meta = session.getSessionFactory().getClassMetadata(User.class);
        Assert.assertEquals(meta.getEntityName(), "org.example.app0.entities.User");

        verify();
    }

    @Test
    public void get_configuration()
    {
        HibernateConfigurer configurer = new HibernateConfigurer()
        {
            public void configure(Configuration configuration)
            {
                configuration.setProperty("foo", "bar");
                configuration.configure();
            }
        };
        HibernateSessionSource source = new HibernateSessionSourceImpl(log, Arrays
                .asList(configurer));

        Configuration config = source.getConfiguration();
        Assert.assertNotNull(config);
        Assert.assertEquals("bar", config.getProperty("foo"));

        // configuration should be immutable
        try
        {
            config.setProperty("hibernate.dialect", "foo");
            Assert.fail("did not throw");
        }
        catch (UnsupportedOperationException e)
        {
            Assert.assertTrue(e.getMessage().contains("immutable"));
        }
    }
}
