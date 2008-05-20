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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class LoggerSourceImplTest extends IOCInternalTestCase
{
    @Test
    public void get_by_class()
    {
        Class clazz = getClass();

        Logger expected = LoggerFactory.getLogger(clazz);
        LoggerSource logSource = new LoggerSourceImpl();
        Logger actual = logSource.getLogger(clazz);

        assertSame(actual, expected);
    }

    @Test
    public void get_by_name()
    {
        String name = "foo.Bar";

        Logger expected = LoggerFactory.getLogger(name);
        LoggerSource logSource = new LoggerSourceImpl();
        Logger actual = logSource.getLogger(name);

        assertSame(actual, expected);

    }
}
