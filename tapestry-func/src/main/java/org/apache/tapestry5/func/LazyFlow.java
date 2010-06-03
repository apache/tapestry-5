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

class LazyFlow<T> extends AbstractFlow<T>
{
    // All instance variables guarded by this

    private boolean resolved;

    private boolean empty;

    private T first;

    private Flow<T> rest;

    private LazyFunction<T> lazyFunction;

    public LazyFlow(LazyFunction<T> lazyFunction)
    {
        this.lazyFunction = lazyFunction;
    }

    public synchronized T first()
    {
        resolve();

        return first;
    }

    public synchronized boolean isEmpty()
    {
        resolve();

        return empty;
    }

    public synchronized Flow<T> rest()
    {
        resolve();

        return rest;
    }

    private synchronized void resolve()
    {
        if (resolved)
            return;

        LazyContinuation<T> continuation = lazyFunction.next();

        if (continuation == null)
        {
            empty = true;
            rest = F.emptyFlow();
        }
        else
        {
            first = continuation.nextValue();

            rest = new LazyFlow<T>(continuation.nextFunction());
        }

        resolved = true;
        
        lazyFunction = null;
    }
}
