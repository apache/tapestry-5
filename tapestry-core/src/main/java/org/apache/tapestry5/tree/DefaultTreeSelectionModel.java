// Copyright 2011, 2013 The Apache Software Foundation
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

import org.apache.tapestry5.BaseOptimizedSessionPersistedObject;
import org.apache.tapestry5.commons.util.CollectionFactory;

import java.util.Set;

/**
 * Default implementation of {@link TreeSelectionModel}. This is simply a wrapper around a set of node ids.
 *
 * @param <T> type of node
 */
public class DefaultTreeSelectionModel<T> extends BaseOptimizedSessionPersistedObject implements TreeSelectionModel<T>
{
    private static final long serialVersionUID = -2568582442906389898L;

    private final Set<String> selectedIds = CollectionFactory.newSet();

    public boolean isSelected(TreeNode<T> node)
    {
        assert node != null;

        return selectedIds.contains(node.getId());
    }

    public void select(TreeNode<T> node)
    {
        assert node != null;

        if (selectedIds.add(node.getId()))
            markDirty();
    }

    public void unselect(TreeNode<T> node)
    {
        assert node != null;

        if (selectedIds.remove(node.getId()))
            markDirty();
    }

    public void clear()
    {
        if (!selectedIds.isEmpty())
        {
            selectedIds.clear();
            markDirty();
        }
    }
}
