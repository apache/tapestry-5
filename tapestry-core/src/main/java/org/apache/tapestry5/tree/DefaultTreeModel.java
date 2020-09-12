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

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A default implementation of TreeModel that starts with a {@link ValueEncoder} (for the element to string conversion),
 * a {@link TreeModelAdapter}, and a list of root nodes.
 *
 * This implementation is <em>not</em> thread safe.
 *
 * @param <T> the type of data in the tree
 * @since 5.3
 */
@SuppressWarnings({"UnusedDeclaration"})
public class DefaultTreeModel<T> implements TreeModel<T>
{
    private final ValueEncoder<T> encoder;

    private final TreeModelAdapter<T> adapter;

    private final List<TreeNode<T>> roots;

    private final Map<String, TreeNode<T>> cache = CollectionFactory.newMap();

    private final Mapper<T, TreeNode<T>> toTreeNode = new Mapper<T, TreeNode<T>>()
    {
        public TreeNode<T> map(T value)
        {
            return new DefaultTreeNode(value);
        }
    };

    private class DefaultTreeNode implements TreeNode<T>
    {
        private final T value;

        private List<TreeNode<T>> children;

        DefaultTreeNode(T value)
        {
            this.value = value;
        }

        public String getId()
        {
            return encoder.toClient(value);
        }

        public T getValue()
        {
            return value;
        }

        public boolean isLeaf()
        {
            return adapter.isLeaf(value);
        }

        public boolean getHasChildren()
        {
            return adapter.hasChildren(value);
        }

        public List<TreeNode<T>> getChildren()
        {
            if (children == null)
            {
                List<T> childValues = adapter.getChildren(value);

                boolean empty = childValues == null || childValues.isEmpty();

                children = empty
                        ? emptyTreeNodeList()
                        : F.flow(childValues).map(toTreeNode).toList();
            }

            return children;
        }

        public String getLabel()
        {
            return adapter.getLabel(value);
        }

        private List<TreeNode<T>> emptyTreeNodeList()
        {
            return Collections.emptyList();
        }

    }

    /**
     * Creates a new model starting from a single root element.
     *
     * @param encoder used to convert values to strings and vice-versa
     * @param adapter adapts elements to the tree
     * @param root    defines the root node of the model
     */
    public DefaultTreeModel(ValueEncoder<T> encoder, TreeModelAdapter<T> adapter, T root)
    {
        this(encoder, adapter, Collections.singletonList(root));
    }

    /**
     * Standard constructor.
     *
     * @param encoder used to convert values to strings and vice-versa
     * @param adapter adapts elements to the tree
     * @param roots   defines the root nodes of the model
     */
    public DefaultTreeModel(ValueEncoder<T> encoder, TreeModelAdapter<T> adapter, List<T> roots)
    {
        assert encoder != null;
        assert adapter != null;
        assert roots != null;
        assert !roots.isEmpty();

        this.encoder = encoder;
        this.adapter = adapter;
        this.roots = F.flow(roots).map(toTreeNode).toList();
    }

    public List<TreeNode<T>> getRootNodes()
    {
        return roots;
    }

    public TreeNode<T> getById(String id)
    {
        assert id != null;

        TreeNode<T> result = findById(id);

        if (result == null)
            throw new IllegalArgumentException(String.format("Could not locate TreeNode '%s'.", id));

        return result;
    }

    private TreeNode<T> findById(String id)
    {
        TreeNode<T> result = cache.get(id);

        if (result != null)
            return result;

        LinkedList<TreeNode<T>> queue = new LinkedList<TreeNode<T>>(roots);

        while (!queue.isEmpty())
        {
            TreeNode<T> node = queue.removeFirst();

            String nodeId = node.getId();

            cache.put(nodeId, node);

            if (nodeId.equals(id))
                return node;

            if (!node.isLeaf() && node.getHasChildren())
            {
                for (TreeNode<T> child : node.getChildren())
                {
                    queue.addFirst(child);
                }
            }
        }

        return null;
    }

    public TreeNode<T> find(T element)
    {
        return findById(encoder.toClient(element));
    }

}
