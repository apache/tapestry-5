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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class FormSupportImplTest extends InternalBaseTestCase
{
    @Test
    public void execute_deferred_with_no_commands()
    {
        FormSupportImpl support = new FormSupportImpl();

        support.executeDeferred();
    }

    @Test
    public void execute_deferred_execute_in_added_order()
    {
        Runnable r1 = newRunnable();
        Runnable r2 = newRunnable();

        getMocksControl().checkOrder(true);

        r1.run();
        r2.run();

        replay();

        FormSupportImpl support = new FormSupportImpl();

        support.defer(r1);
        support.defer(r2);

        support.executeDeferred();

        verify();
    }

    @Test
    public void deferred_commands_execute_once()
    {
        Runnable r1 = newRunnable();
        Runnable r2 = newRunnable();
        Runnable r3 = newRunnable();

        getMocksControl().checkOrder(true);

        r1.run();
        r2.run();

        replay();

        FormSupportImpl support = new FormSupportImpl();

        support.defer(r1);
        support.defer(r2);

        support.executeDeferred();

        verify();

        r3.run();

        replay();

        support.defer(r3);

        support.executeDeferred();

        verify();
    }
}
