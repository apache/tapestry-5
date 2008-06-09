// Copyright 2004, 2005, 2006 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newMap;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Utility used to iterate over the publically visible methods of a class or interface. The MethodIterator understands
 * some complications that can occur when a class inherits the same method from multiple interfaces and with slightly
 * different signatures (due to the fact that declared thrown exceptions can vary slightly for the "same" method).
 *
 * @see org.apache.tapestry5.ioc.services.MethodSignature#isOverridingSignatureOf(MethodSignature)
 */
public class MethodIterator
{
    private boolean toString;

    private int index = 0;

    private final int count;

    private final List<MethodSignature> signatures;

    private static final Comparator<MethodSignature> COMPARATOR = new Comparator<MethodSignature>()
    {
        public int compare(MethodSignature o1, MethodSignature o2)
        {

            return o1.getName().compareTo(o2.getName());
        }
    };


    public MethodIterator(Class subjectClass)
    {
        Method[] methods = subjectClass.getMethods();

        Map<String, MethodSignature> map = newMap();

        for (int i = 0; i < methods.length; i++)
            processMethod(methods[i], map);

        signatures = newList(map.values());
        count = signatures.size();


        Collections.sort(signatures, COMPARATOR);
    }

    private void processMethod(Method m, Map<String, MethodSignature> map)
    {
        toString |= ClassFabUtils.isToString(m);

        MethodSignature sig = new MethodSignature(m);
        String uid = sig.getUniqueId();

        MethodSignature existing = map.get(uid);

        if (existing == null || sig.isOverridingSignatureOf(existing)) map.put(uid, sig);
    }

    public boolean hasNext()
    {
        return index < count;
    }

    /**
     * Returns the next method (as a {@link MethodSignature}, returning null when all are exhausted. Each method
     * signature is returned exactly once (even if the same method signature is defined in multiple inherited classes or
     * interfaces). The method signatures returned in ascending order, according to the "natural ordering".
     *
     * @throws NoSuchElementException if there are no more signatures
     */
    public MethodSignature next()
    {
        if (index >= count) throw new NoSuchElementException();

        return signatures.get(index++);
    }

    /**
     * Returns true if the method <code>public String toString()</code> is part of the interface. This will be known
     * immediately after iterator contruction (it is not necessary to iterate the methods first).
     */
    public boolean getToString()
    {
        return toString;
    }
}
