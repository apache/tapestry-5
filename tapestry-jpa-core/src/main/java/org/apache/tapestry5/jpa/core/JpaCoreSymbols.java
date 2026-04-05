// Copyright 2011, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.jpa.core;

/**
 * Configuration symbols, for use with contributions to {@link org.apache.tapestry5.ioc.services.ApplicationDefaults}.
 *
 * @since 5.3
 */
public class JpaCoreSymbols
{
    /**
     * If "true", then JPA will be started up at application launch, rather than lazily.
     * 
     * @since 5.3
     */
    public static final String EARLY_START_UP = "tapestry.jpa.early-startup";

    /**
     * The location of the persistence configuration file, located on the classpath. This
     * will normally be <code>/META-INF/persistence.xml</code>.
     * 
     * @since 5.3
     */
    public static final String PERSISTENCE_DESCRIPTOR = "tapestry.jpa.persistence-descriptor";
}
