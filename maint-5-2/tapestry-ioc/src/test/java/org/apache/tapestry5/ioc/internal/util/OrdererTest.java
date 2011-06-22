// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class OrdererTest extends IOCInternalTestCase
{
    @Test
    public void no_dependencies()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void override()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        o.override("barney", "Mr. Rubble", "before:*");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("Mr. Rubble", "FRED", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void failed_override()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        try
        {
            o.override("bambam", "Mr. Rubble JR.", "before:*");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Override for object 'bambam' is invalid as it does not match an existing object.");
        }

        verify();
    }

    @Test
    public void missing_constraint_type()
    {
        Logger logger = mockLogger();

        logger.warn(UtilMessages.constraintFormat("fred", "barney"));

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY", "fred");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void unknown_constraint_type()
    {
        Logger logger = mockLogger();

        logger.warn(UtilMessages.constraintFormat("nearby:fred", "barney"));

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY", "nearby:fred");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void nulls_not_included_in_result()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("zippo", null);
        o.add("wilma", "WILMA");
        o.add("groucho", null);
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void duplicate_id()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY");
        o.add("wilma", "WILMA");

        verify();

        logger.warn(UtilMessages.duplicateOrderer("fred"));

        replay();

        o.add("fred", "FRED2");

        verify();

        replay();

        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("FRED", "BARNEY", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void leader()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY", "before:*");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BARNEY", "FRED", "WILMA", "BETTY"));

        verify();
    }

    @Test
    public void trailer()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED");
        o.add("barney", "BARNEY", "after:*");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered.get(3), "BARNEY");

        verify();
    }

    @Test
    public void prereqs()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED", "after:wilma");
        o.add("barney", "BARNEY", "after:fred,betty");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("WILMA", "FRED", "BETTY", "BARNEY"));

        verify();
    }

    @Test
    public void pre_and_post_reqs()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED", "after:wilma");
        o.add("barney", "BARNEY", "after:fred,betty");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY", "before:wilma");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BETTY", "WILMA", "FRED", "BARNEY"));

        verify();
    }

    @Test
    public void case_insensitivity()
    {
        Logger logger = mockLogger();

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED", "after:Wilma");
        o.add("barney", "BARNEY", "after:Fred,BETTY");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY", "before:Wilma");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BETTY", "WILMA", "FRED", "BARNEY"));

        verify();
    }

    @Test
    public void dependency_cycle()
    {
        Logger logger = mockLogger();

        logger.warn("Unable to add 'barney' as a dependency of 'betty', as that forms a "
                + "dependency cycle ('betty' depends on itself via 'barney'). "
                + "The dependency has been ignored.");

        replay();

        Orderer<String> o = new Orderer<String>(logger);

        o.add("fred", "FRED", "after:wilma");
        o.add("barney", "BARNEY", "after:fred,betty");
        o.add("wilma", "WILMA");
        o.add("betty", "BETTY", "after:barney", "before:wilma");

        List<String> ordered = o.getOrdered();

        assertEquals(ordered, Arrays.asList("BETTY", "WILMA", "FRED", "BARNEY"));

        verify();
    }

    @Test
    public void toString_Orderable()
    {
        Orderable<String> simple = new Orderable<String>("simple", "SIMPLE");

        assertEquals(simple.toString(), "Orderable[simple SIMPLE]");

        Orderable<String> complex = new Orderable<String>("complex", "COMPLEX", "after:foo",
                                                          "before:bar");

        assertEquals(complex.toString(), "Orderable[complex after:foo before:bar COMPLEX]");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void toString_DependencyNode()
    {
        Logger logger = mockLogger();

        replay();

        DependencyNode<String> node1 = new DependencyNode<String>(logger, new Orderable("node1",
                                                                                        "NODE1"));

        assertEquals(node1.toString(), "[node1]");

        DependencyNode<String> node2 = new DependencyNode<String>(logger, new Orderable("node2",
                                                                                        "NODE2"));

        DependencyNode<String> node3 = new DependencyNode<String>(logger, new Orderable("node3",
                                                                                        "NODE3"));

        DependencyNode<String> node4 = new DependencyNode<String>(logger, new Orderable("node4",
                                                                                        "NODE4"));

        DependencyNode<String> node5 = new DependencyNode<String>(logger, new Orderable("node5",
                                                                                        "NODE5"));

        node2.addDependency(node1);
        node1.addDependency(node3);
        node1.addDependency(node4);
        node5.addDependency(node1);

        assertEquals(node5.toString(), "[node5: [node1: [node3], [node4]]]");

        verify();
    }
}
