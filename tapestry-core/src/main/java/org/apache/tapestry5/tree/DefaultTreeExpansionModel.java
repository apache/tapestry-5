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

import java.util.Set;

import org.apache.tapestry5.BaseOptimizedSessionPersistedObject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

/**
 * Manages a Set of String {@link TreeNode} ids.
 * 
 * @param <T>
 * @since 5.3.0
 * @see TreeModel
 */
public class DefaultTreeExpansionModel<T> extends BaseOptimizedSessionPersistedObject implements TreeExpansionModel<T>
{
    private final Set<String> expandedIds = CollectionFactory.newSet();

    private final Set<String> selectedIds = CollectionFactory.newSet();

    public boolean isExpanded(TreeNode<T> node)
    {
        return contains(expandedIds, node);
    }

    public void markExpanded(TreeNode<T> node)
    {
        add(expandedIds, node);
    }

    public void markCollapsed(TreeNode<T> node)
    {
        remove(expandedIds, node);
    }

    public boolean isSelected(TreeNode<T> node)
    {
        return contains(selectedIds, node);
    }

    public void select(TreeNode<T> node)
    {
         add(selectedIds, node);
    }

    public void unselect(TreeNode<T> node)
    {
        remove(selectedIds, node);
    }

    public void clear()
    {
        clearSet(expandedIds);

        clearSet(selectedIds);
    }

    private void add(Set<String> ids, TreeNode<T> node)
    {
        assert node != null;

        if (ids.add(node.getId()))
            markDirty();
    }

    private void remove(Set<String> ids, TreeNode<T> node)
    {
        assert node != null;

        if (ids.remove(node.getId()))
            markDirty();
    }

    private boolean contains(Set<String> ids, TreeNode<T> node)
    {
        assert node != null;

        return ids.contains(node.getId());
    }

    private void clearSet(Set<String> set)
    {
        if (!set.isEmpty())
        {
            set.clear();
            markDirty();
        }
    }
}
