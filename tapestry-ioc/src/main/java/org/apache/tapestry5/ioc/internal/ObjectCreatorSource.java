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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceBuilderResources;

/**
 * An object which can, when passed a {@link ServiceBuilderResources}, create a corresponding {@link ObjectCreator}. A
 * secondary responsibility is to provide a description of the creator, which is usually based on the name of the method
 * or constructor to be invoked, and is ultimately used in some debugging or error output.
 */
public interface ObjectCreatorSource
{
    /**
     * Provides an ObjectCreator that can be used to ultimately instantiate the core service implementation.
     */
    ObjectCreator constructCreator(ServiceBuilderResources resources);

    /**
     * Returns a description of the method or constructor that creates the service.
     */
    String getDescription();
}
