// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ObjectComponentEventResultProcessorTest extends TapestryTestCase
{
    @SuppressWarnings("unchecked")
    @Test
    public void invocation_is_failure() throws Exception
    {
        ComponentResources resources = mockComponentResources();
        Component component = mockComponent();
        String result = "*INVALID*";

        train_getComponentResources(component, resources);
        train_getCompleteId(resources, "foo.Bar:gnip.gnop");

        List classes = Arrays.asList(String.class, List.class, Map.class);

        replay();

        ComponentEventResultProcessor p = new ObjectComponentEventResultProcessor(classes);

        try
        {
            p.processResultValue(result);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "A component event handler method returned the value *INVALID*.",
                                  "Return type java.lang.String can not be handled.",
                                  "Configured return types are java.lang.String, java.util.List, java.util.Map.");

        }
    }
}
