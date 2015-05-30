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

package org.apache.tapestry5.internal.services.assets

import org.testng.Assert
import org.testng.annotations.Test

class CompressionAnalyzerImplTests extends Assert  {

    @Test
    void non_match_yields_true() {

        def ca = new CompressionAnalyzerImpl(["image/png": false])

        assertEquals true, ca.isCompressable("text/plain")
    }

    @Test
    void attributes_are_stripped_off() {
        def ca = new CompressionAnalyzerImpl(["image/png": false])

        assertEquals false, ca.isCompressable("image/png;foo=bar")
    }

    @Test
    void searched_by_wildcard_if_not_found() {
        def ca = new CompressionAnalyzerImpl(["image/*": false])

        assertEquals false, ca.isCompressable("image/png")
    }

    @Test
    void most_specific_match_wins() {
        def ca = new CompressionAnalyzerImpl(["image/*": false, "image/svg" : true])

        assertEquals true, ca.isCompressable("image/svg")
    }
}
