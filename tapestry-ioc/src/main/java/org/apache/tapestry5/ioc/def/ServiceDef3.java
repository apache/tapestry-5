// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.def;

import org.apache.tapestry5.ioc.AnnotationAccess;
import org.apache.tapestry5.ioc.annotations.IncompatibleChange;

/**
 * Introduced for Tapestry 5.3, contains new methods to provide access to annotations on the class,
 * and on methods of the class. In rare cases, the same annotation type will appear on the service interface
 * and on the class (or method implementation in the class); the implementation annotation always
 * has precedence over the interface annotation.
 * 
 * @since 5.3
 */
public interface ServiceDef3 extends ServiceDef2, AnnotationAccess
{
    
    /**
     * Returns the service implementation associated with this service. When creating the proxies
     * for the service interface, the implementation of the service interface methods will
     * receive the annotations of the corresponding method in the service interface.
     * @since 5.4
     */
    @IncompatibleChange(release = "5.4", details = "Added method")
    Class getServiceImplementation();

}
