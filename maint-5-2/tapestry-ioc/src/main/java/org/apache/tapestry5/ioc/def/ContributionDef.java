// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.def;

import org.apache.tapestry5.ioc.*;

/**
 * Contribution to a service configuration.
 * <p/>
 * The toString() method of the ContributionDef will be used for some exception reporting and should clearly identify
 * where the contribution comes from; the normal behavior is to identify the class and method of the contribution
 * method.
 */
public interface ContributionDef
{
    /**
     * Identifies the service contributed to.
     */
    String getServiceId();

    /**
     * Performs the work needed to contribute into the standard, unordered configuration.
     *
     * @param moduleSource  the source, if needed, of the module  instance associated with the contribution
     * @param resources     allows access to services visible to the module
     * @param configuration the unordered configuration into which values should be loaded. This instance will
     *                      encapsulate all related error checks (such as passing of nulls or inappropriate classes).
     */
    void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                    Configuration configuration);

    /**
     * Performs the work needed to contribute into the ordered configuration.
     *
     * @param moduleSource  the source, if needed, of the module instance associated with the contribution
     * @param resources     allows access to services visible to the module
     * @param configuration the ordered configuration into which values should be loaded. This instance will encapsulate
     *                      all related error checks (such as passing of nulls or inappropriate classes).
     */
    void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                    OrderedConfiguration configuration);

    /**
     * Performs the work needed to contribute into the mapped configuration.
     *
     * @param moduleSource  the source, if needed, of the module instance associated with the contribution
     * @param resources     allows access to services visible to the module
     * @param configuration the mapped configuration into which values should be loaded. This instance will encapsulate
     *                      all related error checks (such as passing of null keys or values or inappropriate classes,
     *                      or duplicate keys).
     */
    void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                    MappedConfiguration configuration);
}
