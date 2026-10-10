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

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.benchmarks.services.Greeter;
import org.apache.tapestry5.ioc.benchmarks.services.GreeterImpl;
import org.apache.tapestry5.ioc.benchmarks.services.BenchmarkModule;
import org.apache.tapestry5.ioc.modules.TapestryIOCModule;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/**
 * What a service call costs once the registry is warm.
 *
 * Nothing in Tapestry calls a service implementation directly. Every call goes through a Plastic
 * proxy that resolves the service on first use and forwards from then on, and a request touches
 * a great many of them. This measures that indirection against a plain virtual call on the same
 * implementation, so the overhead can be stated as a number rather than assumed to be free.
 *
 * {@link #lookupAndInvoke()} adds the registry lookup to the call. Code that calls
 * {@code registry.getService(...)} per request rather than injecting once is paying that, and
 * this says how much.
 *
 * <pre>
 * ./gradlew :tapestry-ioc:jmh -Pjmh.include=ServiceProxy
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ServiceProxyBenchmark
{
    private Registry registry;

    private Greeter proxy;

    private Greeter direct;

    private String name;

    @Setup(Level.Trial)
    public void startup()
    {
        registry = RegistryBuilder.buildAndStartupRegistry(TapestryIOCModule.class, BenchmarkModule.class);

        proxy = registry.getService(Greeter.class);
        direct = new GreeterImpl();

        name = "Tapestry";

        // Realise the service, so that the first measured call is not the one that builds it.
        proxy.greet(name);
    }

    @TearDown(Level.Trial)
    public void shutdown()
    {
        registry.shutdown();
    }

    /** The control: a plain interface call, no container involved. */
    @Benchmark
    public String directCall()
    {
        return direct.greet(name);
    }

    @Benchmark
    public String proxyCall()
    {
        return proxy.greet(name);
    }

    @Benchmark
    public String lookupAndInvoke()
    {
        return registry.getService(Greeter.class).greet(name);
    }
}
