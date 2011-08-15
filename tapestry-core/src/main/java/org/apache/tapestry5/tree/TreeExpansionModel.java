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

import org.apache.tapestry5.corelib.components.Tree;

/**
 * Tracks which nodes of a {@link TreeModel} are currently expanded. The {@linkplain DefaultTreeExpansionModel default
 * implementation} simply stores a set of {@linkplain TreeNode#getId() unique node
 * ids} to identify expanded nodes. The expansion model is updated whenever folders are expanded or
 * collapsed on the client side.
 *
 * @since 5.3
 * @see Tree
 */
public interface TreeExpansionModel<T>
{
    /**
     * Returns true if the node has been previously {@linkplain #markExpanded(TreeNode) expanded}.
     *
     * @param node
     *            node to check for expansion
     * @return  true if expanded
     */
    boolean isExpanded(TreeNode<T> node);

    /** Marks the node as expanded. */
    void markExpanded(TreeNode<T> node);

    /** Marks the node as collapsed (not expanded). */
    void markCollapsed(TreeNode<T> node);

    /** Marks all nodes as collapsed. */
    void clear();
}
