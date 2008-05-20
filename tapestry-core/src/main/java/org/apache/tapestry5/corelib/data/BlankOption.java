// Copyright 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.corelib.data;

/**
 * Used with the {@link org.apache.tapestry5.corelib.components.Select} component to control whether an initial blank
 * option is supplied.  Determines the optionality of the undelrying property from the Select's {@linkplain
 * org.apache.tapestry5.FieldValidator#isRequired() validate parameter}
 */
public enum BlankOption
{
    /**
     * Always include the blank option, even if the underlying property is required.
     */
    ALWAYS,

    /**
     * Never include the blank option, even if the underlying property is optional.
     */
    NEVER,

    /**
     * The default: include the blank option if the underlying property is optional.
     */
    AUTO;
}
