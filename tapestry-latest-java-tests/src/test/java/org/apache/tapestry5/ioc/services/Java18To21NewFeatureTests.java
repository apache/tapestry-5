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
package org.apache.tapestry5.ioc.services;

import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for Tapestry-IoC with Java language features introduced between Java SE 18 and 21.
 * <ul>
 *   <li>JEP 431: Sequenced collections</li>
 *   <li>JEP 440: Record patterns</li>
 *   <li>JEP 441: Pattern matching</li>
 *   <li>JEP 444: Virtual threads</li>
 * </ul>
 */
class Java18To21NewFeatureTests
{
    public static class Java18To21Module
    {
        public static void bind(ServiceBinder binder)
        {
            binder.bind(Java18To21Service.class, Java18To21ServiceImpl.class);
            binder.bind(Java18To21ConcreteService.class);
        }
    }

    private static Java18To21Service java18To21Service;

    private static Java18To21ConcreteService java18To21ConcreteService;

    private static Registry registry;

    @BeforeAll
    public static void setup()
    {
        registry = RegistryBuilder.buildAndStartupRegistry(Java18To21Module.class);
        java18To21Service = registry.getService(Java18To21Service.class);
        java18To21ConcreteService = registry.getService(Java18To21ConcreteService.class);
    }

    /**
     * Tests SequencedCollection as a service return type (JEP 431).
     * @see https://openjdk.org/jeps/431
     */
    @Test
    public void sequencedCollectionReturnType()
    {
        SequencedCollection<String> serviceCol = java18To21Service.getSequencedCollection();

        assertNotNull(serviceCol);
        assertEquals("first", serviceCol.getFirst());
        assertEquals("last", serviceCol.getLast());

        SequencedCollection<String> concreteCol = java18To21ConcreteService.getSequencedCollection();
        assertNotNull(concreteCol);
        assertEquals("first", concreteCol.getFirst());
        assertEquals("last", concreteCol.getLast());
    }

    /**
     * Tests SequencedMap as a service return type (JEP 431).
     * @see https://openjdk.org/jeps/431
     */
    @Test
    public void sequencedMapReturnType()
    {
        SequencedMap<String, Integer> serviceMap = java18To21Service.getSequencedMap();
        assertNotNull(serviceMap);
        assertEquals("one", serviceMap.firstEntry().getKey());
        assertEquals("three", serviceMap.lastEntry().getKey());

        SequencedMap<String, Integer> concreteMap = java18To21ConcreteService.getSequencedMap();
        assertNotNull(concreteMap);
        assertEquals("one", concreteMap.firstEntry().getKey());
        assertEquals("three", concreteMap.lastEntry().getKey());
    }

    /**
     * Tests record patterns in switch expressions (JEP 440).
     * https://openjdk.org/jeps/440
     */
    @Test
    public void recordPatternsInSwitch()
    {
        java18To21Service.recordPatternsInSwitch();
        java18To21ConcreteService.recordPatternsInSwitch();
    }

    /**
     * Tests pattern matching for switch with a sealed class hierarchy (JEP 441).
     * @see https://openjdk.org/jeps/441
     */
    @Test
    public void patternMatchingForSwitchWithSealedClass()
    {
        java18To21Service.patternMatchingForSwitchWithSealedClass();
        java18To21ConcreteService.patternMatchingForSwitchWithSealedClass();
    }

    /**
     * Tests pattern matching for switch with a sealed interface (JEP 441).
     * @see https://openjdk.org/jeps/441
     */
    @Test
    public void patternMatchingForSwitchWithSealedInterface()
    {
        java18To21Service.patternMatchingForSwitchWithSealedInterface();
        java18To21ConcreteService.patternMatchingForSwitchWithSealedInterface();
    }

    /**
     * Tests record patterns in instanceof expressions (JEP 440).
     * @see https://openjdk.org/jeps/440
     */
    @Test
    public void recordPatternsInInstanceOf()
    {
        java18To21Service.recordPatternsInInstanceOf();
        java18To21ConcreteService.recordPatternsInInstanceOf();
    }

    /**
     * Tests that Tapestry service proxies are callable from virtual threads (JEP 444).
     * @see https://openjdk.org/jeps/444
     */
    @Test
    public void virtualThreadServiceCall() throws InterruptedException
    {
        AtomicReference<String> serviceResult = new AtomicReference<>();
        AtomicReference<String> concreteResult = new AtomicReference<>();

        Thread vt1 = Thread.ofVirtual().start(() ->
                serviceResult.set(java18To21Service.getSequencedCollection().getFirst()));
        Thread vt2 = Thread.ofVirtual().start(() ->
                concreteResult.set(java18To21ConcreteService.getSequencedCollection().getFirst()));

        vt1.join();
        vt2.join();

        assertEquals("first", serviceResult.get());
        assertEquals("first", concreteResult.get());
    }

}
