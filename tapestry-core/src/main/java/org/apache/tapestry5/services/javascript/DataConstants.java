// Copyright 2012 The Apache Software Foundation
//
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
 * Constants related to client-side behaviors; generally, these are attributes added to elements on the server
 * side to trigger behaviors on the client-side.
 *
 * @since 5.4
 */
public class DataConstants
{
    /**
     * Attribute, set to "true" (or any non-null value) to indicate that the element should be notified about
     * form submission and validations events.
     */
    public static String VALIDATION_ATTRIBUTE = "data-validation";
}
