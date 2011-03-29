// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic;

/**
 * Used when converting a field into a property (that is, adding accessor methods for the field) to identify
 * which method(s) to create (a getter to access the value and/or a mutator to modify the value).
 * 
 * @see PlasticField#createAccessors(PropertyAccessType, String)
 */
public enum PropertyAccessType
{
    /** Create just a getter, not a mutator. */
    READ_ONLY,

    /** Create just a mutator, not a getter. */
    WRITE_ONLY,

    /** Create both a mutator and a getter. */
    READ_WRITE;
}
