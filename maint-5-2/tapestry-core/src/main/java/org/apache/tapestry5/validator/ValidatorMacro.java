// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.validator;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Allows support for "validator macros", a simple-minded way of combining several related valiations together under
 * a single name. The service's configuration maps string keys (macro names) to string values (validation constraints).
 * 
 * @since 5.2.0
 */
@UsesMappedConfiguration(String.class)
public interface ValidatorMacro
{
    /**
     * Given a <em>potential</em> validator macro (a simple string name), returns the value for that macro, a
     * comma-separated list of validation constraints.
     * 
     * @return constraints, or null if no such validator macro
     */
    String valueForMacro(String validatorMacro);
}
