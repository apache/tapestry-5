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

package org.apache.tapestry5.ioc.internal.services;

/**
 * Used to test {@link org.apache.tapestry5.ioc.internal.services.ExceptionAnalyzerImpl} against an exception that has a
 * write-only property.
 */
public class WriteOnlyPropertyException extends Exception
{
    private String code;

    public String getCode()
    {
        return code;
    }

    public void setFaultCode(int code)
    {
        this.code = String.format("%04d", code);
    }
}
