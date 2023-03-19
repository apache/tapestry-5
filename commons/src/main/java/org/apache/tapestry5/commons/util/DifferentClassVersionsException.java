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

import org.apache.tapestry5.commons.internal.util.TapestryException;

/**
 * Exception used when trying to assemble a page but different versions of the same class are found.
 * 
 * @since 5.8.3
 */
public class DifferentClassVersionsException extends TapestryException
{
    
    private static final long serialVersionUID = 1L;
    
    private final String className;
    
    private final ClassLoader classLoader1;
    
    private final ClassLoader classLoader2;

    public DifferentClassVersionsException(String message, String className, ClassLoader classLoader1, ClassLoader classLoader2) 
    {
        super(message, null);
        this.className = className;
        this.classLoader1 = classLoader1;
        this.classLoader2 = classLoader2;
    }
    
    public String getClassName() 
    {
        return className;
    }
    
    public ClassLoader getClassLoader1() 
    {
        return classLoader1;
    }
    
    public ClassLoader getClassLoader2() 
    {
        return classLoader2;
    }
    
}
