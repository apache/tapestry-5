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
import org.apache.tapestry5.commons.services.Coercion;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * The warm coercion path, which every request walks many times over: component parameter binding,
 * event context values and property conduits all funnel through {@link TypeCoercer#coerce}.
 *
 * This benchmark was written to investigate a lock in
 * {@link TypeCoercerImpl}{@code .getTargetCoercion}, which took a {@code ReentrantReadWriteLock}
 * read lock on every call, hit or not, because {@code typeToTargetCoercion} was a
 * {@code WeakHashMap}, and a WeakHashMap read is a write, since {@code get} expunges cleared
 * entries. At eight threads that lock cost 1690 ns per lookup against 9 ns single-threaded. It has
 * since been replaced by a {@code ConcurrentHashMap}; see
 * {@code ISSUE_TYPECOERCER_LOCK_CONTENTION.md} and {@code benchmark-results/type-coercer-*.json}.
 *
 * It is kept as a regression guard, and answers two questions:
 *
 * <ul>
 * <li>How much of a warm coercion is bookkeeping rather than conversion? Compare
 * {@link #lookupOnly()} against {@link #stringToInteger()}.</li>
 * <li>Does the lookup still scale? Re-run with {@code -Pjmh.threads=max} and compare against the
 * single-threaded numbers. They should stay close. If a future change reintroduces
 * synchronisation on this path, this is where it will show up.</li>
 * </ul>
 *
 * {@link #alreadyAssignable()} is the control: it returns before reaching the lock at all, so it
 * measures the floor that the other numbers should be read against.
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=TypeCoercer
 * ./gradlew :commons:jmh -Pjmh.include=TypeCoercer -Pjmh.threads=max
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TypeCoercerBenchmark
{
    private TypeCoercer coercer;

    private String numericString;
    private String booleanString;
    private Integer number;

    @Setup
    public void setup()
    {
        CoercionTupleCollector collector = new CoercionTupleCollector();

        BasicTypeCoercions.provideBasicTypeCoercions(collector);
        BasicTypeCoercions.provideJSR310TypeCoercions(collector);

        coercer = new TypeCoercerImpl(collector.getTuples());

        numericString = "12345";
        booleanString = "true";
        number = 12345;

        // Warm every cache the benchmarks below rely on, so that the measurement never
        // includes the coercion search. That path is a startup cost, not a per-request one.
        stringToInteger();
        stringToBoolean();
        integerToString();
        lookupOnly();
    }

    // Cache hit: two map lookups and one Integer.valueOf
    @Benchmark
    public Object stringToInteger()
    {
        return coercer.coerce(numericString, Integer.class);
    }

    @Benchmark
    public Object stringToBoolean()
    {
        return coercer.coerce(booleanString, Boolean.class);
    }

    @Benchmark
    public Object integerToString()
    {
        return coercer.coerce(number, String.class);
    }

    /**
     * The lookup without the conversion: two map reads, one per cache layer.
     * Whatever this costs is pure overhead on every coercion.
     */
    @Benchmark
    public Coercion<String, Integer> lookupOnly()
    {
        return coercer.getCoercion(String.class, Integer.class);
    }

    /**
     * Control: the early return in the coerce method, before either cache is consulted
     */
    @Benchmark
    public Object alreadyAssignable()
    {
        return coercer.coerce(numericString, String.class);
    }
}
