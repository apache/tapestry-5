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
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Anonymous inner classes against lambdas, on real Tapestry code rather than a synthetic fixture.
 *
 * {@link BasicTypeCoercions#provideBasicTypeCoercions} contributes over two dozens of coercions
 * as anonymous inner classes, and {@link LambdaTypeCoercions} is the same method written with
 * lambdas and method references.
 *
 * The two styles differ in three ways that pull in opposite directions, which is the whole reason
 * to measure rather than assume:
 *
 * <dl>
 * <dt>Cold</dt>
 * <dd>An anonymous class costs one class file to find, read, define and verify. A lambda costs an
 * {@code invokedynamic} bootstrap, which spins a hidden class through {@code LambdaMetafactory} on
 * first execution. Historically the bootstrap is the more expensive of the two, and it is paid on
 * exactly the paths that make startup and first page load feel slow. Hence {@code cold*} below.</dd>
 *
 * <dt>Warm</dt>
 * <dd>Once linked, a non-capturing lambda is a singleton: re-running the contribution method
 * allocates nothing for it. A non-capturing anonymous class allocates a fresh instance every time.
 * Run with {@code -Pjmh.profilers=gc} to see that as an allocation rate, which is the number that
 * matters here, not the nanoseconds.</dd>
 *
 * <dt>Footprint</dt>
 * <dd>Not measurable from inside the JVM: ~27 fewer class files in the jar, against the same amount
 * of hidden classes created at runtime. Compare {@code build/classes} directory sizes for that.</dd>
 * </dl>
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=CoercionStyle -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Thread)
public class CoercionStyleBenchmark
{
    private CoercionTupleCollector collector;

    @Setup(Level.Iteration)
    public void reset()
    {
        collector = new CoercionTupleCollector();
    }

    // ---------------------------------------------------------------------------------------
    // Cold: one measured invocation in a JVM that has never executed this code.
    //
    // Twenty forks because a single shot of interpreted, unlinked code is noisy.
    // Treat the spread as seriously as the mean.
    // JMH forks each benchmark method separately, so coldAnonymousClasses and coldLambdas
    // never share a JVM.
    // ---------------------------------------------------------------------------------------

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 20, warmups = 0)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Object coldAnonymousClasses()
    {
        BasicTypeCoercions.provideBasicTypeCoercions(collector);
        return collector.getTuples();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 20, warmups = 0)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Object coldLambdas()
    {
        LambdaTypeCoercions.provideBasicTypeCoercions(collector);
        return collector.getTuples();
    }

    /**
     * Same as {@link #coldLambdas()}, but executing a throwaway lambda first.
     *
     * This was written to separate two costs: bootstrapping the method-handle machinery, which
     * the first lambda in a JVM pays for once, from linking an individual call site, which every
     * lambda pays. It does not succeed, and the reason is worth keeping rather than deleting the
     * benchmark: JMH's own harness executes lambdas before it ever calls a benchmark method, so
     * {@code LambdaMetafactory} is already initialised in {@link #coldLambdas()} too. The two
     * scores come out within noise of each other.
     *
     * What that leaves is still the number that matters. Both measure the same amount of call
     * sites linking in a JVM whose method-handle machinery is already up, which is exactly the
     * situation a real application is in by the time its registry starts. Isolating the one-time
     * initialisation would need a harness that does not itself use lambdas, and it would answer
     * a question nobody is asking.
     */
    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Fork(value = 20, warmups = 0)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Object coldLambdasMetafactoryPrimed(Blackhole blackhole)
    {
        blackhole.consume(primeMetafactory());

        LambdaTypeCoercions.provideBasicTypeCoercions(collector);
        return collector.getTuples();
    }

    private static Runnable primeMetafactory()
    {
        return () -> { };
    }

    // ---------------------------------------------------------------------------------------
    // Warm: steady state, where the interesting axis is allocation rather than time.
    // ---------------------------------------------------------------------------------------

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Object warmAnonymousClasses()
    {
        CoercionTupleCollector fresh = new CoercionTupleCollector();
        BasicTypeCoercions.provideBasicTypeCoercions(fresh);
        return fresh.getTuples();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Object warmLambdas()
    {
        CoercionTupleCollector fresh = new CoercionTupleCollector();
        LambdaTypeCoercions.provideBasicTypeCoercions(fresh);
        return fresh.getTuples();
    }
}
