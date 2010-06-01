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
 * An operational function used with a {@link Flow}.
 * 
 * @since 5.2.0
 * @see Flow#each(Worker)
 */
public abstract class Worker<T>
{
    /**
     * Perform the operation on some object of type T.
     */
    public abstract void work(T value);

    /**
     * Combines this worker with the other worker, forming a new composite worker. In the composite,
     * the value from the Flow is passed first to this worker, then to the other worker.
     */
    public Worker<T> combine(final Worker<? super T> other)
    {
        assert other != null;

        final Worker<T> first = this;

        return new Worker<T>()
        {
            public void work(T value)
            {
                first.work(value);
                other.work(value);
            }

        };
    }

}
