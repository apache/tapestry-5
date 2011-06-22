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

import org.apache.tapestry5.ioc.IdMatcher;
import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.internal.IdMatcherImpl;
import org.apache.tapestry5.ioc.internal.OrIdMatcher;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Used to order objects into an "execution" order. Each object must have a unique id. It may specify a list of
 * constraints which identify the ordering of the objects.
 */
public class IdToDependencyNode<T>
{
  private final OneShotLock lock = new OneShotLock();

  private final Logger logger;

  private final List<Orderable> orderables = CollectionFactory.newList();

  private final Map<String, Orderable<T>> idToOrderable = CollectionFactory.newCaseInsensitiveMap();

  private final Map<String, DependencyNode<T>> idToDependencyNode = CollectionFactory.newCaseInsensitiveMap();

  // Special node that is always dead last: all other nodes are a dependency
  // of the trailer.

  private DependencyNode<T> trailer;

  interface DependencyLinker<T>
  {
    void link(DependencyNode<T> source, DependencyNode<T> target);
  }

  // before: source is added as a dependency of target, so source will
  // appear before target.

  final DependencyLinker<T> before = new DependencyLinker<T>()
  {
    public void link(DependencyNode<T> source, DependencyNode<T> target)
    {
      target.addDependency(source);
    }
  };

  // after: target is added as a dependency of source, so source will appear
  // after target.

  final DependencyLinker<T> after = new DependencyLinker<T>()
  {
    public void link(DependencyNode<T> source, DependencyNode<T> target)
    {
      source.addDependency(target);
    }
  };

  public IdToDependencyNode(Logger logger)
  {
    this.logger = logger;
  }

  /**
   * Adds an object to be ordered.
   *
   * @param orderable
   */
  public void add(Orderable<T> orderable)
  {
    lock.check();

    String id = orderable.getId();

    if (idToOrderable.containsKey(id))
    {
      logger.warn(UtilMessages.duplicateOrderer(id));
      return;
    }

    orderables.add(orderable);

    idToOrderable.put(id, orderable);
  }

  /**
   * Adds an object to be ordered.
   *
   * @param id          unique, qualified id for the target
   * @param target      the object to be ordered (or null as a placeholder)
   * @param constraints optional, variable constraints
   * @see #add(org.apache.tapestry5.ioc.Orderable)
   */

  public void add(String id, T target, String... constraints)
  {
    lock.check();

    add(new Orderable<T>(id, target, constraints));
  }

  public List<T> getOrdered()
  {
    lock.lock();

    initializeGraph();

    List<T> result = newList();

    for (Orderable<T> orderable : trailer.getOrdered())
    {
      T target = orderable.getTarget();

      // Nulls are placeholders that are skipped.

      if (target != null) result.add(target);
    }

    return result;
  }

  private void initializeGraph()
  {
    trailer = new DependencyNode<T>(logger, new Orderable<T>("*-trailer-*", null));

    addNodes();

    addDependencies();
  }

  private void addNodes()
  {
    for (Orderable<T> orderable : orderables)
    {
      DependencyNode<T> node = new DependencyNode<T>(logger, orderable);

      idToDependencyNode.put(orderable.getId(), node);

      trailer.addDependency(node);
    }
  }

  private void addDependencies()
  {
    for (Orderable<T> orderable : orderables)
    {
      addDependencies(orderable);
    }
  }

  private void addDependencies(Orderable<T> orderable)
  {
    String sourceId = orderable.getId();

    for (String constraint : orderable.getConstraints())
    {
      addDependencies(sourceId, constraint);
    }
  }

  private void addDependencies(String sourceId, String constraint)
  {
    int colonx = constraint.indexOf(':');

    String type = colonx > 0 ? constraint.substring(0, colonx) : null;

    DependencyLinker<T> linker = null;

    if ("after".equals(type))
      linker = after;
    else if ("before".equals(type)) linker = before;

    if (linker == null)
    {
      logger.warn(UtilMessages.constraintFormat(constraint, sourceId));
      return;
    }

    String patternList = constraint.substring(colonx + 1);

    linkNodes(sourceId, patternList, linker);
  }

  private void linkNodes(String sourceId, String patternList, DependencyLinker<T> linker)
  {
    Collection<DependencyNode<T>> nodes = findDependencies(sourceId, patternList);

    DependencyNode<T> source = idToDependencyNode.get(sourceId);

    for (DependencyNode<T> target : nodes)
    {
      linker.link(source, target);
    }
  }

  private Collection<DependencyNode<T>> findDependencies(String sourceId, String patternList)
  {
    IdMatcher matcher = buildMatcherForPattern(patternList);

    Collection<DependencyNode<T>> result = newList();

    for (String id : idToDependencyNode.keySet())
    {
      if (sourceId.equals(id)) continue;

      if (matcher.matches(id)) result.add(idToDependencyNode.get(id));
    }

    return result;
  }

  private IdMatcher buildMatcherForPattern(String patternList)
  {
    List<IdMatcher> matchers = newList();

    for (String pattern : patternList.split(","))
    {
      IdMatcher matcher = new IdMatcherImpl(pattern.trim());

      matchers.add(matcher);
    }

    return matchers.size() == 1 ? matchers.get(0) : new OrIdMatcher(matchers);
  }
}
