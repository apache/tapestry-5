// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.model.ComponentModel;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Allows for the specification of per-component meta-data. Meta data can later be accessed via {@link
 * ComponentModel#getMeta(String)}. Meta data keys are case insensitive. Meta data defined by a subclass overrides meta
 * data for the super class (where the keys conflict).
 *
 * @see org.apache.tapestry5.MetaDataConstants
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface Meta
{
    /**
     * The meta data as a list of "name=value" elements.
     */
    String[] value();
}
