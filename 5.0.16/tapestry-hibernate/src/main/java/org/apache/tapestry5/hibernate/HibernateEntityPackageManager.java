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

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

import java.util.Collection;

/**
 * Contains a set of contributed package names from which to load entities.
 * <p/>
 * The service's configuration is the names of Java packages to search for Hibernate entities.
 */
@UsesConfiguration(String.class)
public interface HibernateEntityPackageManager
{
    /**
     * Returns packages from which read entity classes
     */
    Collection<String> getPackageNames();
}
