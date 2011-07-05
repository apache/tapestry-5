// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.jmx;

import javax.management.ObjectName;

/**
 * Creates an MBean server and registers MBeans with the created server. The registered MBeans are unregistered when
 * Registry is shut down.
 * 
 * @since 5.2.0
 */
public interface MBeanSupport
{

    /**
     * Registers the specified MBean with the server.
     * 
     * @param bean
     *            the MBean instance
     * @param objectName
     *            the name for the MBean
     */
    void register(Object bean, ObjectName objectName);

    /**
     * Registers the specific MBean with the server.
     * 
     * @param bean
     *            the MBean instance
     * @param name
     *            string name used to create an {@link ObjectName}
     * @since 5.3
     */
    void register(Object bean, String name);

    /**
     * Unregisters the specified MBean from the server.
     * 
     * @param objectName
     *            the name for the MBean
     */
    void unregister(ObjectName objectName);
}
