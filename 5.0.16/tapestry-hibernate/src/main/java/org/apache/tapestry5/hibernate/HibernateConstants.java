//  Copyright 2008 The Apache Software Foundation
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

/**
 * Defines constants used inside the Tapestry Hibernate intergration.
 */
public class HibernateConstants
{
    /**
     * If true (the default), then {@link org.apache.tapestry5.ValueEncoder}s are automatically created for each entity.
     * Override to "false" to handle entity value encoding explicitly.
     */
    public static final String PROVIDE_ENTITY_VALUE_ENCODERS_SYMBOL = "tapestry.hibernate.provide-entity-value-encoders";

    /**
     * If true, then the last {@link org.apache.tapestry5.hibernate.HibernateConfigurer} will invoke {@link
     * org.hibernate.cfg.Configuration#configure()}, to read the application's <code>hibernate.cfg.xml</code>. This
     * should be set to false for applications that configure exclusively in code.
     */
    public static final String DEFAULT_CONFIGURATION = "tapestry.hibernate.default-configuration";
}
