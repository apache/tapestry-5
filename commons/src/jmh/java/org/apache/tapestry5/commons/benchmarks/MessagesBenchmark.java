// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.commons.benchmarks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.util.AbstractMessages;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Message lookup, which happens for every {@code ${message:key}} in every template on every
 * render, and for every validation message a form produces.
 *
 * Two things about {@link AbstractMessages} are worth a number:
 *
 * <ul>
 * <li>{@code get(key)} resolves the key once. It used to be written as
 * {@code if (contains(key)) return valueForKey(key)} with {@code contains} itself being
 * {@code valueForKey(key) != null}, so every successful lookup resolved the key twice — two map
 * lookups where one would do for a property-file catalog, and worse for a subclass whose
 * {@code valueForKey} is more than a map lookup. {@link #setup()} asserts the single resolution
 * so that a regression fails loudly rather than quietly costing every template a second
 * lookup.</li>
 * <li>{@code format(key, args)} goes through {@code String.format} on every call. The
 * {@link MessageFormatter} is cached, but the format string it holds is re-parsed each time,
 * because {@code Formatter} is not thread-safe and so cannot be cached alongside it.</li>
 * </ul>
 *
 * {@link #getUnparameterised()} against {@link #formatWithArguments()} is the interesting pair:
 * it says how much a message costs once it takes arguments, and therefore whether pre-parsing the
 * format would be worth the complexity.
 *
 * The catalog here is backed by a plain {@link HashMap} so that the numbers describe
 * {@code AbstractMessages} itself rather than property-file loading.
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=Messages -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MessagesBenchmark
{
    // Counts how often the catalog is actually consulted, for countingGet()
    static class CountingMessages extends AbstractMessages
    {
        final AtomicInteger resolutions = new AtomicInteger();

        private final Map<String, String> values;

        CountingMessages(Locale locale, Map<String, String> values)
        {
            super(locale);

            this.values = values;
        }

        @Override
        protected String valueForKey(String key)
        {
            resolutions.incrementAndGet();

            return values.get(key);
        }

        @Override
        public Set<String> getKeys()
        {
            return values.keySet();
        }
    }

    static class MapMessages extends AbstractMessages
    {
        private final Map<String, String> values;

        MapMessages(Locale locale, Map<String, String> values)
        {
            super(locale);

            this.values = values;
        }

        @Override
        protected String valueForKey(String key)
        {
            return values.get(key);
        }

        @Override
        public Set<String> getKeys()
        {
            return values.keySet();
        }
    }

    private Messages messages;

    private CountingMessages counting;

    private String plainKey;

    private String parameterisedKey;

    private String missingKey;

    private Object[] arguments;

    @Setup
    public void setup()
    {
        Map<String, String> values = new HashMap<>();

        values.put("page-title", "Customer overview");
        values.put("greeting", "Hello, %s. You have %d unread messages.");

        messages = new MapMessages(Locale.ENGLISH, values);
        counting = new CountingMessages(Locale.ENGLISH, values);

        plainKey = "page-title";
        parameterisedKey = "greeting";
        missingKey = "no-such-key";

        arguments = new Object[]{ "Ben", 3 };

        verifySingleResolution();
    }

    /**
     * Pins down the claim in this class's comment: one {@code get} of a present key consults the
     * catalog exactly once.
     * A regression to the old two-lookup form fails here and says so, rather than leaving a stale
     * comment behind and a benchmark that quietly measures something else.
     */
    private void verifySingleResolution()
    {
        counting.resolutions.set(0);

        counting.get(plainKey);

        int resolutions = counting.resolutions.get();

        if (resolutions != 1)
        {
            throw new IllegalStateException(String.format(
                    "Expected AbstractMessages.get() to resolve the key once, but it resolved it %d time(s).",
                    resolutions));
        }
    }

    /**
     * The common case: a message with no arguments, pulled straight out of a template
     */
    @Benchmark
    public String getUnparameterised()
    {
        return messages.get(plainKey);
    }

    /** 
     * The miss path, which templates hit for every optional message
     */
    @Benchmark
    public String getMissing()
    {
        return messages.get(missingKey);
    }

    /**
     * Cached formatter lookup on its own, without the formatting
     */
    @Benchmark
    public MessageFormatter getFormatter()
    {
        return messages.getFormatter(parameterisedKey);
    }

    /** 
     * Formatter lookup plus String.format
     */
    @Benchmark
    public String formatWithArguments()
    {
        return messages.format(parameterisedKey, arguments);
    }
}
