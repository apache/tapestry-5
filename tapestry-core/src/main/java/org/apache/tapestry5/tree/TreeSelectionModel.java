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

/**
 * Tracks which <em>leaf</em> nodes of a {@link TreeModel} are currently selected. The {@linkplain DefaultTreeSelectionModel default
 * implementation} simply stores a set of {@linkplain TreeNode#getId() unique node
 * ids} to identify selected nodes. The selection model is updated whenever the user clicks on the label for a leaf node.
 *
 * In the future, new methods may be added that will support selection of folders as well as leafs, and define the rules for
 * how selections and de-selections propagate down to children or up to parents.
 *
 * @param <T> type of node
 * @see org.apache.tapestry5.corelib.components.Tree
 * @since 5.3
 */
public interface TreeSelectionModel<T>
{

    /**
     * Returns {@code true}, if the given node is selected.
     *
     * @param node node to check
     */
    boolean isSelected(TreeNode<T> node);

    /**
     * Selects a node.
     *
     * @param node node to select
     */
    void select(TreeNode<T> node);

    /**
     * Unselects a node.
     *
     * @param node node to unselect
     */
    void unselect(TreeNode<T> node);

    /**
     * Clears the selection.
     */
    void clear();
}
