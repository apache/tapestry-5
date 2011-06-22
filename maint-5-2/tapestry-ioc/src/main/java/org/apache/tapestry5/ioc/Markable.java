// Copyright 2010 The Apache Software Foundation
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

import java.util.Set;

import org.apache.tapestry5.ioc.def.ServiceDef;


/**
 * Interface implemented by objects which need to disambiguate services with marker annotations.
 *
 * @since 5.2.2
 */
public interface Markable
{
    /**
     * Returns an optional set of <em>marker annotation</em>. Marker annotations are used to disambiguate services; the
     * combination of a marker annotation and a service type is expected to be unique. Note that it is not possible
     * to identify which annotations are markers and which are not when this set is constructed, so it may include
     * non-marker annotations.
     *
     * @see ServiceDef#getMarkers()
     */
    Set<Class> getMarkers();

    /**
     * Returns the service interface associated with the service.
     *
     * @see ServiceDef#getServiceInterface()
     */
    Class getServiceInterface();
}
