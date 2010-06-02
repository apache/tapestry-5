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
 * {@link Flow} implementation that filters (applies a {@link Predicate}) to another Flow.
 * 
 * @since 5.2.0
 */
class FilteredFlow<T> extends AbstractFlow<T>
{
    private final Predicate<? super T> predicate;

    // Reset to null once resolved
    // Guarded by this
    private Flow<T> filteredFlow;

    // Has first, rest, empty been resolved?
    // Guarded by this
    private boolean resolved;

    // No matches in filteredFlow?
    // Guarded by this
    private boolean empty;

    // First match from filteredFlow
    // Guarded by this
    private T first;

    // Remaining flow after the first match
    // Guarded by this
    private Flow<T> rest;

    FilteredFlow(Predicate<? super T> predicate, Flow<T> filteredFlow)
    {
        this.predicate = predicate;
        this.filteredFlow = filteredFlow;
    }

    public synchronized T first()
    {
        resolve();

        return first;
    }

    public synchronized Flow<T> rest()
    {
        resolve();

        return rest;
    }

    public synchronized boolean isEmpty()
    {
        resolve();

        return empty;
    }

    private void resolve()
    {
        if (resolved)
            return;

        Flow<T> cursor = filteredFlow;

        while (true)
        {
            if (cursor.isEmpty())
            {
                empty = true;
                rest = F.emptyFlow();
                break;
            }

            T potential = cursor.first();

            if (predicate.accept(potential))
            {
                first = potential;
                rest = new FilteredFlow<T>(predicate, cursor.rest());
                break;
            }

            cursor = cursor.rest();
        }

        resolved = true;

        // No longer needed once resolved.

        filteredFlow = null;
    }
}
