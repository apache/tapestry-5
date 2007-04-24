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

package org.apache.tapestry.integration;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.test.JettyRunner;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tapestry seems least stable at initial startup (all those proxies and generated code). So we're
 * giving it a crushing just as it starts up.
 */
public class LaunchCrusherTest extends Assert
{
    // Jetty's default thread pool max size is 256.  If you push this number too high, you get connection reset by peer
    // errors (I think Jetty reject requests when there's no available thread, after a short timeout).
    private static final int THREAD_COUNT = 50;

    /** The port on which the internal copy of Jetty is executed. */
    public static final int JETTY_PORT = 9999;

    private JettyRunner _jetty;

    private final CyclicBarrier _barrier = new CyclicBarrier(THREAD_COUNT + 1);

    class Worker implements Runnable
    {
        private String _name;

        private String _content;

        public synchronized String getContent()
        {
            return _content;
        }

        public void run()
        {
            _name = Thread.currentThread().getName();

            System.out.printf("[%s] waiting ...\n", _name);

            try
            {
                _barrier.await();

                URL url = new URL(String.format("http://localhost:%d/", JETTY_PORT));

                readContent(url);

                _barrier.await();
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }

        private void readContent(URL url)
        {
            System.out.printf("[%s] reading ...\n", _name);

            try
            {
                InputStream is = url.openStream();
                InputStreamReader reader = new InputStreamReader(new BufferedInputStream(is));

                StringBuilder builder = new StringBuilder();

                char[] buffer = new char[10000];

                while (true)
                {
                    int length = reader.read(buffer);

                    if (length < 0)
                        break;

                    builder.append(buffer, 0, length);
                }

                reader.close();

                synchronized (this)
                {
                    _content = builder.toString();
                }

                System.out.printf("[%s] done.\n", _name);
            }
            catch (Exception ex)
            {
                System.out.printf("[%s] fail: %s.\n", _name, ex.toString());

                synchronized (this)
                {
                    _content = "[" + ex.toString() + "]";
                }
            }
        }
    }

    @BeforeClass
    public void setup()
    {
        _jetty = new JettyRunner("/", JETTY_PORT, "src/test/app1");
    }

    @AfterClass
    public void cleanup()
    {
        _jetty.stop();
    }

    @Test
    public void crushing_number_of_threads_at_startup() throws Exception
    {
        Worker[] workers = new Worker[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++)
        {
            workers[i] = new Worker();

            new Thread(workers[i]).start();
        }

        _barrier.await();

        System.out.printf("%d threads started\n", THREAD_COUNT);

        _barrier.await();

        String expected = workers[0].getContent();
        String failureContent = null;
        List<Integer> failures = newList();

        System.out.printf("*****\n%s\n*****\n", expected);

        for (int i = 1; i < THREAD_COUNT; i++)
        {
            String actual = workers[i].getContent();

            if (!actual.equals(expected))
            {
                failures.add(i);

                if (failureContent == null)
                    failureContent = actual;

            }
        }

        if (failureContent != null)
        {
            System.err.println("Failures in thread(s): " + InternalUtils.join(failures));

            assertEquals(failureContent, expected);
        }
    }
}
