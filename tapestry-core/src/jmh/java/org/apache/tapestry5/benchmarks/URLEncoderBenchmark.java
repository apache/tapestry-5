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

import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.internal.services.URLEncoderImpl;
import org.apache.tapestry5.services.URLEncoder;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * URL-safe encoding of event context values, which runs for every value in every link a page
 * renders. A grid of a hundred rows with an action link per row encodes at least a hundred times.
 *
 * {@code URLEncoderImpl.encode} has the same shape as the escaping problem in
 * {@link MarkupEscapingBenchmark}, and slightly worse:
 *
 * <ul>
 * <li>It allocates a {@code StringBuilder} of twice the input length <em>unconditionally</em>,
 * before it knows whether anything needs encoding at all — and then, if nothing did, throws it
 * away and returns the original string. {@code AbstractMarkupModel.encode} at least waits until
 * it has seen a character that needs escaping.</li>
 * <li>Each character that does need encoding goes through
 * {@code String.format("$%04x", chAsInt)}, which parses a format string, allocates a
 * {@code Formatter} and boxes the argument, to produce four hex digits.</li>
 * </ul>
 *
 * {@link #encodeClean()} is the case that matters. Page names, event names and identifier-like
 * context values are overwhelmingly made of safe characters, so the common path is the one that
 * allocates a buffer it does not use. Whatever that costs is pure waste, and
 * {@link #encodeClean()} against {@link #encodeDirty()} says how much of the total it is.
 *
 * Read this one with {@code -Pjmh.profilers=gc}; the allocation per operation is the finding.
 *
 * <pre>
 * ./gradlew :tapestry-core:jmh -Pjmh.include=URLEncoder -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class URLEncoderBenchmark
{
    private URLEncoder encoder;

    // Nothing to encode: a page name, an event name, a numeric id
    private String clean;

    // A handful of characters need encoding: a name, a path, a formatted date
    private String dirty;

    private String encodedClean;

    private String encodedDirty;

    @Setup
    public void setup()
    {
        encoder = new URLEncoderImpl();

        clean = "customer.overview:activate-4711";
        dirty = "Müller & Sons, Ltd. / Hamburg";

        encodedClean = encoder.encode(clean);
        encodedDirty = encoder.encode(dirty);
    }

    @Benchmark
    public String encodeClean()
    {
        return encoder.encode(clean);
    }

    @Benchmark
    public String encodeDirty()
    {
        return encoder.encode(dirty);
    }

    @Benchmark
    public String decodeClean()
    {
        return encoder.decode(encodedClean);
    }

    @Benchmark
    public String decodeDirty()
    {
        return encoder.decode(encodedDirty);
    }
}
