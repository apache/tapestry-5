// Copyright 2011 The Apache Software Foundation
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

import java.util.List;

class Interleaver<T> implements LazyFunction<T>
{
    private final int valueIndex;

    private final List<Flow<T>> flows;

    Interleaver(List<Flow<T>> flows)
    {
        this(flows, 0);
    }

    private Interleaver(List<Flow<T>> flows, int valueIndex)
    {
        this.flows = flows;
        this.valueIndex = valueIndex;
    }

    @Override
    public LazyContinuation<T> next()
    {
        if (valueIndex == 0)
        {
            for (Flow<T> flow : flows)
            {
                if (flow.isEmpty())
                    return null;
            }

        }

        return new LazyContinuation<T>(flows.get(valueIndex).first(), nextFunction());
    }

    private LazyFunction<T> nextFunction()
    {
        if (valueIndex < flows.size() - 1) { return new Interleaver<T>(flows, valueIndex + 1); }

        List<Flow<T>> nextFlows = F.flow(flows).map(new Mapper<Flow<T>, Flow<T>>()
        {
            @Override
            public Flow<T> map(Flow<T> element)
            {
                return element.rest();
            }
        }).toList();

        return new Interleaver<T>(nextFlows, 0);
    }

}
