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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * A source of {@link Block}s used to display the properties of a bean (used by the {@link
 * org.apache.tapestry5.corelib.components.Grid} component), or to edit the properties of a bean (used by the {@link
 * org.apache.tapestry5.corelib.components.BeanEditForm} component). Contributions to this service (a configuration of
 * {@link BeanBlockContribution}s) define what properties may be editted.
 * <p/>
 * Blocks are accessed in terms of a <strong>data type</strong> a string that identifies the type of data to be editted,
 * such as "string", "date", "boolean", etc.
 * <p/>
 * Tapestry contributes a number of default data types and corresponding edit and display blocks. The {@link
 * org.apache.tapestry5.services.BeanBlockOverrideSource} service allows these to be overridden.
 *
 * @see org.apache.tapestry5.services.DataTypeAnalyzer
 * @see org.apache.tapestry5.services.TapestryModule#contributeBeanBlockSource(org.apache.tapestry5.ioc.Configuration)
 */
@UsesConfiguration(BeanBlockContribution.class)
public interface BeanBlockSource
{
    /**
     * Returns a block which can be used to render an editor for the given data type, in the form of a field label and
     * input field.
     *
     * @param datatype logical name for the type of data to be displayed
     * @return the Block
     * @throws RuntimeException if no appropriate block is available
     */
    Block getEditBlock(String datatype);

    /**
     * Returns a block which can be used to render output for the given data type.
     *
     * @param datatype logical name for the type of data to be displayed
     * @return the Block
     * @throws RuntimeException if no appropriate block is available
     */
    Block getDisplayBlock(String datatype);

    /**
     * Checks to see if there is a display block for the indicated data type.
     *
     * @param datatype to check for
     * @return true if a block is available
     */
    boolean hasDisplayBlock(String datatype);
}
