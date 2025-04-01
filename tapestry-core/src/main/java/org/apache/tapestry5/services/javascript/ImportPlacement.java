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
 * Enumeration class defining the possible placements of JavaScript imports.
 *
 * @since 5.10.0
 */
public enum ImportPlacement
{
    /**
     * Inside the <code>&lt;head&gt;</code> HTML element.
     */
    HEAD,
    
    /**
     * Towards the top of the <code>&lt;body&gt;</code> HTML element.
     */
    BODY_TOP,

    /**
     * Towards the bottom of the <code>&lt;body&gt;</code> HTML element.
     */
    BODY_BOTTOM

}
