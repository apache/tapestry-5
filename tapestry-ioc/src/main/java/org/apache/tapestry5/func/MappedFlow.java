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
 * Flow that wraps around another Flow, transforming it value by value into a new Flow.
 * 
 * @since 5.2.0
 */
class MappedFlow<T, X> extends AbstractFlow<X>
{
    private final Mapper<T, X> mapper;

    // Used to determine first, rest
    private Flow<T> mappedFlow;

    // Guarded by this
    private boolean resolved;

    // Guarded by this
    private X first;

    // Guarded by this
    private Flow<X> rest;

    public MappedFlow(Mapper<T, X> mapper, Flow<T> mappedFlow)
    {
        this.mapper = mapper;
        this.mappedFlow = mappedFlow;
    }

    public synchronized X first()
    {
        resolve();

        return first;
    }

    public boolean isEmpty()
    {
        return false;
    }

    public synchronized Flow<X> rest()
    {
        resolve();

        return rest;
    }

    private void resolve()
    {
        if (resolved)
            return;

        // The mappedFlow should never be empty

        first = mapper.map(mappedFlow.first());

        Flow<T> mappedRest = mappedFlow.rest();

        if (mappedRest.isEmpty())
            rest = F.emptyFlow();
        else
            rest = new MappedFlow<T, X>(mapper, mappedRest);

        mappedFlow = null;

        resolved = true;
    }
}
