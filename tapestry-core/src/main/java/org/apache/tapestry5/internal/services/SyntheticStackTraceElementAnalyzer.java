// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.StackTraceElementAnalyzer;
import org.apache.tapestry5.services.StackTraceElementClassConstants;

/**
 * Identifies any code associated with line 1 as omitted (i.e., it's a synthetic
 * method related to an inner class).
 * 
 * @since 5.2.0
 */
public class SyntheticStackTraceElementAnalyzer implements StackTraceElementAnalyzer
{
    public String classForFrame(StackTraceElement frame)
    {
        return frame.getLineNumber() == 1 ? StackTraceElementClassConstants.OMITTED : null;
    }

}
