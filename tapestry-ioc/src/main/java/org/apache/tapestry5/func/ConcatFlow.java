// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.func;

/**
 * A flow that implements a lazy concatination of two flows. Because of lazy filtering,
 * we don't know at construction time whether either of the underlying flows are empty.
 * 
 * @since 5.2.0
 */
class ConcatFlow<T> extends AbstractFlow<T>
{
    private Flow<T> firstFlow;

    private Flow<T> secondFlow;

    private boolean resolved;

    private boolean isEmpty;

    private T first;

    private Flow<T> rest;

    @SuppressWarnings("unchecked")
    public ConcatFlow(Flow<T> firstFlow, Flow<? extends T> secondFlow)
    {
        this.firstFlow = firstFlow;
        this.secondFlow = (Flow<T>) secondFlow;
    }

    public synchronized T first()
    {
        resolve();

        return first;
    }

    public synchronized boolean isEmpty()
    {
        resolve();

        return isEmpty;
    }

    public synchronized Flow<T> rest()
    {
        resolve();

        return rest;
    }

    @SuppressWarnings("unchecked")
    private void resolve()
    {
        if (resolved)
            return;

        if (!firstFlow.isEmpty())
        {
            first = firstFlow.first();

            Flow<T> restOfFirst = firstFlow.rest();

            if (restOfFirst.isEmpty())
                rest = secondFlow;
            else
                rest = new ConcatFlow(restOfFirst, secondFlow);
        }
        else
        {
            first = secondFlow.first();
            rest = secondFlow.rest();

            isEmpty = secondFlow.isEmpty();
        }

        firstFlow = null;
        secondFlow = null;

        resolved = true;
    }
}
