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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Locale;

public class ThreadLocaleImplTest extends IOCInternalTestCase
{
    private ThreadLocale threadLocale;

    private static final Locale FAKE_LOCALE1 = new Locale("klingon");

    private static final Locale FAKE_LOCALE2 = new Locale("ferrengi");

    @BeforeClass
    public void setup()
    {
        threadLocale = getService(ThreadLocale.class);
    }

    @Test
    public void different_threads_track_different_values() throws InterruptedException
    {
        final Locale initial = threadLocale.getLocale();

        threadLocale.setLocale(FAKE_LOCALE1);

        assertSame(threadLocale.getLocale(), FAKE_LOCALE1);

        Runnable r = new Runnable()
        {
            public void run()
            {
                assertSame(threadLocale.getLocale(), initial);
            }
        };

        Thread t = new Thread(r);

        t.start();
        t.join();

        cleanupThread();
    }

    public void thread_locale_reverts_after_cleanup()
    {
        Locale initial = threadLocale.getLocale();

        threadLocale.setLocale(FAKE_LOCALE2);

        cleanupThread();

        assertSame(threadLocale.getLocale(), initial);
    }
}
