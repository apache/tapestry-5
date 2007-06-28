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

package org.apache.tapestry.ioc.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tapestry.ioc.LogSource;
import org.testng.annotations.Test;

public class LogSourceImplTest extends IOCInternalTestCase
{
    @Test
    public void get_by_class()
    {
        Class clazz = getClass();

        Log expected = LogFactory.getLog(clazz);
        LogSource logSource = new LogSourceImpl();
        Log actual = logSource.getLog(clazz);

        assertSame(actual, expected);
    }

    @Test
    public void get_by_name()
    {
        String name = "foo.Bar";

        Log expected = LogFactory.getLog(name);
        LogSource logSource = new LogSourceImpl();
        Log actual = logSource.getLog(name);

        assertSame(actual, expected);

    }
}
