// Copyright 2006 The Apache Software Foundation
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

import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageFormatterImplTest extends Assert
{
    private String run(String format, Object... args)
    {
        return new MessageFormatterImpl(format, null).format(args);
    }

    @Test
    public void standard_args()
    {
        assertEquals(run("Tapestry is %s.", "cool"), "Tapestry is cool.");
        assertEquals(run("Tapestry release #%d.", 5), "Tapestry release #5.");
        assertEquals(run("%s is %s at version %d.", "Tapestry", "cool", 5), "Tapestry is cool at version 5.");
    }

    @Test
    public void throwable_argument()
    {
        Throwable t = new RuntimeException("Just didn't feel right.");

        assertEquals(run("%s failed: %s", "Something", t), "Something failed: Just didn't feel right.");
    }

    @Test
    public void throwable_argument_with_null_message()
    {
        Throwable t = new NullPointerException();

        assertEquals(run("%s failed: %s", "Something", t), "Something failed: java.lang.NullPointerException");
    }
}
