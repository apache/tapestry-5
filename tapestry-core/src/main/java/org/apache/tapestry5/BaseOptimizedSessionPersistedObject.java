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

package org.apache.tapestry5;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.tapestry5.http.OptimizedSessionPersistedObject;

/**
 * Base implementation of
 * {@link org.apache.tapestry5.http.OptimizedSessionPersistedObject}. Subclasses
 * should invoke {@link #markDirty()} after the internal state of the object changes.
 *
 * Due to the concurrent nature of session attributes it's important that markDirty occurs <strong>after</strong>
 * the object has been changed. If the change occurs before the object has been mutated it's possible that another
 * thread may re-store the object before the changes are actually made!
 *
 * @since 5.1.1.0
 */
public abstract class BaseOptimizedSessionPersistedObject implements OptimizedSessionPersistedObject, Serializable
{
    private static final long serialVersionUID = 172352928643322125L;

    private transient AtomicBoolean dirty = new AtomicBoolean(false);

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        dirty = new AtomicBoolean(false);
    }

    public final boolean checkAndResetDirtyMarker()
    {
        return dirty.getAndSet(false);
    }

    /**
     * Invoked by the subclass after internal state of the object changes.
     */
    protected final void markDirty()
    {
        dirty.set(true);
    }
}
