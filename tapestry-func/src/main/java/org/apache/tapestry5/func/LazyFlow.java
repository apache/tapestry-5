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

    // Retained up until resolve() is called
    
    private LazyFunction<T> lazyFunction;

    // Set inside resolve()
    private boolean empty;

    // Set inside resolve(), used and discarded inside first()
    private LazyValue<T> lazyFirst;

    // Set inside resolve()
    private Flow<T> rest;

    // Set inside first()
    private T first;

    public LazyFlow(LazyFunction<T> lazyFunction)
    {
        this.lazyFunction = lazyFunction;
    }

    @Override
    public synchronized T first()
    {
        resolve();

        // Immediately after resolving, all we have is the function to call to get
        // the first object. And once we get that object, we don't need (or want) the
        // function.

        if (lazyFirst != null)
        {
            first = lazyFirst.get();
            lazyFirst = null;
        }

        return first;
    }

    @Override
    public synchronized boolean isEmpty()
    {
        resolve();

        return empty;
    }

    @Override
    public synchronized Flow<T> rest()
    {
        resolve();

        return rest;
    }

    private synchronized void resolve()
    {
        if (lazyFunction == null)
            return;

        LazyContinuation<T> continuation = lazyFunction.next();

        if (continuation == null)
        {
            empty = true;
            rest = F.emptyFlow();
        }
        else
        {
            lazyFirst = continuation.nextValue();

            rest = new LazyFlow<T>(continuation.nextFunction());
        }

        lazyFunction = null;
    }
}
