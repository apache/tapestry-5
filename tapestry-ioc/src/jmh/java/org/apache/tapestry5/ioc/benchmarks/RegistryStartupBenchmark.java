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

package org.apache.tapestry5.ioc.benchmarks;

import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.modules.TapestryIOCModule;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Building the IoC registry, which every application restart pays for before it can serve
 * anything.
 *
 * Only tapestry-ioc's own module is loaded here, so this is the floor: module class scanning,
 * service definition, decorator and contribution wiring, and the eager-loading of whatever
 * declares itself eager. Loading a page and its component classes on top of it is measured by
 * {@code PageLoadBenchmark} in tapestry-core.
 *
 * Single-shot in a fresh JVM, because that is the only honest way to measure startup: half of
 * what it costs is class loading and interpretation that never happens again. Do not run this
 * with {@code -Pjmh.quick}, which would force three measurement iterations per fork and warm
 * two of them; use {@code -Pjmh.forks} to shorten it instead.
 *
 * {@link #buildOnly()} against {@link #buildAndStartup()} separates defining the services from
 * eagerly instantiating them, and {@link #buildAndResolveOneService()} shows what the first
 * service realisation adds on top — that is the proxy-and-construct path every later service
 * repeats.
 *
 * <pre>
 * ./gradlew :tapestry-ioc:jmh -Pjmh.include=RegistryStartup -Pjmh.forks=5
 * </pre>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 10, warmups = 0)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
public class RegistryStartupBenchmark
{
    private Registry registry;

    @TearDown(Level.Iteration)
    public void shutdown()
    {
        if (registry != null)
        {
            registry.shutdown();
            registry = null;
        }
    }

    /**
     * Definitions only: no service is instantiated.
     */
    @Benchmark
    public Registry buildOnly()
    {
        registry = new RegistryBuilder().add(TapestryIOCModule.class).build();

        return registry;
    }

    @Benchmark
    public Registry buildAndStartup()
    {
        registry = RegistryBuilder.buildAndStartupRegistry(TapestryIOCModule.class);

        return registry;
    }

    /**
     * Adds the first real service realisation: a Plastic proxy, constructor injection, and the
     * decorator stack around it.
     */
    @Benchmark
    public Object buildAndResolveOneService()
    {
        registry = RegistryBuilder.buildAndStartupRegistry(TapestryIOCModule.class);

        TypeCoercer coercer = registry.getService(TypeCoercer.class);

        // getService returns a proxy; the service is not built until it is called.
        return coercer.coerce("42", Integer.class);
    }

    /**
     * A second, unrelated service, to show whether the first one carried shared cost.
     */
    @Benchmark
    public Object buildAndResolveTwoServices()
    {
        registry = RegistryBuilder.buildAndStartupRegistry(TapestryIOCModule.class);

        Object first = registry.getService(TypeCoercer.class).coerce("42", Integer.class);
        Object second = registry.getService(SymbolSource.class).valueForSymbol("tapestry.version");

        return first.hashCode() + second.hashCode();
    }
}
