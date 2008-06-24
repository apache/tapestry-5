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

package org.apache.tapestry5.model;

import org.apache.tapestry5.annotations.Parameter;

/**
 * Model for a <em>formal</em> parameter of a component.
 *
 * @see Parameter
 */
public interface ParameterModel
{
    /**
     * The name of the parameter.
     */
    String getName();

    /**
     * If true, the parameter is required.
     */
    boolean isRequired();

    /**
     * If true, then no check is needed. If false, then the bound value must not be null.
     */
    boolean isAllowNull();

    /**
     * The default binding prefix for the parameter, usually "prop".
     */
    String getDefaultBindingPrefix();
}
