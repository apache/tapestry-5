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

import org.apache.tapestry5.func.Mapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * The controlled counterpart to {@link CoercionStyleBenchmark}: instead of one real contribution
 * method, this isolates the three things that actually differ between an anonymous inner class
 * and a lambda.
 *
 * <ol>
 * <li><b>Invocation</b> of an already-created instance.</li>
 * <li><b>Creation</b>. A non-capturing lambda is linked once and handed back as a singleton
 * forever after. A non-capturing anonymous class allocates every time it is evaluated. A capturing
 * lambda allocates too, so the win exists only where the anonymous class captured nothing.
 * Run with {@code -Pjmh.profilers=gc}; the allocation rate is the result here, not the time.</li>
 * <li><b>Call-site shape</b>. Tapestry rarely calls one implementation through one site. It calls
 * a contributed list of them — coercions, render commands, page-assembly actions — through a
 * single megamorphic site, where the JIT can no longer inline the target. If the two styles differ
 * anywhere non-obviously, it is here.</li>
 * </ol>
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=AnonymousVsLambda -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class AnonymousVsLambdaBenchmark
{
    // Non-constant, so the JIT cannot fold the results away
    private int input;

    private Mapper<Integer, Integer> anonymous;
    private Mapper<Integer, Integer> lambda;
    private Mapper<Integer, Integer> methodReference;

    private Mapper<Integer, Integer>[] anonymousMegamorphic;
    private Mapper<Integer, Integer>[] lambdaMegamorphic;

    @Setup
    public void setup()
    {
        input = 42;

        anonymous = new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer element)
            {
                return element + 1;
            }
        };

        lambda = element -> element + 1;

        methodReference = AnonymousVsLambdaBenchmark::increment;

        anonymousMegamorphic = anonymousMappers();
        lambdaMegamorphic = lambdaMappers();
    }

    private static Integer increment(Integer element)
    {
        return element + 1;
    }

    // ---------------------------------------------------------------------------------------
    // 1. Invocation of an existing instance, monomorphic call site.
    // ---------------------------------------------------------------------------------------

    @Benchmark
    public Integer invokeAnonymous()
    {
        return anonymous.map(input);
    }

    @Benchmark
    public Integer invokeLambda()
    {
        return lambda.map(input);
    }

    @Benchmark
    public Integer invokeMethodReference()
    {
        return methodReference.map(input);
    }

    // ---------------------------------------------------------------------------------------
    // 2. Creation. The point of interest is allocation, so read these with -Pjmh.profilers=gc.
    // ---------------------------------------------------------------------------------------

    @Benchmark
    public Mapper<Integer, Integer> createAnonymousNonCapturing()
    {
        return new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer element)
            {
                return element + 1;
            }
        };
    }

    /**
     * Expected to allocate nothing at all: the JVM hands back the same linked instance
     */
    @Benchmark
    public Mapper<Integer, Integer> createLambdaNonCapturing()
    {
        return element -> element + 1;
    }

    @Benchmark
    public Mapper<Integer, Integer> createAnonymousCapturing()
    {
        final int captured = input;

        return new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer element)
            {
                return element + captured;
            }
        };
    }

    /**
     * Capturing removes the singleton advantage: this allocates, like the anonymous class does
     */
    @Benchmark
    public Mapper<Integer, Integer> createLambdaCapturing()
    {
        final int captured = input;

        return element -> element + captured;
    }

    // ---------------------------------------------------------------------------------------
    // 3. Megamorphic dispatch, the shape Tapestry's contributed collections actually have.
    // ---------------------------------------------------------------------------------------

    @Benchmark
    public void invokeMegamorphicAnonymous(Blackhole blackhole)
    {
        for (Mapper<Integer, Integer> mapper : anonymousMegamorphic)
        {
            blackhole.consume(mapper.map(input));
        }
    }

    @Benchmark
    public void invokeMegamorphicLambda(Blackhole blackhole)
    {
        for (Mapper<Integer, Integer> mapper : lambdaMegamorphic)
        {
            blackhole.consume(mapper.map(input));
        }
    }

    @SuppressWarnings("unchecked")
    private static Mapper<Integer, Integer>[] anonymousMappers()
    {
        return new Mapper[]{
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return element + 1;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return element - 1;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return element * 2;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return element / 2;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return element ^ 0x5f;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return -element;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return element % 7;
                    }
                },
                new Mapper<Integer, Integer>()
                {
                    @Override
                    public Integer map(Integer element)
                    {
                        return Integer.reverse(element);
                    }
                }
        };
    }

    @SuppressWarnings("unchecked")
    private static Mapper<Integer, Integer>[] lambdaMappers()
    {
        return new Mapper[]{
                (Mapper<Integer, Integer>) element -> element + 1,
                (Mapper<Integer, Integer>) element -> element - 1,
                (Mapper<Integer, Integer>) element -> element * 2,
                (Mapper<Integer, Integer>) element -> element / 2,
                (Mapper<Integer, Integer>) element -> element ^ 0x5f,
                (Mapper<Integer, Integer>) element -> -element,
                (Mapper<Integer, Integer>) element -> element % 7,
                (Mapper<Integer, Integer>) Integer::reverse
        };
    }
}
