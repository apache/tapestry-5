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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * Used to override the default {@link org.apache.tapestry5.services.BeanBlockSource} for a particular data type.  The
 * service accepts the same configuration of {@link org.apache.tapestry5.services.BeanBlockContribution}s as the main
 * service.
 */
@UsesConfiguration(BeanBlockContribution.class)
public interface BeanBlockOverrideSource
{
    /**
     * Returns a block which can be used to render an editor for the given data type, in the form of a field label and
     * input field.
     *
     * @param datatype logical name for the type of data to be displayed
     * @return the Block
     * @throws null if no override is available
     */
    Block getEditBlock(String datatype);

    /**
     * Returns a block which can be used to render output for the given data type.
     *
     * @param datatype logical name for the type of data to be displayed
     * @return the Block
     * @throws null if no override is available
     */
    Block getDisplayBlock(String datatype);

    /**
     * Checks to see if there is a display block for the indicated data type.
     *
     * @param datatype to check for
     * @return true if an override display block is available
     */
    boolean hasDisplayBlock(String datatype);
}
