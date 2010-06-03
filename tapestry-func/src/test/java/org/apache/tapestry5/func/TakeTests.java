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

import org.testng.annotations.Test;

public class TakeTests extends FuncAssert
{
    @Test
    public void take_from_empty_list()
    {
        assertSame(F.flow().take(34), F.EMPTY_FLOW);
    }

    @Test
    public void take_from_flow()
    {
        Predicate<Integer> evenp = new Predicate<Integer>()
        {

            public boolean accept(Integer object)
            {
                return object % 2 == 0;
            }
        };

        assertFlowValues(F.series(1, 1).remove(evenp).take(2), 1, 3);
    }

    @Test
    public void take_from_array_flow()
    {
        Flow<Integer> flow = F.flow(1, 2, 3, 4, 5);

        assertFlowValues(flow.take(2), 1, 2);

        assertFlowValues(flow.take(99), 1, 2, 3, 4, 5);

        assertSame(flow.take(0), F.EMPTY_FLOW);
    }
}
