//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.internal.util.Invokable;

/**
 * Wraps a {@link org.apache.tapestry5.ioc.internal.util.Invokable} object as a {@link Runnable}. After it is run, the
 * result can be collected.
 */
public class InvokableToRunnable<T> implements Runnable
{
    private final Invokable<T> invokable;

    private T result;

    public InvokableToRunnable(Invokable<T> invokable)
    {
        this.invokable = invokable;
    }

    public void run()
    {
        result = invokable.invoke();
    }

    public T getResult()
    {
        return result;
    }

    public static <T> InvokableToRunnable wrap(Invokable<T> invokable)
    {
        return new InvokableToRunnable(invokable);
    }
}
