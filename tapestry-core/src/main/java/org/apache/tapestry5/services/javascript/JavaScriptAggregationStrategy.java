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

package org.apache.tapestry5.services.javascript;

/**
 * Used with {@link org.apache.tapestry5.services.javascript.JavaScriptStack} to identify how libraries and modules
 * within the stack can be aggregated.
 *
 * @since 5.4
 */
public enum JavaScriptAggregationStrategy
{
    /**
     * The default strategy is to combine all the assets and minimize them together.
     */
    COMBINE_AND_MINIMIZE,

    /**
     * Alternately, the assets can be combined, but not minimized (because some resources
     * do not support minimization).
     */
    COMBINE_ONLY,

    /**
     * The assets are not combined or minimized at all.
     */
    DO_NOTHING;

    public boolean enablesCombine() { return this != DO_NOTHING; }

    public boolean enablesMinimize() { return this == COMBINE_AND_MINIMIZE; }
}
