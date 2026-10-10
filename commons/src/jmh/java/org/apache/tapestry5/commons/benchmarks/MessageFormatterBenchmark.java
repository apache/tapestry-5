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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.commons.MessageFormatter;
import org.apache.tapestry5.commons.internal.util.MessageFormatterImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * What {@code MessageFormatterImpl.format} costs, and what it could cost at best.
 *
 * This is a hotter path than it looks. Validators build their client-side message during
 * <em>render</em>, not only on failure (see {@code Max.render}), which calls
 * {@code buildMessage} to fill in {@code data-max-message}. Every validated field on every
 * rendered form formats at least one message per validator.
 *
 * The formatter caches nothing: the format string is final, yet {@code String.format} re-parses it
 * on every call, allocating an {@code ArrayList} plus a {@code FixedString} or
 * {@code FormatSpecifier} per chunk, on top of the {@code Formatter} and its {@code StringBuilder}.
 *
 * The question is whether pre-parsing at construction is worth it, and the two message shapes here
 * are chosen to answer it honestly rather than flatteringly:
 *
 * <ul>
 * <li>{@link #currentSimple()} uses {@code required}, a bare {@code %s} message. 58% of the
 * shipped catalog looks like this, and it is locale-independent, so a hand-rolled formatter could
 * reproduce it exactly.</li>
 * <li>{@link #currentValidator()} uses {@code max-integer}, which is
 * {@code %2$s requires a value no larger than %1$d} — argument indices <em>and</em> a numeric
 * conversion. 35% of the catalog contains {@code %d}, and critically that 35% includes the
 * validator messages, which are the hot ones. {@code %d} applies the locale's zero digit, so it
 * cannot be replaced with {@code Long.toString} without changing output in locales that do not use
 * ASCII digits.</li>
 * </ul>
 *
 * The {@code ceiling*} benchmarks are hand-written concatenation producing the identical string.
 * They are not a proposed implementation — they are the floor, showing the most any amount of
 * pre-parsing could possibly recover. If the gap is small, the optimization is not worth the risk
 * of diverging from {@code String.format} across 24 shipped locales.
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=MessageFormatterBenchmark -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MessageFormatterBenchmark
{
    // core.properties: {@code required}
    private static final String SIMPLE = "You must provide a value for %s.";

    // core.properties: {@code max-integer}
    private static final String VALIDATOR = "%2$s requires a value no larger than %1$d.";

    private MessageFormatter simple;

    private MessageFormatter validator;

    private String label;

    private Long constraint;

    @Setup
    public void setup()
    {
        simple = new MessageFormatterImpl(SIMPLE, Locale.ENGLISH);
        validator = new MessageFormatterImpl(VALIDATOR, Locale.ENGLISH);

        label = "Order quantity";
        constraint = 250L;
    }

    @Benchmark
    public String currentSimple()
    {
        return simple.format(label);
    }

    @Benchmark
    public String currentValidator()
    {
        return validator.format(constraint, label);
    }

    /**
     * Hand-written equivalent of {@link #currentSimple()}: the floor, not a proposal
     */
    @Benchmark
    public String ceilingSimple()
    {
        return "You must provide a value for " + label + ".";
    }

    /**
     * Hand-written equivalent of {@link #currentValidator()}: the floor, not a proposal
     */
    @Benchmark
    public String ceilingValidator()
    {
        return label + " requires a value no larger than " + constraint + ".";
    }
}
