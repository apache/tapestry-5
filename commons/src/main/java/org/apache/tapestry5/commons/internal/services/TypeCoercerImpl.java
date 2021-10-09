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

package org.apache.tapestry5.commons.internal.services;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.tapestry5.commons.internal.util.InheritanceSearch;
import org.apache.tapestry5.commons.internal.util.InternalCommonsUtils;
import org.apache.tapestry5.commons.internal.util.LockSupport;
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.StringToEnumCoercion;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.plastic.PlasticUtils;

@SuppressWarnings("all")
public class TypeCoercerImpl extends LockSupport implements TypeCoercer
{
    // Constructed from the service's configuration.

    private final Map<Class, List<CoercionTuple>> sourceTypeToTuple = CollectionFactory.newMap();

    /**
     * A coercion to a specific target type. Manages a cache of coercions to specific types.
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
            Class sourceType = input != null ? input.getClass() : Void.class;

            if (type.isAssignableFrom(sourceType))
            {
                return input;
            }

            Coercion c = getCoercion(sourceType);

            try
            {
                return type.cast(c.coerce(input));
            } catch (Exception ex)
            {
                throw new RuntimeException(ServiceMessages.failedCoercion(input, type, c, ex), ex);
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

    private static final Coercion NO_COERCION = new Coercion<Object, Object>()
    {
        @Override
        public Object coerce(Object input)
        {
            return input;
        }
    };

    private static final Coercion COERCION_NULL_TO_OBJECT = new Coercion<Void, Object>()
    {
        @Override
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

    public TypeCoercerImpl(Map<CoercionTuple.Key, CoercionTuple> tuples)
    {
        for (CoercionTuple tuple : tuples.values())
        {
            Class key = tuple.getSourceType();

            InternalCommonsUtils.addToMapList(sourceTypeToTuple, key, tuple);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object coerce(Object input, Class targetType)
    {
        assert targetType != null;

        Class effectiveTargetType = PlasticUtils.toWrapperType(targetType);

        if (effectiveTargetType.isInstance(input))
        {
            return input;
        }


        return getTargetCoercion(effectiveTargetType).coerce(input);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> Coercion<S, T> getCoercion(Class<S> sourceType, Class<T> targetType)
    {
        assert sourceType != null;
        assert targetType != null;

        Class effectiveSourceType = PlasticUtils.toWrapperType(sourceType);
        Class effectiveTargetType = PlasticUtils.toWrapperType(targetType);

        if (effectiveTargetType.isAssignableFrom(effectiveSourceType))
        {
            return NO_COERCION;
        }

        return getTargetCoercion(effectiveTargetType).getCoercion(effectiveSourceType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S, T> String explain(Class<S> sourceType, Class<T> targetType)
    {
        assert sourceType != null;
        assert targetType != null;

        Class effectiveTargetType = PlasticUtils.toWrapperType(targetType);
        Class effectiveSourceType = PlasticUtils.toWrapperType(sourceType);

        // Is a coercion even necessary? Not if the target type is assignable from the
        // input value.

        if (effectiveTargetType.isAssignableFrom(effectiveSourceType))
        {
            return "";
        }

        return getTargetCoercion(effectiveTargetType).explain(effectiveSourceType);
    }

    private TargetCoercion getTargetCoercion(Class targetType)
    {
        try
        {
            acquireReadLock();

            TargetCoercion tc = typeToTargetCoercion.get(targetType);

            return tc != null ? tc : createAndStoreNewTargetCoercion(targetType);
        } finally
        {
            releaseReadLock();
        }
    }

    private TargetCoercion createAndStoreNewTargetCoercion(Class targetType)
    {
        try
        {
            upgradeReadLockToWriteLock();

            // Inner check since some other thread may have beat us to it.

            TargetCoercion tc = typeToTargetCoercion.get(targetType);

            if (tc == null)
            {
                tc = new TargetCoercion(targetType);
                typeToTargetCoercion.put(targetType, tc);
            }

            return tc;
        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }

    @Override
    public void clearCache()
    {
        try
        {
            acquireReadLock();

            // There's no need to clear the typeToTargetCoercion map, as it is a WeakHashMap and
            // will release the keys for classes that are no longer in existence. On the other hand,
            // there's likely all sorts of references to unloaded classes inside each TargetCoercion's
            // individual cache, so clear all those.

            for (TargetCoercion tc : typeToTargetCoercion.values())
            {
                // Can tc ever be null?

                tc.clearCache();
            }
        } finally
        {
            releaseReadLock();
        }
    }

    /**
     * Here's the real meat; we do a search of the space to find coercions, or a system of
     * coercions, that accomplish
     * the desired coercion.
     *
     * There's <strong>TREMENDOUS</strong> room to improve this algorithm. For example, inheritance lists could be
     * cached. Further, there's probably more ways to early prune the search. However, even with dozens or perhaps
     * hundreds of tuples, I suspect the search will still grind to a conclusion quickly.
     *
     * The order of operations should help ensure that the most efficient tuple chain is located. If you think about how
     * tuples are added to the queue, there are two factors: size (the number of steps in the coercion) and
     * "class distance" (that is, number of steps up the inheritance hiearchy). All the appropriate 1 step coercions
     * will be considered first, in class distance order. Along the way, we'll queue up all the 2 step coercions, again
     * in class distance order. By the time we reach some of those, we'll have begun queueing up the 3 step coercions, and
     * so forth, until we run out of input tuples we can use to fabricate multi-step compound coercions, or reach a
     * final response.
     *
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
        if (sourceType == Void.class)
        {
            return searchForNullCoercion(targetType);
        }
        
        // Trying to find exact match.
        Optional<CoercionTuple> maybeTuple = 
                getTuples(sourceType, targetType).stream()
                    .filter((t) -> sourceType.equals(t.getSourceType()) && 
                            targetType.equals(t.getTargetType())).findFirst();
        
        if (maybeTuple.isPresent())
        {
            return maybeTuple.get().getCoercion();
        }

        // These are instance variables because this method may be called concurrently.
        // On a true race, we may go to the work of seeking out and/or fabricating
        // a tuple twice, but it's more likely that different threads are looking
        // for different source/target coercions.

        Set<CoercionTuple.Key> consideredTuples = CollectionFactory.newSet();
        LinkedList<CoercionTuple> queue = CollectionFactory.newLinkedList();

        seedQueue(sourceType, targetType, consideredTuples, queue);

        while (!queue.isEmpty())
        {
            CoercionTuple tuple = queue.removeFirst();

            // If the tuple results in a value type that is assignable to the desired target type,
            // we're done! Later, we may add a concept of "cost" (i.e. number of steps) or
            // "quality" (how close is the tuple target type to the desired target type). Cost
            // is currently implicit, as compound tuples are stored deeper in the queue,
            // so simpler coercions will be located earlier.

            Class tupleTargetType = tuple.getTargetType();

            if (targetType.isAssignableFrom(tupleTargetType))
            {
                return tuple.getCoercion();
            }

            // So .. this tuple doesn't get us directly to the target type.
            // However, it *may* get us part of the way. Each of these
            // represents a coercion from the source type to an intermediate type.
            // Now we're going to look for conversions from the intermediate type
            // to some other type.

            queueIntermediates(sourceType, targetType, tuple, consideredTuples, queue);
        }

        // Not found anywhere. Identify the source and target type and a (sorted) list of
        // all the known coercions.

        throw new UnknownValueException(String.format("Could not find a coercion from type %s to type %s.",
                sourceType.getName(), targetType.getName()), buildCoercionCatalog());
    }

    /**
     * Coercion from null is special; we match based on the target type and its not a spanning
     * search. In many cases, we
     * return a pass-thru that leaves the value as null.
     *
     * @param targetType
     *         desired type
     * @return the coercion
     */
    private Coercion searchForNullCoercion(Class targetType)
    {
        List<CoercionTuple> tuples = getTuples(Void.class, targetType);

        for (CoercionTuple tuple : tuples)
        {
            Class tupleTargetType = tuple.getTargetType();

            if (targetType.equals(tupleTargetType))
                return tuple.getCoercion();
        }

        // Typical case: no match, this coercion passes the null through
        // as null.

        return COERCION_NULL_TO_OBJECT;
    }

    /**
     * Builds a string listing all the coercions configured for the type coercer, sorted
     * alphabetically.
     */
    @SuppressWarnings("unchecked")
    private AvailableValues buildCoercionCatalog()
    {
        List<CoercionTuple> masterList = CollectionFactory.newList();

        for (List<CoercionTuple> list : sourceTypeToTuple.values())
        {
            masterList.addAll(list);
        }

        return new AvailableValues("Configured coercions", masterList);
    }

    /**
     * Seeds the pool with the initial set of coercions for the given type.
     */
    private void seedQueue(Class sourceType, Class targetType, Set<CoercionTuple.Key> consideredTuples,
                           LinkedList<CoercionTuple> queue)
    {
        // Work from the source type up looking for tuples

        for (Class c : new InheritanceSearch(sourceType))
        {
            List<CoercionTuple> tuples = getTuples(c, targetType);

            if (tuples == null)
            {
                continue;
            }

            for (CoercionTuple tuple : tuples)
            {
                queue.addLast(tuple);
                consideredTuples.add(tuple.getKey());
            }

            // Don't pull in Object -> type coercions when doing
            // a search from null.

            if (sourceType == Void.class)
            {
                return;
            }
        }
    }

    /**
     * Creates and adds to the pool a new set of coercions based on an intermediate tuple. Adds
     * compound coercion tuples
     * to the end of the queue.
     *
     * @param sourceType
     *         the source type of the coercion
     * @param targetType
     *         TODO
     * @param intermediateTuple
     *         a tuple that converts from the source type to some intermediate type (that is not
     *         assignable to the target type)
     * @param consideredTuples
     *         set of tuples that have already been added to the pool (directly, or as a compound
     *         coercion)
     * @param queue
     *         the work queue of tuples
     */
    @SuppressWarnings("unchecked")
    private void queueIntermediates(Class sourceType, Class targetType, CoercionTuple intermediateTuple,
                                    Set<CoercionTuple.Key> consideredTuples, LinkedList<CoercionTuple> queue)
    {
        Class intermediateType = intermediateTuple.getTargetType();

        for (Class c : new InheritanceSearch(intermediateType))
        {
            for (CoercionTuple tuple : getTuples(c, targetType))
            {
                if (consideredTuples.contains(tuple.getKey()))
                {
                    continue;
                }

                Class newIntermediateType = tuple.getTargetType();

                // If this tuple is for coercing from an intermediate type back towards our
                // initial source type, then ignore it. This should only be an optimization,
                // as branches that loop back towards the source type will
                // eventually be considered and discarded.

                if (sourceType.isAssignableFrom(newIntermediateType))
                {
                    continue;
                }

                // The intermediateTuple coercer gets from S --> I1 (an intermediate type).
                // The current tuple's coercer gets us from I2 --> X. where I2 is assignable
                // from I1 (i.e., I2 is a superclass/superinterface of I1) and X is a new
                // intermediate type, hopefully closer to our eventual target type.

                Coercion compoundCoercer = new CompoundCoercion(intermediateTuple.getCoercion(), tuple.getCoercion());

                CoercionTuple compoundTuple = new CoercionTuple(sourceType, newIntermediateType, compoundCoercer, false);

                // So, every tuple that is added to the queue can take as input the sourceType.
                // The target type may be another intermediate type, or may be something
                // assignable to the target type, which will bring the search to a successful
                // conclusion.

                queue.addLast(compoundTuple);
                consideredTuples.add(tuple.getKey());
            }
        }
    }

    /**
     * Returns a non-null list of the tuples from the source type.
     *
     * @param sourceType
     *         used to locate tuples
     * @param targetType
     *         used to add synthetic tuples
     * @return non-null list of tuples
     */
    private List<CoercionTuple> getTuples(Class sourceType, Class targetType)
    {
        List<CoercionTuple> tuples = sourceTypeToTuple.get(sourceType);

        if (tuples == null)
        {
            tuples = Collections.emptyList();
        }

        // So, when we see String and an Enum type, we add an additional synthetic tuple to the end
        // of the real list. This is the easiest way to accomplish this is a thread-safe and class-reloading
        // safe way (i.e., what if the Enum is defined by a class loader that gets discarded?  Don't want to cause
        // memory leaks by retaining an instance). In any case, there are edge cases where we may create
        // the tuple unnecessarily (such as when an explicit string-to-enum coercion is part of the TypeCoercer
        // configuration), but on the whole, this is cheap and works.

        if (sourceType == String.class && Enum.class.isAssignableFrom(targetType))
        {
            tuples = extend(tuples, new CoercionTuple(sourceType, targetType, new StringToEnumCoercion(targetType)));
        }
        else if (Enum.class.isAssignableFrom(sourceType) && targetType == String.class)
        {
            // TAP5-2565
            tuples = extend(tuples, new CoercionTuple(sourceType, targetType, (value)->((Enum) value).name()));
        }

        return tuples;
    }

    private static <T> List<T> extend(List<T> list, T extraValue)
    {
        return F.flow(list).append(extraValue).toList();
    }
}
