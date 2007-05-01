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

package org.apache.tapestry.ioc.internal.services;

import java.util.Locale;

import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThreadLocaleImplTest extends IOCInternalTestCase
{
    private ThreadLocale _threadLocale;

    private static final Locale FAKE_LOCALE1 = new Locale("klingon");

    private static final Locale FAKE_LOCALE2 = new Locale("ferrengi");

    @BeforeClass
    public void setup()
    {
        _threadLocale = getService(ThreadLocale.class);
    }

    @Test
    public void different_threads_track_different_values() throws InterruptedException
    {
        final Locale initial = _threadLocale.getLocale();

        _threadLocale.setLocale(FAKE_LOCALE1);

        assertSame(_threadLocale.getLocale(), FAKE_LOCALE1);

        Runnable r = new Runnable()
        {
            public void run()
            {
                assertSame(_threadLocale.getLocale(), initial);
            }
        };

        Thread t = new Thread(r);

        t.start();
        t.join();

        cleanupThread();
    }

    public void thread_locale_reverts_after_cleanup()
    {
        Locale initial = _threadLocale.getLocale();

        _threadLocale.setLocale(FAKE_LOCALE2);

        cleanupThread();

        assertSame(_threadLocale.getLocale(), initial);
    }
}
