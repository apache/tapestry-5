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

import org.apache.tapestry.Block;
import org.apache.tapestry.corelib.components.BeanEditForm;
import org.apache.tapestry.corelib.components.Grid;

/**
 * A source of {@link Block}s used to display the properties of a bean (used by the {@link Grid}
 * component), or to edit the properties of a bean (used by the {@link BeanEditForm} component).
 * Contributions to this service define what properties may be editted.
 * 
 * @see DataTypeAnalyzer
 */
public interface BeanBlockSource
{
    /**
     * Returns a block which can be used to present an editor for the given data type, in the form
     * of a field label and input field.
     * 
     * @param datatype
     *            logical name for the type of data to be displayed
     * @return the Block
     * @throws RuntimeException
     *             if no appropriate block is available
     */
    Block getEditBlock(String datatype);

    /**
     * Returns a block which can be used to present an output for the given data type.
     * 
     * @param datatype
     *            logical name for the type of data to be displayed
     * @return the Block
     * @throws RuntimeException
     *             if no appropriate block is available
     */
    Block getDisplayBlock(String datatype);

    /**
     * Checks to see if there is a display block for the indicated data type.
     * 
     * @param datatype
     *            to check for
     * @return true if a block is available
     */
    boolean hasDisplayBlock(String datatype);
}
