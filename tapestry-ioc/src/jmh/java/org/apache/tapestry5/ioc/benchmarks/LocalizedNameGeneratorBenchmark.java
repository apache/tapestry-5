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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.ioc.util.LocalizedNameGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Localized resource name generation, which sits underneath message catalogs, templates and
 * assets. Every lookup for a locale walks the whole variant chain ({@code Base_de_DE_x.ext},
 * {@code Base_de_DE.ext}, {@code Base_de.ext}, {@code Base.ext}) until something is found, and
 * a miss walks all of it.
 *
 * The generator allocates a {@code StringBuilder} per instance and a {@code String} per variant,
 * so the cost scales with how specific the locale is. That is the point of the three benchmarks:
 * an application serving {@code de_DE} does strictly more work per lookup than one serving
 * {@code en}, and {@link #fullVariant()} is what the worst case actually costs.
 *
 * Run this one with {@code -Pjmh.profilers=gc}, as the nanoseconds are small, and allocations
 * can be an indicator if it's worth to cache.
 *
 * <pre>
 * ./gradlew :tapestry-ioc:jmh -Pjmh.include=LocalizedNameGenerator -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class LocalizedNameGeneratorBenchmark
{
    private static final String PATH = "org/example/pages/Index.properties";

    private Locale language;

    private Locale languageAndCountry;

    private Locale full;

    @Setup
    public void setup()
    {
        language = new Locale("en");
        languageAndCountry = new Locale("de", "DE");
        full = new Locale("de", "DE", "bavarian");
    }

    @Benchmark
    public void languageOnly(Blackhole blackhole)
    {
        for (String name : new LocalizedNameGenerator(PATH, language))
        {
            blackhole.consume(name);
        }
    }

    @Benchmark
    public void languageAndCountry(Blackhole blackhole)
    {
        for (String name : new LocalizedNameGenerator(PATH, languageAndCountry))
        {
            blackhole.consume(name);
        }
    }

    @Benchmark
    public void fullVariant(Blackhole blackhole)
    {
        for (String name : new LocalizedNameGenerator(PATH, full))
        {
            blackhole.consume(name);
        }
    }
}
