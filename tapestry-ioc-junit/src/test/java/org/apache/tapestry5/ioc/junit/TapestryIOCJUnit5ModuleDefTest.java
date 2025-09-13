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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TapestryIOCJUnitExtension.class)
@Registry(modules = TapestryIOCJUnit5ModuleDefTest.ModuleDefTestModule.class)
public class TapestryIOCJUnit5ModuleDefTest
{

    @ModuleDef
    public static org.apache.tapestry5.ioc.def.ModuleDef createModuleDef1()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("a", new Date(111));
        map.put("b", 222L);
        return new MapModuleDef(map);
    }

    @ModuleDef
    public static org.apache.tapestry5.ioc.def.ModuleDef createModuleDef2()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("c", 333);
        return new MapModuleDef(map);
    }

    public static class ModuleDefTestModule
    {
        public String buildD()
        {
            return "444";
        }
    }

    @Inject
    private Date a;
    @Inject
    private Long b;
    @Inject
    private Integer c;
    @Inject
    private String d;

    @Test
    void testModuleDefInject()
    {
        assertEquals(new Date(111), a);
        assertEquals(Long.valueOf(222L), b);
        assertEquals(Integer.valueOf(333), c);
        assertEquals("444", d);
    }
}
