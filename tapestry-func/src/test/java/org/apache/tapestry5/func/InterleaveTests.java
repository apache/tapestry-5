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

import org.testng.annotations.Test;

public class InterleaveTests extends BaseFuncTest
{
    @Test
    public void interleave_equal_length_lists()
    {
        Flow<Integer> first = F.flow(1, 2, 3);
        Flow<Integer> second = F.flow(100, 200, 300);
        Flow<Integer> third = F.flow(1000, 2000, 3000);

        assertFlowValues(first.interleave(second, third), 1, 100, 1000, 2, 200, 2000, 3, 300, 3000);
    }

    @Test
    public void interleave_unequal_length_lists()
    {
        Flow<Integer> first = F.flow(1, 2, 3);
        Flow<Integer> second = F.flow(100);
        Flow<Integer> third = F.flow(1000, 2000, 3000);

        assertFlowValues(first.interleave(second, third), 1, 100, 1000);

    }
}
