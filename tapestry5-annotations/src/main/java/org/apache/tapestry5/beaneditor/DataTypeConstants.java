// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.beaneditor;

/**
 * Defines the names of data types used to select an editor (or display) block for a property.
 *
 * @see DataType
 * @since 5.3
 */
public class DataTypeConstants
{
    /**
     * Used for {@link String} properties.
     */
    public static final String TEXT = "text";

    /**
     * Used for properties of {@link Number} type.
     */
    public static final String NUMBER = "number";

    /**
     * Used for {@link Enum}s.
     */
    public static final String ENUM = "enum";

    /**
     * Used for boolean properties.
     */
    public static final String BOOLEAN = "boolean";

    /**
     * Used to render a JavaScript calendar for a {@link java.util.Date} property.
     */
    public static final String DATE = "date";

    /**
     * Used to render a JavaScript calendar for a {@link java.util.Calendar} property.
     */
    public static final String CALENDAR = "calendar";

    /**
     * Used to render a password field for a {@link String} property.
     */
    public static final String PASSWORD = "password";

    /**
     * Used to render a textarea field for a {@link String} property.
     */
    public static final String LONG_TEXT = "longtext";

}
