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

package org.apache.tapestry5.ioc;

/**
 * A callback used to create a service implementation.
 */
public interface ServiceBuilder<T>
{
    /**
     * Construct the service.  A non-null object that implements the service interface must be returned.
     *
     * @param resources used to lookup dependencies or access resources
     * @return the core service implementation
     */
    T buildService(ServiceResources resources);
}
