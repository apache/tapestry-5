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

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.StackTraceElementAnalyzer;
import org.apache.tapestry5.services.StackTraceElementClassConstants;

/**
 * Identifies frames for application classes.
 *
 * @since 5.1.0.0
 */
public class ApplicationStackTraceElementAnalyzer implements StackTraceElementAnalyzer
{
    private final String appPackage;

    public ApplicationStackTraceElementAnalyzer(
            @Inject @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
            String appPackage)
    {
        this.appPackage = appPackage;
    }

    public String classForFrame(StackTraceElement frame)
    {
        return frame.getClassName().startsWith(appPackage) && frame.getLineNumber() > 0
               ? StackTraceElementClassConstants.USER_CODE
               : null;
    }
}
