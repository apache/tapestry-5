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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.tapestry5.commons.util.CaseInsensitiveMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * {@link CaseInsensitiveMap}, which carries request parameters, headers, component parameters and
 * every string-to-enum coercion in the framework through {@code StringToEnumCoercion}.
 *
 * It does not lower-case keys and cache the result. Instead every lookup walks the key character
 * by character through {@code Character.toLowerCase} to compute a case-insensitive hash, then
 * binary-searches and confirms with {@code equalsIgnoreCase}. That trades allocation for
 * repeated work, which is the right trade for a map written once and read once, and the wrong
 * one for a map read on every request with the same handful of keys.
 *
 * The {@link HashMap} benchmarks are the control. They are not a fair substitute, as they are
 * case-sensitive, which is the whole point of the class. But they establish what the lookup
 * would cost without the case handling, and therefore what the case-insensitivity is being paid
 * for.
 *
 * Key length matters here in a way it does not for a {@code HashMap}, whose hash is cached on the
 * {@link String}. {@link #getLongKey()} against {@link #getShortKey()} shows how steeply.
 *
 * <pre>
 * ./gradlew :commons:jmh -Pjmh.include=CaseInsensitiveMap
 * </pre>
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CaseInsensitiveMapBenchmark
{
    private static final String SHORT_KEY = "id";

    private static final String LONG_KEY = "content-security-policy-report-only";

    private static final String MIXED_CASE_KEY = "Content-Type";

    private Map<String, String> caseInsensitive;

    private Map<String, String> hashMap;

    @SuppressWarnings("unchecked")
    @Setup
    public void setup()
    {
        caseInsensitive = new CaseInsensitiveMap<>();
        hashMap = new HashMap<>();

        // Roughly the size of a request's header or parameter map.
        for (Map<String, String> map : new Map[]{ caseInsensitive, hashMap })
        {
            map.put(SHORT_KEY, "value");
            map.put(LONG_KEY, "value");
            map.put(MIXED_CASE_KEY, "text/html");
            map.put("accept", "*/*");
            map.put("accept-encoding", "gzip");
            map.put("user-agent", "benchmark");
            map.put("host", "localhost");
            map.put("connection", "keep-alive");
        }
    }

    @Benchmark
    public String getShortKey()
    {
        return caseInsensitive.get(SHORT_KEY);
    }

    @Benchmark
    public String getLongKey()
    {
        return caseInsensitive.get(LONG_KEY);
    }

    @Benchmark
    public String getMixedCaseKey()
    {
        return caseInsensitive.get("content-type");
    }

    @Benchmark
    public String getMissingKey()
    {
        return caseInsensitive.get("x-not-present");
    }

    @Benchmark
    public String hashMapShortKey()
    {
        return hashMap.get(SHORT_KEY);
    }

    @Benchmark
    public String hashMapLongKey()
    {
        return hashMap.get(LONG_KEY);
    }
}
