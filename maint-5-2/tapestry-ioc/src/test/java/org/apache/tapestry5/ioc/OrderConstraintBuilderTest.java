// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class OrderConstraintBuilderTest extends IOCTestCase
{
    @Test
    public void after()
    {
        String[] constraints = OrderConstraintBuilder.after("A").build();
        
        assertEquals(constraints.length, 1);
        assertEquals(constraints[0], "after:A");
    }

    @Test
    public void afterAll()
    {
        String[] constraints = OrderConstraintBuilder.afterAll().build();
        
        assertEquals(constraints.length, 1);
        assertEquals(constraints[0], "after:*");
    }

    @Test
    public void before()
    {
        String[] constraints = OrderConstraintBuilder.before("B").build();
        
        assertEquals(constraints.length, 1);
        assertEquals(constraints[0], "before:B");
    }

    @Test
    public void beforeAll()
    {
        String[] constraints = OrderConstraintBuilder.beforeAll().build();
        
        assertEquals(constraints.length, 1);
        assertEquals(constraints[0], "before:*");
    }
    


    @Test
    public void combine()
    {
        String[] constraints = OrderConstraintBuilder.before("A").after("B").build();
        
        assertEquals(constraints.length, 2);
        assertEquals(constraints[0], "before:A");
        assertEquals(constraints[1], "after:B");
    }
}
