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

import org.apache.tapestry5.dom.AbstractMarkupModel;
import org.apache.tapestry5.dom.DefaultMarkupModel;
import org.apache.tapestry5.dom.MarkupModel;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Output escaping, which runs over every text node and every attribute value of every response.
 *
 * {@link AbstractMarkupModel} holds two escapers written to different designs, and the comparison
 * between them is the point of this class:
 *
 * <ul>
 * <li>{@code encode}, used for text nodes, scans for a special character and, on finding one,
 * allocates a {@code StringBuilder} of twice the input length and copies the prefix in with
 * {@code substring} — a second allocation. From then on it appends character by character,
 * including all the characters that needed no escaping at all.</li>
 * <li>{@code encodeQuoted}, used for attribute values, appends into a buffer the caller already
 * owns and copies unescaped runs wholesale via {@code subSequence}, so it allocates nothing per
 * call and never walks a run one character at a time.</li>
 * </ul>
 *
 * If {@code encodeQuoted}'s design wins here, {@code encode} should adopt it.
 *
 * Comparing the two fairly takes care, and a first pass at this benchmark got it wrong twice:
 *
 * <ul>
 * <li>They do not escape the same characters. {@code encodeQuoted} also escapes the quote
 * character, so any input containing one gives it strictly more work to do and the comparison
 * silently stops being about the strategy. Every input below is therefore restricted to
 * {@code < > &}, which both escape identically.</li>
 * <li>They do not produce the same thing. On input needing no escaping at all, {@code encode}
 * returns the argument untouched while {@code encodeQuoted} still copies it into the buffer. The
 * {@code clean} pair is a comparison of two different jobs, and is here as context for the
 * escaping cases rather than as a race to be won.</li>
 * </ul>
 *
 * The input shapes matter more than the absolute numbers. Real markup is overwhelmingly
 * {@link #encodeClean()}: no special character at all, where {@code encode} allocates nothing and
 * only the scan costs anything. Escaping-heavy content is where the two designs diverge, and
 * {@link #encodeSparse()} against {@link #encodeQuotedSparse()} is the pair to read first,
 * because prose with the occasional ampersand is what most pages are made of.
 *
 * <pre>
 * ./gradlew :tapestry-core:jmh -Pjmh.include=MarkupEscaping -Pjmh.profilers=gc
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MarkupEscapingBenchmark
{
    private MarkupModel model;

    // None of these contains a quote or an apostrophe, so that encode and encodeQuoted escape
    // exactly the same characters and the two can be compared.

    /** No character needs escaping: the common case, and the one worth optimising for. */
    private String clean;

    /** Prose with an ampersand in it: the realistic escaping case. */
    private String sparse;

    /** Embedded markup that has to be shown rather than interpreted: the worst case. */
    private String dense;

    private StringBuilder buffer;

    @Setup
    public void setup()
    {
        model = new DefaultMarkupModel();

        clean = repeat("The quick brown fox jumps over the lazy dog. ", 4);
        sparse = repeat("Bread & butter, sold at less than 5 units. ", 4);
        dense = repeat("<span class=x>a &amp; b</span> ", 4);

        buffer = new StringBuilder(1024);
    }

    private static String repeat(String text, int times)
    {
        StringBuilder builder = new StringBuilder(text.length() * times);

        for (int i = 0; i < times; i++)
        {
            builder.append(text);
        }

        return builder.toString();
    }

    @Benchmark
    public String encodeClean()
    {
        return model.encode(clean);
    }

    @Benchmark
    public String encodeSparse()
    {
        return model.encode(sparse);
    }

    @Benchmark
    public String encodeDense()
    {
        return model.encode(dense);
    }

    @Benchmark
    public StringBuilder encodeQuotedClean()
    {
        buffer.setLength(0);
        model.encodeQuoted(clean, buffer);
        return buffer;
    }

    @Benchmark
    public StringBuilder encodeQuotedSparse()
    {
        buffer.setLength(0);
        model.encodeQuoted(sparse, buffer);
        return buffer;
    }

    @Benchmark
    public StringBuilder encodeQuotedDense()
    {
        buffer.setLength(0);
        model.encodeQuoted(dense, buffer);
        return buffer;
    }
}
