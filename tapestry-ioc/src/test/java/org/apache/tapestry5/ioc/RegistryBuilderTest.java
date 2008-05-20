// Copyright 2006, 2007 The Apache Software Foundation
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

import java.util.Arrays;
import java.util.List;

public class RegistryBuilderTest extends IOCTestCase
{
    @Test
    public void sub_module()
    {
        RegistryBuilder builder = new RegistryBuilder();

        builder.add(MasterModule.class);

        Registry r = builder.build();

        // Borrowed from IntegrationTest, this will only work if both FredModule and BarneyModule
        // are loaded.

        NameListHolder service = r.getService("UnorderedNames", NameListHolder.class);

        List<String> names = service.getNames();

        assertEquals(names, Arrays.asList("Beta", "Gamma", "UnorderedNames"));

        r.shutdown();
    }

    @Test
    public void manifest()
    {
        RegistryBuilder builder = new RegistryBuilder();

        String value = String.format("%s, %s, %s", FredModule.class.getName(), BarneyModule.class
                .getName(), RegistryBuilderTestModule.class.getName());

        IOCUtilities.addModulesInList(builder, value);

        Registry registry = builder.build();

        Square service = registry.getService(Square.class);

        assertEquals(service.square(4), 16l);

        // This proves that the IOC works, the service builder method was invoked, that the
        // ClassFactory service was accessed and used.

        assertEquals(service.toString(), "<Proxy for Square(org.apache.tapestry5.ioc.Square)>");

        registry.shutdown();
    }
}
