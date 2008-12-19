// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InheritanceSearch;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.TypeCoercer;

import java.util.*;

public class TypeCoercerImpl implements TypeCoercer
{
    // Constructed from the service's configuration.

    private final Map<Class, List<CoercionTuple>> sourceTypeToTuple = CollectionFactory.newMap();

    /**
     * A coercion to a specific target type.  Manages a cache of coercions to specific types.
     */
    private class TargetCoercion
    {
        private final Class type;

        private final Map<Class, Coercion> cache = CollectionFactory.newConcurrentMap();

        TargetCoercion(Class type)
        {
            this.type = type;
        }

        void clearCache()
        {
            cache.clear();
        }

        Object coerce(Object input)
        {

            Class sourceType = input != null ? input.getClass() : void.class;

            if (type.isAssignableFrom(sourceType)) return input;

            Coercion c = getCoercion(sourceType);

            try
            {
                return type.cast(c.coerce(input));
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ServiceMessages.failedCoercion(
                        input,
                        type,
                        c,
                        ex), ex);
            }
        }

        String explain(Class sourceType)
        {
            return getCoercion(sourceType).toString();
        }

        private Coercion getCoercion(Class sourceType)
        {
            Coercion c = cache.get(sourceType);

            if (c == null)
            {
                c = findOrCreateCoercion(sourceType, type);
                cache.put(sourceType, c);
            }
            return c;
        }
    }

    /**
     * Map from a target type to a TargetCoercion for that type.
     */
    private final Map<Class, TargetCoercion> typeToTargetCoercion = new WeakHashMap<Class, TargetCoercion>();

    private static final Coercion COERCION_NULL_TO_OBJECT = new Coercion<Void, Object>()
    {
        public Object coerce(Void input)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "null --> null";
        }
    };

    public TypeCoercerImpl(Collection<CoercionTuple> tuples)
    {
        for (CoercionTuple tuple : tuples)
        {
            Class key = tuple.getSourceType();

            InternalUtils.addToMapList(sourceTypeToTuple, key, tuple);
        }
    }

    @SuppressWarnings("unchecked")
    public Object coerce(Object input, Class targetType)
    {
        Defense.notNull(targetType, "targetType");

        Class effectiveTargetType = ClassFabUtils.getWrapperType(targetType);

        if (effectiveTargetType.isInstance(input)) return input;

        return getTargetCoercion(effectiveTargetType).coerce(input);
    }

    @SuppressWarnings("unchecked")
    public <S, T> String explain(Class<S> inputType, Class<T> targetType)
    {
        Defense.notNull(inputType, "inputType");
        Defense.notNull(targetType, "targetType");

        Class effectiveTargetType = ClassFabUtils.getWrapperType(targetType);

        // Is a coercion even necessary? Not if the target type is assignable from the
        // input value.

        if (effectiveTargetType.isAssignableFrom(inputType)) return "";

        return getTargetCoercion(targetType).explain(inputType);
    }

    private synchronized TargetCoercion getTargetCoercion(Class targetType)
    {
        TargetCoercion tc = typeToTargetCoercion.get(targetType);

        if (tc == null)
        {
            tc = new TargetCoercion(targetType);
            typeToTargetCoercion.put(targetType, tc);
        }

        return tc;
    }

    public synchronized void clearCache()
    {
        // There's no need to clear the typeToTargetCoercion map, as it is a WeakHashMap and
        // will release the keys for classes that are no longer in existence.  On the other hand,
        // there's likely all sorts of references to unloaded classes inside each TargetCoercion's
        // individual cache, so clear all those.

        for (TargetCoercion tc : typeToTargetCoercion.values())
        {
            // Can tc ever be null?

            tc.clearCache();
        }
    }

    /**
     * Here's the real meat; we do a search of the space to find coercions, or a system of coercions, that accomplish
     * the desired coercion.
     * <p/>
     * There's <strong>TREMENDOUS</strong> room to improve this algorithm. For example, inheritance lists could be
     * cached. Further, there's probably more ways to early prune the search. However, even with dozens or perhaps
     * hundreds of tuples, I suspect the search will still grind to a conclusion quickly.
     * <p/>
     * The order of operations should help ensure that the most efficient tuple chain is located. If you think about how
     * tuples are added to the queue, there are two factors: size (the number of steps in the coercion) and "class
     * distance" (that is, number of steps up the inheritance hiearchy). All the appropriate 1 step coercions will be
     * considered first, in class distance order. Along the way, we'll queue up all the 2 step coercions, again in class
     * distance order. By the time we reach some of those, we'll have begun queing up the 3 step coercions, and so
     * forth, until we run out of input tuples we can use to fabricate multi-step compound coercions, or reach a final
     * response.
     * <p/>
     * This does create a good number of short lived temporary objects (the compound tuples), but that's what the GC is
     * really good at.
     *
     * @param sourceType
     * @param targetType
     * @return coercer from sourceType to targetType
     */
    @SuppressWarnings("unchecked")
    private Coercion findOrCreateCoercion(Class sourceType, Class targetType)
    {
        if (sourceType == void.class) return searchForNullCoercion(targetType);

        // These are instance variables because this method may be called concurrently.
        // On a true race, we may go to the work of seeking out and/or fabricating
        // a tuple twice, but it's more likely that different threads are looking
        // for different source/target coercions.

        Set<CoercionTuple> consideredTuples = CollectionFactory.newSet();
        LinkedList<CoercionTuple> queue = CollectionFactory.newLinkedList();

        seedQueue(sourceType, consideredTuples, queue);

        while (!queue.isEmpty())
        {
            CoercionTuple tuple = queue.removeFirst();

            // If the tuple results in a value type that is assignable to the desired target type,
            // we're done! Later, we may add a concept of "cost" (i.e. number of steps) or
            // "quality" (how close is the tuple target type to the desired target type). Cost
            // is currently implicit, as compound tuples are stored deeper in the queue,
            // so simpler coercions will be located earlier.

            Class tupleTargetType = tuple.getTargetType();

            if (targetType.isAssignableFrom(tupleTargetType)) return tuple.getCoercion();

            // So .. this tuple doesn't get us directly to the target type.
            // However, it *may* get us part of the way. Each of these
            // represents a coercion from the source type to an intermediate type.
            // Now we're going to look for conversions from the intermediate type
            // to some other type.

            queueIntermediates(sourceType, tuple, consideredTuples, queue);
        }

        // Not found anywhere. Identify the source and target type and a (sorted) list of
        // all the known coercions.

        throw new IllegalArgumentException(ServiceMessages.noCoercionFound(
                sourceType,
                targetType,
                buildCoercionCatalog()));
    }

    /**
     * Coercion from null is special; we match based on the target type and its not a spanning search. In many cases, we
     * return a pass-thru that leaves the value as null.
     *
     * @param targetType desired type
     * @return the coercion
     */
    private Coercion searchForNullCoercion(Class targetType)
    {
        List<CoercionTuple> tuples = sourceTypeToTuple.get(void.class);

        // We know it will never be null, because we make contributions
        // to ensure this, but a little check doesn't hurt.

        if (tuples != null)
        {
            for (CoercionTuple tuple : tuples)
            {
                Class tupleTargetType = tuple.getTargetType();

                if (targetType.equals(tupleTargetType)) return tuple.getCoercion();
            }
        }

        // Typical case: no match, this coercion passes the null through
        // as null.

        return COERCION_NULL_TO_OBJECT;
    }

    /**
     * Builds a string listing all the coercions configured for the type coercer, sorted alphabetically.
     */
    private String buildCoercionCatalog()
    {
        List<String> descriptions = newList();

        for (List<CoercionTuple> list : sourceTypeToTuple.values())
        {
            for (CoercionTuple tuple : list)
                descriptions.add(tuple.toString());
        }

        return InternalUtils.joinSorted(descriptions);
    }

    /**
     * Seeds the pool with the initial set of coercions for the given type.
     */
    private void seedQueue(Class sourceType, Set<CoercionTuple> consideredTuples,
                           LinkedList<CoercionTuple> queue)
    {
        // Work from the source type up looking for tuples

        for (Class c : new InheritanceSearch(sourceType))
        {
            List<CoercionTuple> tuples = sourceTypeToTuple.get(c);

            if (tuples == null) continue;

            for (CoercionTuple tuple : tuples)
            {
                queue.addLast(tuple);
                consideredTuples.add(tuple);
            }

            // Don't pull in Object -> type coercions when doing
            // a search from null.

            if (sourceType == void.class) return;
        }
    }

    /**
     * Creates and adds to the pool a new set of coercions based on an intermediate tuple. Adds compound coercion tuples
     * to the end of the queue.
     *
     * @param sourceType        the source type of the coercion
     * @param intermediateTuple a tuple that converts from the source type to some intermediate type (that is not
     *                          assignable to the target type)
     * @param consideredTuples  set of tuples that have already been added to the pool (directly, or as a compound
     *                          coercion)
     * @param queue             the work queue of tuples
     */
    @SuppressWarnings("unchecked")
    private void queueIntermediates(Class sourceType, CoercionTuple intermediateTuple,
                                    Set<CoercionTuple> consideredTuples, LinkedList<CoercionTuple> queue)
    {
        Class intermediateType = intermediateTuple.getTargetType();

        for (Class c : new InheritanceSearch(intermediateType))
        {
            List<CoercionTuple> tuples = sourceTypeToTuple.get(c);

            if (tuples == null) continue;

            for (CoercionTuple tuple : tuples)
            {
                if (consideredTuples.contains(tuple)) continue;

                Class newIntermediateType = tuple.getTargetType();

                // If this tuple is for coercing from an intermediate type back towards our
                // initial source type, then ignore it. This should only be an optimization,
                // as branches that loop back towards the source type will
                // eventually be considered and discarded.

                if (sourceType.isAssignableFrom(newIntermediateType)) continue;

                // The intermediateTuple coercer gets from S --> I1 (an intermediate type).
                // The current tuple's coercer gets us from I2 --> X. where I2 is assignable
                // from I1 (i.e., I2 is a superclass/superinterface of I1) and X is a new
                // intermediate type, hopefully closer to our eventual target type.

                Coercion compoundCoercer = new CompoundCoercion(intermediateTuple.getCoercion(),
                                                                tuple.getCoercion());

                CoercionTuple compoundTuple = new CoercionTuple(sourceType, newIntermediateType,
                                                                compoundCoercer, false);

                // So, every tuple that is added to the queue can take as input the sourceType.
                // The target type may be another intermdiate type, or may be something
                // assignable to the target type, which will bring the search to a succesful
                // conclusion.

                queue.addLast(compoundTuple);
                consideredTuples.add(tuple);
            }
        }
    }
}
