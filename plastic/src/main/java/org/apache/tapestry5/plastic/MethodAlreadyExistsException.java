// Copyright 2024 The Apache Software Foundation
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
 * Exception raised when there's an attempt to create a method which
 * already exists in a class.
 * 
 * This extends {@linkplain IllegalArgumentException} for backward compatibility.
 * 
 * @since 5.9.0
 */
public class MethodAlreadyExistsException extends IllegalArgumentException
{

    private static final long serialVersionUID = 1L;

    public MethodAlreadyExistsException(String message) 
    {
        super(message);
    }
    
}
