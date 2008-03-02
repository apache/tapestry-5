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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.annotations.GenerateAccessors;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.services.ExceptionAnalysis;
import org.apache.tapestry.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry.ioc.services.ExceptionInfo;

import java.util.List;

/**
 * Integral part of the default {@link org.apache.tapestry.corelib.pages.ExceptionReport} page used to break apart and
 * display the properties of the exception.
 *
 * @see org.apache.tapestry.ioc.services.ExceptionAnalyzer
 */
public class ExceptionDisplay
{
    /**
     * Exception to report.
     */
    @Parameter(required = true)
    private Throwable _exception;

    @Inject
    private ExceptionAnalyzer _analyzer;

    @Inject
    @Symbol(InternalConstants.TAPESTRY_APP_PACKAGE_PARAM)
    private String _appPackage;

    @GenerateAccessors
    private ExceptionInfo _info;

    @GenerateAccessors
    private String _propertyName;

    @GenerateAccessors
    private StackTraceElement _frame;

    @GenerateAccessors
    private List<ExceptionInfo> _stack;

    void setupRender()
    {
        ExceptionAnalysis analysis = _analyzer.analyze(_exception);

        _stack = analysis.getExceptionInfos();
    }

    public boolean getShowPropertyList()
    {
        // True if either is non-empty

        return !(_info.getPropertyNames().isEmpty() && _info.getStackTrace().isEmpty());
    }

    public Object getPropertyValue()
    {
        return _info.getProperty(_propertyName);
    }

    public String getFrameClass()
    {
        if (_frame.getClassName().startsWith(_appPackage) && _frame.getLineNumber() > 0) return "t-usercode-frame";

        return null;
    }
}
