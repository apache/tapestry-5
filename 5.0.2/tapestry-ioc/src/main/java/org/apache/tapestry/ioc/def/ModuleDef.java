// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.def;

import java.util.Set;

/**
 * Defines the contents of a module. In the default case, this is information about the services
 * provided by the module builder class.
 * 
 * 
 */
public interface ModuleDef
{
    /** Returns the fully qualified ids of the services built/provided by the module. */
    Set<String> getServiceIds();

    /**
     * Returns a service definition via the fully qualified service id.
     * 
     * @param serviceId
     *            fully qualified service id
     * @return service definition or null if it doesn't exist
     */
    ServiceDef getServiceDef(String serviceId);

    /**
     * Returns all the decorator definitions built/provided by this module.
     */
    Set<DecoratorDef> getDecoratorDefs();

    /**
     * Returns all the contribution definitions built/provided by this module.
     */
    Set<ContributionDef> getContributionDefs();

    /**
     * Returns the module id extracted from the {@link org.apache.tapestry.ioc.annotations.Id}
     * annotation on the module builder class (or calculated from the module builder's package, if
     * the annotation is not present).
     */
    String getModuleId();

    /**
     * Returns the class that will be instantiated. Annotated instance methods of this class are
     * invoked to build services, to decorate/intercept services, and make contributions to other
     * services.
     */
    Class getBuilderClass();
}