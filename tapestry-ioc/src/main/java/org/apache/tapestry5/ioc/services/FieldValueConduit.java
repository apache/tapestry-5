// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.PlasticField;

/**
 * Used as a replacement for a field.
 *
 * @see PlasticField
 * @see FieldConduit
 * @since 5.2.0
 * @deprecated Deprecated in 5.3, using Plastic equivalents
 */
public interface FieldValueConduit
{

    /**
     * Reads the current value of the field.
     *
     * @return current value (possibly null)
     */
    Object get();

    /**
     * Sets the value of the field
     *
     * @param newValue to be captured
     */
    void set(Object newValue);

}
