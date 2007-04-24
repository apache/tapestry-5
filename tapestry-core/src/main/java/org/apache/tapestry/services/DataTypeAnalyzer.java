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

package org.apache.tapestry.services;

import org.apache.tapestry.corelib.components.BeanEditForm;
import org.apache.tapestry.corelib.components.Grid;
import org.apache.tapestry.ioc.services.PropertyAdapter;

/**
 * Used by {@link BeanModelSource} to identify the type of data associated with a particular
 * property (represented as a {@link PropertyAdapter}). The data type is a string used to determine
 * what kind of interface to use for displaying the value of the property, or what kind of interface
 * to use for editting the value of the property. Command property types are "text", "enum",
 * "checkbox", but the list is extensible.
 * 
 * @see Grid
 * @see BeanEditForm
 */
public interface DataTypeAnalyzer
{
    /** Identifies the data type, if known, or returns null if not known. */
    String identifyDataType(PropertyAdapter adapter);
}
