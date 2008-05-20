// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import java.lang.annotation.*;

/**
 * Indicates that a method should only be evaluated once and the result cached. All further calls to the method will
 * return the cached result. Note that this annotation is inheritence-safe; if a subclass calls a superclass method that
 * has \@Cached then the value the subclass method gets is the cached value.
 * <p/>
 * The watch parameter can be passed a binding expression which will be evaluated each time the method is called. The
 * method will then only be executed the first time it is called and after that only when the value of the binding
 * changes. This can be used, for instance, to have the method only evaluated once per iteration of a loop by setting
 * watch to the value or index of the loop.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cached
{
    /**
     * The optional binding to watch (default binding prefix is "prop").
     */
    String watch() default "";
}
