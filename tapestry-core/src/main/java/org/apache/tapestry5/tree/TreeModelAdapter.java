// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.tree;

import java.util.List;

/**
 * Used with {@link DefaultTreeModel} to define how to extract labels and child nodes from a value.
 *
 * @since 5.3
 */
public interface TreeModelAdapter<T>
{
    /**
     * Determines if the value is a leaf or a (potential) container of children.
     *
     * @see TreeNode#isLeaf()
     */
    boolean isLeaf(T value);

    /**
     * Returns true if the value has children (only invoked for non-leaf values).
     *
     * @see TreeNode#getHasChildren()
     */
    boolean hasChildren(T value);

    /**
     * Returns the children, in the order they should be presented to the client.
     * This should return the childen in the correct presentation or, or return null or an empty list.
     *
     * @see TreeNode#getChildren()
     */
    List<T> getChildren(T value);

    /**
     * Returns a text label for the value, which may be presented to the client.
     *
     * @see TreeNode#getLabel()
     */
    String getLabel(T value);
}
