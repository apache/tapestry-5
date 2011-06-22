// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.StackTraceElementAnalyzer;
import org.apache.tapestry5.services.StackTraceElementClassConstants;

/**
 * Encapsulates a number of tests for identifying stack frames that are a side-effect
 * of various Tapestry Aspect Oriented Programming and other code generation behaviors.
 * 
 * @since 5.2.0
 */
public class TapestryAOPStackFrameAnalyzer implements StackTraceElementAnalyzer
{
    private static final String[] SYNTHETIC_METHOD_PREFIXES = new String[]
    { "_$get_", "_$set_", "_$readaccess_", "_$writeaccess_" };

    public String classForFrame(StackTraceElement frame)
    {
        if (omit(frame))
            return StackTraceElementClassConstants.OMITTED;

        return null;
    }

    private boolean omit(StackTraceElement frame)
    {
        // $FieldAccess class in root package is generated

        if (frame.getClassName().startsWith("$FieldAccess_"))
            return true;

        if (frame.getMethodName().equals("invoke") && frame.getClassName().contains("$MethodAccess_"))
            return true;

        if (frame.getMethodName().equals("invokeAdvisedMethod") && frame.getClassName().contains("$invocation_"))
            return true;

        for (String prefix : SYNTHETIC_METHOD_PREFIXES)
        {
            if (frame.getMethodName().startsWith(prefix))
                return true;
        }

        return false;
    }

}
