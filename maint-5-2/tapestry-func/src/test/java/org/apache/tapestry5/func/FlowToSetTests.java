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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;

public class FlowToSetTests extends BaseFuncTest
{
    @Test
    public void empty_flow_to_set()
    {
        assertSame(F.emptyFlow().toSet(), Collections.EMPTY_SET);
    }

    @Test
    public void nonempty_flow()
    {
        Set<Integer> set = F.range(1, 10).filter(evenp).toSet();

        assertTrue(set.containsAll(Arrays.asList(2, 4, 6, 8)));
    }
}
