// Copyright 2006–2025 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.junit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TapestryIOCJUnitExtension.class)
@Registry(modules = TapestryIOCJUnit5AfterMethodTest.AfterMethodTestModule.class, shutdown = RegistryShutdownType.AFTER_METHOD)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TapestryIOCJUnit5AfterMethodTest
{

    public static class AfterMethodTestModule
    {
        public List<String> buildList()
        {
            List<String> list = new ArrayList<>();
            list.add("foo");
            return list;
        }
    }

    @Inject
    private List<String> list;

    @Test
    void testInjectA()
    {
        assertArrayEquals(new Object[]
        { "foo" }, list.toArray());
        list.add("bar");
    }

    @Test
    void testInjectB()
    {
        assertArrayEquals(new Object[]
        { "foo" }, list.toArray());
        list.add("baz");
    }

    @Test
    void testInjectC()
    {
        assertArrayEquals(new Object[]
        { "foo" }, list.toArray());
    }
}
