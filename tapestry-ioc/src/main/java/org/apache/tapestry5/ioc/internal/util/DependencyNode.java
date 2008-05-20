// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Orderable;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.slf4j.Logger;

import java.util.List;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.util.Orderer} to establish backward dependencies for {@link
 * org.apache.tapestry5.ioc.Orderable} objects.
 *
 * @param <T>
 */

class DependencyNode<T>
{
    private final Logger logger;

    private final Orderable<T> orderable;

    private final List<DependencyNode<T>> dependencies = CollectionFactory.newList();

    DependencyNode(Logger logger, Orderable<T> orderable)
    {
        this.logger = logger;
        this.orderable = orderable;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(String.format("[%s", getId()));

        boolean first = true;

        for (DependencyNode<T> node : dependencies)
        {

            buffer.append(first ? ": " : ", ");

            buffer.append(node.toString());

            first = false;
        }

        buffer.append("]");

        return buffer.toString();
    }

    /**
     * Returns the underlying {@link Orderable}'s id.
     */
    public String getId()
    {
        return orderable.getId();
    }

    void addDependency(DependencyNode<T> node)
    {
        if (node.isReachable(this))
        {
            logger.warn(UtilMessages.dependencyCycle(node, this));
            return;
        }

        // Make this node depend on the other node.
        // That forces the other node's orderable
        // to appear before this node's orderable.

        dependencies.add(node);
    }

    boolean isReachable(DependencyNode<T> node)
    {
        if (this == node) return true;

        // Quick fast pass for immediate dependencies

        for (DependencyNode<T> d : dependencies)
        {
            if (d == node) return true;
        }

        // Slower second pass looks for
        // indirect dependencies

        for (DependencyNode<T> d : dependencies)
        {
            if (d.isReachable(node)) return true;
        }

        return false;
    }

    /**
     * Returns the {@link Orderable} objects for this node ordered based on dependencies.
     */
    List<Orderable<T>> getOrdered()
    {
        List<Orderable<T>> result = newList();

        fillOrder(result);

        return result;
    }

    private void fillOrder(List<Orderable<T>> list)
    {
        if (list.contains(orderable)) return;

        // Recusively add dependencies

        for (DependencyNode<T> node : dependencies)
        {
            node.fillOrder(list);
        }

        list.add(orderable);
    }

}
