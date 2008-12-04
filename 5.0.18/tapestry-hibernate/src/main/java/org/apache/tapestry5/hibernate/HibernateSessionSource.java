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

package org.apache.tapestry5.hibernate;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Responsible for creating a Hibernate session as needed. Internally, is responsible for Hibernate {@link
 * Configuration}, resulting in a {@link SessionFactory}.
 * <p/>
 * The service's configuration is a {@linkplain org.apache.tapestry5.ioc.services.ChainBuilder chain of command} of
 * configurator objects.
 */
@UsesOrderedConfiguration(HibernateConfigurer.class)
public interface HibernateSessionSource
{
    /**
     * Creates a new session using the {@link #getSessionFactory() SessionFactory} created at service startup.
     */
    Session create();

    /**
     * Returns the SessionFactory from which Hibernate sessions are created.
     */
    SessionFactory getSessionFactory();

    /**
     * Returns the final configuration used to create the {@link SessionFactory}. The configuration is immutable.
     */
    Configuration getConfiguration();
}
