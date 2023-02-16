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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.File;
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
        assertFalse(
                String.format("During testing, %s shouldn't exist", ComponentDependencyRegistry.FILENAME), 
                new File(ComponentDependencyRegistry.FILENAME).exists());
        componentDependencyRegistry = new ComponentDependencyRegistryImpl();
    }
    
    @Test(timeOut = 5000)
    public void listen()
    {
        
        add("foo", "bar");
        add("d", "a");
        add("dd", "aa");
        add("dd", "a");
        add("dd", "a");
        
        final List<String> resources = Arrays.asList("a", "aa", "none");
        final List<String> result = componentDependencyRegistry.listen(resources);
        Collections.sort(result);
        assertEquals(result, Arrays.asList("d", "dd"));
        
        final List<String> returnValue = componentDependencyRegistry.listen(Collections.emptyList());
        assertEquals(returnValue, Collections.emptyList());
        assertEquals(componentDependencyRegistry.getDependents("bar").size(), 0);
        assertEquals(componentDependencyRegistry.getDependencies("foo").size(), 0);
        
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

        add(foo, bar);
        add(something, fulano);
        add(other, beltrano);
        add(other, fulano);
        add(other, fulano);
        add(bar, null);
        add(fulano, null);
        add(beltrano, null);
        
        assertEquals(componentDependencyRegistry.getDependencies(other), new HashSet<>(Arrays.asList(fulano, beltrano)));
        assertEquals(componentDependencyRegistry.getDependencies(something), new HashSet<>(Arrays.asList(fulano)));
        assertEquals(componentDependencyRegistry.getDependencies(fulano), new HashSet<>(Arrays.asList()));
        assertEquals(componentDependencyRegistry.getDependencies(foo), new HashSet<>(Arrays.asList(bar)));
        assertEquals(componentDependencyRegistry.getDependencies(bar), new HashSet<>(Arrays.asList()));

        assertEquals(componentDependencyRegistry.getDependents(bar), new HashSet<>(Arrays.asList(foo)));
        assertEquals(componentDependencyRegistry.getDependents(fulano), new HashSet<>(Arrays.asList(other, something)));
        assertEquals(componentDependencyRegistry.getDependents(foo), new HashSet<>(Arrays.asList()));
        
        assertEquals(componentDependencyRegistry.getRootClasses(), new HashSet<>(Arrays.asList(bar, fulano, beltrano)));
        
        assertTrue(componentDependencyRegistry.contains(foo));
        assertTrue(componentDependencyRegistry.contains(bar));
        assertTrue(componentDependencyRegistry.contains(other));
        assertTrue(componentDependencyRegistry.contains(something));
        assertTrue(componentDependencyRegistry.contains(fulano));
        assertTrue(componentDependencyRegistry.contains(beltrano));
        assertFalse(componentDependencyRegistry.contains("blah"));

        assertTrue(componentDependencyRegistry.getClassNames().contains(foo));
        assertTrue(componentDependencyRegistry.getClassNames().contains(bar));
        assertTrue(componentDependencyRegistry.getClassNames().contains(other));
        assertTrue(componentDependencyRegistry.getClassNames().contains(something));
        assertTrue(componentDependencyRegistry.getClassNames().contains(fulano));
        assertTrue(componentDependencyRegistry.getClassNames().contains(beltrano));
        assertFalse(componentDependencyRegistry.getClassNames().contains("blah"));
        
    }
    
    private void add(String component, String dependency)
    {
        componentDependencyRegistry.add(component, dependency, true);
    }

}
