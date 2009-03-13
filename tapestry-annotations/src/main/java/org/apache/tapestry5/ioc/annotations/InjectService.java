// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;


/**
 * Annotation used with parameters of service builder methods to identify the service to be injected into the service
 * builder method via the parameter. In many cases the {@link org.apache.tapestry5.ioc.annotations.Inject} annotation is
 * more flexible or appropriate.
 * <p/>
 * This annotation may also be used with fields of service implementation classes, modules, or other objects constructed
 * via {@link org.apache.tapestry5.ioc.ObjectLocator#autobuild(Class)}.
 */
@Target({PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
public @interface InjectService
{

    /**
     * The id of the service to inject; either a fully qualified id, or the unqualified id of a service within the same
     * module.
     */
    String value();
}
