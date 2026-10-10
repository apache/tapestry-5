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

package org.apache.tapestry5.benchmarks;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.internal.services.PageLoader;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.test.PageTester;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Page loading, decomposed.
 *
 * "Page loading is slow" is really four costs stacked on top of each other, and they have
 * different fixes, so the benchmarks below separate them:
 *
 * <ol>
 * <li>{@link #coldRegistryStartup} — building the IoC registry. Paid once per application, but it
 * is what a developer waits for on every restart.</li>
 * <li>{@link #coldFirstPageLoad} — the first page through a fresh registry. On top of the page
 * itself this realises the whole service graph behind {@link PageLoader}, and transforms the core
 * component classes through Plastic.</li>
 * <li>{@link #coldSecondPageLoad} — a different page, once that machinery is up. This is the
 * honest "how expensive is one more page" number, and the one to watch when optimising
 * {@code PageLoaderImpl}.</li>
 * <li>{@link #warmRichPageAssembly} / {@link #warmSimplePageAssembly} — repeated assembly in a
 * long-lived registry, where templates are parsed and component classes transformed already. This
 * is what a page-cache miss costs a running application, and the difference between the two pages
 * is the per-component cost of assembly.</li>
 * </ol>
 *
 * The cold benchmarks are single-shot in fresh JVMs: interpreted, unlinked, JIT-free.
 * That is the code path a cold application actually takes. Their spread is wide: read the min/max,
 * not just the mean.
 *
 * The benchmark application lives in {@code src/jmh/java/.../benchmarks/app} with its templates
 * in {@code src/jmh/webapp}. {@link PageTester} resolves the context path relative to the working
 * directory, which Gradle sets to the module directory.
 *
 * Do not run the cold benchmarks with {@code -Pjmh.quick}: it forces three measurement
 * iterations per fork, and only the first of them is cold. Shorten them with {@code -Pjmh.forks}
 * instead.
 *
 * <pre>
 * ./gradlew :tapestry-core:jmh -Pjmh.include=PageLoad
 * ./gradlew :tapestry-core:jmh -Pjmh.include='PageLoad.cold' -Pjmh.forks=3
 * ./gradlew :tapestry-core:jmh -Pjmh.include='PageLoad.warm' -Pjmh.profilers=gc
 * </pre>
 */
public class PageLoadBenchmark
{
    private static final String APP_PACKAGE = "org.apache.tapestry5.benchmarks.app";

    private static final String APP_NAME = "";

    private static final String CONTEXT_PATH = "src/jmh/webapp";

    private static final String SIMPLE_PAGE = "SimplePage";

    private static final String RICH_PAGE = "RichPage";

    private static final ComponentResourceSelector SELECTOR = new ComponentResourceSelector(Locale.ENGLISH);

    private static PageTester newTester()
    {
        return new PageTester(APP_PACKAGE, APP_NAME, CONTEXT_PATH);
    }

    // ---------------------------------------------------------------------------------------
    // Cold. Each of these lives in its own state class, because JMH runs a state's @Setup for
    // every benchmark that references it — sharing one state would start a registry inside the
    // very measurement that is supposed to be timing the startup.
    // ---------------------------------------------------------------------------------------

    /** 
     * Holds nothing up front; the benchmark builds the registry and teardown disposes of it.
     */
    @State(Scope.Thread)
    public static class NoRegistry
    {
        PageTester tester;

        @TearDown(Level.Iteration)
        public void shutdown()
        {
            if (tester != null)
            {
                tester.shutdown();
                tester = null;
            }
        }
    }

    /** 
     * A registry that is up but has never loaded a page.
     */
    @State(Scope.Thread)
    public static class FreshRegistry
    {
        PageTester tester;

        PageLoader loader;

        @Setup(Level.Iteration)
        public void start()
        {
            tester = newTester();
            loader = tester.getService(PageLoader.class);
        }

        @TearDown(Level.Iteration)
        public void shutdown()
        {
            tester.shutdown();
        }
    }

    /** 
     * A registry that has already loaded one page, so the loading machinery is realised.
     */
    @State(Scope.Thread)
    public static class UsedRegistry
    {
        PageTester tester;

        PageLoader loader;

        @Setup(Level.Iteration)
        public void start()
        {
            tester = newTester();
            loader = tester.getService(PageLoader.class);
            loader.loadPage(SIMPLE_PAGE, SELECTOR);
        }

        @TearDown(Level.Iteration)
        public void shutdown()
        {
            tester.shutdown();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 10, warmups = 0)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Object coldRegistryStartup(NoRegistry state)
    {
        state.tester = newTester();

        return state.tester.getRegistry();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 10, warmups = 0)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Page coldFirstPageLoad(FreshRegistry state)
    {
        return state.loader.loadPage(RICH_PAGE, SELECTOR);
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Fork(value = 10, warmups = 0)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Page coldSecondPageLoad(UsedRegistry state)
    {
        return state.loader.loadPage(RICH_PAGE, SELECTOR);
    }

    // ---------------------------------------------------------------------------------------
    // Warm: one registry for the whole trial, assembling the same page over and over.
    // ---------------------------------------------------------------------------------------

    @State(Scope.Benchmark)
    public static class WarmRegistry
    {
        PageTester tester;

        PageLoader loader;

        @Setup(Level.Trial)
        public void start()
        {
            tester = newTester();
            loader = tester.getService(PageLoader.class);

            // Take the one-off costs — service realisation, class transformation, template
            // parsing — outside the measurement, which is the point of the warm benchmarks.
            loader.loadPage(SIMPLE_PAGE, SELECTOR);
            loader.loadPage(RICH_PAGE, SELECTOR);
        }

        @TearDown(Level.Trial)
        public void shutdown()
        {
            tester.shutdown();
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Page warmSimplePageAssembly(WarmRegistry state)
    {
        return state.loader.loadPage(SIMPLE_PAGE, SELECTOR);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public Page warmRichPageAssembly(WarmRegistry state)
    {
        return state.loader.loadPage(RICH_PAGE, SELECTOR);
    }
}
