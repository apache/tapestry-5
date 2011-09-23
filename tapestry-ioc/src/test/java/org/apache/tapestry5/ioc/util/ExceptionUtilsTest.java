// Copyright 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

import org.apache.tapestry5.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExceptionUtilsTest extends Assert
{
    private final PropertyAccess access = new PropertyAccessImpl();

    @Test
    public void find_cause_with_match()
    {
        TapestryException inner = new TapestryException("foo", null);

        RuntimeException outer = new RuntimeException(inner);

        assertSame(ExceptionUtils.findCause(outer, TapestryException.class), inner);
        assertSame(ExceptionUtils.findCause(outer, TapestryException.class, access), inner);

    }

    @Test
    public void find_cause_no_match()
    {
        RuntimeException re = new RuntimeException("No cause for you!");

        assertNull(ExceptionUtils.findCause(re, TapestryException.class));
        assertNull(ExceptionUtils.findCause(re, TapestryException.class, access));
    }

    @Test
    public void find_hidden_exception()
    {
        RuntimeException inner = new RuntimeException();
        Exception outer = new ExceptionWrapper(inner);

        // TAP5-1639: The old code can't find inner
        assertNull(ExceptionUtils.findCause(outer, RuntimeException.class));

        // The new reflection-based code can:
        assertSame(ExceptionUtils.findCause(outer, RuntimeException.class, access), inner);
    }

}
