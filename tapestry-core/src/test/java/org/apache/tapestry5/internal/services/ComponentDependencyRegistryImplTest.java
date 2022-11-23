// Copyright 2022 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests {@link ComponentDependencyRegistryImpl}.
 */
public class ComponentDependencyRegistryImplTest
{
    
    private ComponentDependencyRegistryImpl componentDependencyRegistry;
    
    @BeforeClass
    public void setup()
    {
        componentDependencyRegistry = new ComponentDependencyRegistryImpl();
    }
    
    @Test(timeOut = 5000)
    public void listen()
    {
        
        componentDependencyRegistry.add("foo", "bar");
        componentDependencyRegistry.add("d", "a");
        componentDependencyRegistry.add("dd", "aa");
        componentDependencyRegistry.add("dd", "a");
        componentDependencyRegistry.add("dd", "a");
        
        final List<String> resources = Arrays.asList("a", "aa", "none");
        final List<String> result = componentDependencyRegistry.listen(resources);
        Collections.sort(result);
        assertEquals(result, Arrays.asList("d", "dd"));
        
    }
    
    @Test
    public void dependency_methods()
    {
        
        final String foo = "foo";
        final String bar = "bar";
        final String something = "d";
        final String other = "dd";
        final String fulano = "a";
        final String beltrano = "aa";
        
        assertEquals(componentDependencyRegistry.getDependencies(foo), Collections.emptySet(), 
                "getDependents() should never return null");

        assertEquals(componentDependencyRegistry.getDependents(foo), Collections.emptySet(), 
                "getDependents() should never return null");

        componentDependencyRegistry.add(foo, bar);
        componentDependencyRegistry.add(something, fulano);
        componentDependencyRegistry.add(other, beltrano);
        componentDependencyRegistry.add(other, fulano);
        componentDependencyRegistry.add(other, fulano);
        
        assertEquals(componentDependencyRegistry.getDependencies(other), new HashSet<>(Arrays.asList(fulano, beltrano)));
        assertEquals(componentDependencyRegistry.getDependencies(something), new HashSet<>(Arrays.asList(fulano)));
        assertEquals(componentDependencyRegistry.getDependencies(fulano), new HashSet<>(Arrays.asList()));
        assertEquals(componentDependencyRegistry.getDependencies(foo), new HashSet<>(Arrays.asList(bar)));
        assertEquals(componentDependencyRegistry.getDependencies(bar), new HashSet<>(Arrays.asList()));

        assertEquals(componentDependencyRegistry.getDependents(bar), new HashSet<>(Arrays.asList(foo)));
        assertEquals(componentDependencyRegistry.getDependents(fulano), new HashSet<>(Arrays.asList(other, something)));
        assertEquals(componentDependencyRegistry.getDependents(foo), new HashSet<>(Arrays.asList()));
        
    }

}
