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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.ExceptionTracker;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExceptionTrackerImplTest extends Assert
{
    @Test
    public void check_exception_tracking()
    {
        Throwable t1 = new RuntimeException();
        Throwable t2 = new RuntimeException();

        ExceptionTracker et = new ExceptionTrackerImpl();

        for (int i = 0; i < 3; i++)
        {
            assertEquals(et.exceptionLogged(t1), i != 0);
            assertEquals(et.exceptionLogged(t2), i != 0);
        }
    }
}
