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
package org.apache.tapestry5.ioc.services;

import java.util.SequencedCollection;
import java.util.SequencedMap;

public interface Java18To21Service
{

    /**
     * Tests SequencedCollection as a return type (JEP 431).
     * @see https://openjdk.org/jeps/431
     */
    SequencedCollection<String> getSequencedCollection();

    /**
     * Tests SequencedMap as a return type (JEP 431).
     * @see https://openjdk.org/jeps/431
     */
    SequencedMap<String, Integer> getSequencedMap();

    /**
     * Tests record patterns in instanceof expressions (JEP 440).
     * @see https://openjdk.org/jeps/440
     */
    void recordPatternsInInstanceOf();

    /**
     * Tests record patterns in switch expressions (JEP 440).
     * @see https://openjdk.org/jeps/440
     */
    void recordPatternsInSwitch();

    /**
     * Tests pattern matching for switch with a sealed class hierarchy (JEP 441).
     * @see https://openjdk.org/jeps/441
     */
    void patternMatchingForSwitchWithSealedClass();

    /**
     * Tests pattern matching for switch with a sealed interface (JEP 441).
     * @see https://openjdk.org/jeps/441
     */
    void patternMatchingForSwitchWithSealedInterface();
}
