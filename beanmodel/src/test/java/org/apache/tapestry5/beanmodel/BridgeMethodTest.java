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

package org.apache.tapestry5.beanmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A method invocation resolves to the declared method, not to a compiler-generated bridge method,
 * whatever order {@link Class#getMethods()} returns them in.
 */
public class BridgeMethodTest
{
    private static PropertyConduitSource source;

    @BeforeAll
    static void setup()
    {
        source = PropertyConduitSourceFixture.create();
    }

    @Test
    void generic_interface_method_has_the_resolved_return_type()
    {
        assertSame(Long.class, source.create(LongProducer.class, "produce()").getPropertyType());
    }

    @Test
    void generic_interface_method_is_invoked()
    {
        assertEquals(42L, source.create(LongProducer.class, "produce()").get(new LongProducer()));
    }

    @Test
    void method_inherited_from_package_private_class_is_invoked()
    {
        // javac makes the inherited method reachable only through a bridge method in the public subclass.

        assertEquals("base", source.create(PublicSubclass.class, "getValue()").get(new PublicSubclass()));
    }

    @Test
    void method_inherited_from_package_private_class_has_its_return_type()
    {
        assertSame(String.class, source.create(PublicSubclass.class, "getValue()").getPropertyType());
    }

    public interface Producer<T>
    {
        T produce();
    }

    public static class LongProducer implements Producer<Long>
    {
        @Override
        public Long produce()
        {
            return 42L;
        }
    }

    static class PackagePrivateBase
    {
        public String getValue()
        {
            return "base";
        }
    }

    public static class PublicSubclass extends PackagePrivateBase
    {
    }
}
