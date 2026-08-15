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

package plasticbenchmarks.subjects;

/**
 * A stand-in for a Tapestry page class: private fields that a real transformation would turn into
 * conduit-backed properties, and methods that it would advise.
 *
 * The package is controlled by the {@code PlasticManager} of
 * {@code org.apache.tapestry5.plastic.benchmarks.PlasticTransformationBenchmark}, so every class
 * in it is transformed on load. It sits outside {@code org.apache.tapestry5} for the same reason
 * the existing Plastic tests use {@code testsubjects}: the controlled package must not overlap
 * with Plastic's own.
 */
public class BenchmarkSubject
{
    private String name;

    private int count;

    private boolean active;

    private long timestamp;

    private Object payload;

    // No getters or setters here on purpose: the transformation generates accessors for every
    // field, and Plastic refuses to overwrite one that already exists. The methods below are
    // the ones the transformation advises.

    public String describe()
    {
        return name + ":" + count;
    }

    public int increment(int amount)
    {
        count += amount;
        return count;
    }

    public boolean toggle()
    {
        active = !active;
        return active;
    }

    public long stamp(long value)
    {
        timestamp = value;
        return timestamp;
    }

    public Object exchange(Object value)
    {
        Object previous = payload;
        payload = value;
        return previous;
    }

    public void reset()
    {
        name = null;
        count = 0;
        active = false;
        timestamp = 0L;
        payload = null;
    }
}
