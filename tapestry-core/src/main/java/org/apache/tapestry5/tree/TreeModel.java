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

import javax.swing.tree.TreeSelectionModel;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.corelib.components.Tree;

/**
 * A model for tree-oriented data used by the {@link Tree} component. The default implemention, {@link DefaultTreeModel}
 * uses a {@link ValueEncoder} and a {@link TreeModelAdapter} to supply the
 * underlying information.
 * 
 * @param <T>
 *            type of data in the tree
 * @since 5.3
 * @see TreeSelectionModel
 */
public interface TreeModel<T>
{
    /**
     * Returns the node or nodes that are the top level of the tree.
     */
    List<TreeNode<T>> getRootNodes();

    /**
     * Locates a node in the tree by its unique id.
     * 
     * @throws IllegalArgumentException
     *             if no such node exists
     * @see TreeNode#getId()
     */
    TreeNode<T> getById(String id);

    /**
     * Recursively searches from the root nodes to find the tree node that matches
     * the provided element.
     * 
     * @param element
     *            to search for
     * @return matching node, or null if not found
     */
    TreeNode<T> find(T element);
}
