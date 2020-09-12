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

package org.apache.tapestry5.commons.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Used by {@link org.apache.tapestry5.beanmodel.services.BeanModelSource} to identify the type of data associated with a particular property (represented as a
 * {@link PropertyAdapter}). The data type is a string used to determine what kind of interface to use for displaying
 * the value of the property, or what kind of interface to use for editing the value of the property. Common property
 * types are "text", "enum", "checkbox", but the list is extensible.
 *
 * Different strategies for identifying the data type are encapsulated in the DataTypeAnalyzer service, forming a
 * chain of command.
 *
 * The DefaultDataTypeAnalyzer service maps property types to data type names.
 *
 * The DataTypeAnalyzer service is an extensible {@linkplain org.apache.tapestry5.ioc.services.ChainBuilder chain of
 * command}), that (by default) includes {@link org.apache.tapestry5.commons.internal.services.AnnotationDataTypeAnalyzer} and
 * the {@link org.apache.tapestry5.commons.internal.services.DefaultDataTypeAnalyzer} service (ordered last).   It uses an ordered configuration.
 *
 * @see org.apache.tapestry5.corelib.components.Grid
 * @see org.apache.tapestry5.corelib.components.BeanEditForm
 * @see org.apache.tapestry5.services.BeanBlockSource
 */
@UsesOrderedConfiguration(DataTypeAnalyzer.class)
@UsesMappedConfiguration(key = Class.class, value = String.class)
public interface DataTypeAnalyzer
{
    /**
     * Identifies the data type, if known, or returns null if not known.
     */
    String identifyDataType(PropertyAdapter adapter);
}
