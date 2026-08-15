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

import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.commons.internal.BasicTypeCoercions;
import org.apache.tapestry5.commons.internal.services.TypeCoercerImpl;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.TimeInterval;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Coercion from strings, which is what a request actually does.
 *
 * Almost everything entering a Tapestry application arrives as text: component parameters out of
 * a template, event context values out of a URL, form submissions, query parameters.
 * It then gets coerced to a target type on the way in. {@link TypeCoercerBenchmark} measures the
 * machinery around a coercion; this measures the coercions themselves, on the types a page really binds.
 *
 * The pairing to watch is {@link #stringToEnum()}. It runs through
 * {@code StringToEnumCoercion}, which is backed by a
 * {@code CaseInsensitiveMap}, so an enum-typed component parameter pays for the character-by-
 * character case-insensitive hash measured in {@link CaseInsensitiveMapBenchmark} on top of the
 * coercion lookup itself. Two layers of overhead around what could be one map hit.
 *
 * {@link #stringToTimeInterval()} is the outlier by design: it parses a string with a regular
 * expression on every call. It is used for cache and expiry settings, so it is not on a hot path
 * today, but it is what a genuinely expensive coercion looks like next to the cheap ones.
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=StringCoercion -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class StringCoercionBenchmark
{
    public enum Position
    {
        ABOVE, BELOW, LEFT, RIGHT, CENTER
    }

    private TypeCoercer coercer;

    private String number;

    private String decimal;

    private String bool;

    private String enumValue;

    private String interval;

    private Integer boxed;

    @Setup
    public void setup()
    {
        CoercionTupleCollector collector = new CoercionTupleCollector();

        BasicTypeCoercions.provideBasicTypeCoercions(collector);
        BasicTypeCoercions.provideJSR310TypeCoercions(collector);

        coercer = new TypeCoercerImpl(collector.getTuples());

        number = "12345";
        decimal = "1234.5678";
        bool = "true";
        enumValue = "center";
        interval = "2 h 30 m";
        boxed = 12345;

        // Warm the coercion search, which is a startup cost and not what is being measured.
        stringToInteger();
        stringToLong();
        stringToDouble();
        stringToBoolean();
        stringToEnum();
        stringToTimeInterval();
        objectToString();
    }

    @Benchmark
    public Object stringToInteger()
    {
        return coercer.coerce(number, Integer.class);
    }

    @Benchmark
    public Object stringToLong()
    {
        return coercer.coerce(number, Long.class);
    }

    @Benchmark
    public Object stringToDouble()
    {
        return coercer.coerce(decimal, Double.class);
    }

    @Benchmark
    public Object stringToBoolean()
    {
        return coercer.coerce(bool, Boolean.class);
    }

    /**
     * Goes through StringToEnumCoercion, and therefore through a CaseInsensitiveMap
     */
    @Benchmark
    public Object stringToEnum()
    {
        return coercer.coerce(enumValue, Position.class);
    }

    /**
     * Parses with a regular expression on every call
     */
    @Benchmark
    public Object stringToTimeInterval()
    {
        return coercer.coerce(interval, TimeInterval.class);
    }

    /**
     * The other direction, which every rendered parameter value takes
     */
    @Benchmark
    public Object objectToString()
    {
        return coercer.coerce(boxed, String.class);
    }
}
