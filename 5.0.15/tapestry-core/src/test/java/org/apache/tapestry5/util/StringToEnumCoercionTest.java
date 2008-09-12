// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.util;

import junit.framework.AssertionFailedError;
import org.apache.tapestry5.Stooge;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StringToEnumCoercionTest extends Assert
{
    @Test
    public void value_found()
    {
        StringToEnumCoercion<Stooge> coercion = new StringToEnumCoercion<Stooge>(Stooge.class);

        assertSame(coercion.coerce("moe"), Stooge.MOE);
        assertSame(coercion.coerce("curly_joe"), Stooge.CURLY_JOE);
    }

    @Test
    public void blank_is_null()
    {
        StringToEnumCoercion<Stooge> coercion = new StringToEnumCoercion<Stooge>(Stooge.class);

        assertNull(coercion.coerce(""));
    }

    @Test
    public void value_not_found()
    {
        StringToEnumCoercion<Stooge> coercion = new StringToEnumCoercion<Stooge>(Stooge.class);

        try
        {
            coercion.coerce("shemp");
            throw new AssertionFailedError("unreachable");
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Input \'shemp\' does not identify a value from enumerated type org.apache.tapestry5.Stooge. Available values: CURLY_JOE, LARRY, MOE.");
        }
    }
}
