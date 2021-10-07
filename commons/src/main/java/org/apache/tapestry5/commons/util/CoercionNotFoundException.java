// Copyright 2021 The Apache Software Foundation
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

package org.apache.tapestry5.commons.util;

import org.apache.tapestry5.commons.services.TypeCoercer;

/**
 * Exception used when {@link TypeCoercer} doesn't find a coercion from a type to another.
 * 
 * @since 5.8.0
 */
public class CoercionNotFoundException extends UnknownValueException
{
    
    private static final long serialVersionUID = 1L;

    final private Class<?> sourceType;
    
    final private Class<?> targetType;

    public CoercionNotFoundException(String message, AvailableValues availableValues, Class<?> sourceType, Class<?> targetType) 
    {
        super(message, availableValues);
        this.sourceType = sourceType;
        this.targetType = targetType;
    }
    
    /**
     * Returns the source type.
     */
    public Class<?> getSourceType() {
        return sourceType;
    }

    
    /**
     * Returns the target type.
     */
    public Class<?> getTargetType() {
        return targetType;
    }

}
