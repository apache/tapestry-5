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

public class TakeDropTests extends BaseFuncTest
{
    @Test
    public void take_from_empty_list()
    {
        assertSame(F.flow().take(34), F.EMPTY_FLOW);
    }

    @Test
    public void take_from_flow()
    {
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

    @Test
    public void take_and_drop()
    {
        // This can go much, much larger but starts taking a while. Don't hold a reference to the
        // start
        // of the series or it can run out of memory.
        int length = 100000;

        assertFlowValues(F.series(1, 1).filter(evenp).drop(length).take(3), 2 * length + 2, 2 * length + 4,
                2 * length + 6);
    }

    @Test
    public void drop_from_empty_is_empty()
    {
        assertSame(F.flow().drop(99), F.EMPTY_FLOW);
    }

    @Test
    public void drop_exact_size_of_flow_is_empty()
    {
        assertTrue(F.range(1, 10).filter(evenp).drop(4).isEmpty());
    }

    @Test
    public void lazy_drop_more_than_available()
    {
        assertTrue(F.range(1, 10).filter(evenp).drop(5).isEmpty());
    }

    @Test
    public void drop_all_from_array_flow_is_empty_flow()
    {
        assertSame(F.flow(1, 2, 3).drop(3), F.EMPTY_FLOW);
    }

    @Test
    public void drop_zero_from_array_flow_is_same()
    {
        Flow<Integer> flow = F.flow(1, 2, 3);

        assertSame(flow.drop(0), flow);
    }

    @Test
    public void drop_from_array_flow()
    {
        assertFlowValues(F.range(1, 10).reverse().drop(2), 7, 6, 5, 4, 3, 2, 1);
    }
}
