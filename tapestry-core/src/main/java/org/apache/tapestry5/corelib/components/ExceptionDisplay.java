// Copyright 2008-2013 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.ExceptionAnalysis;
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry5.ioc.services.ExceptionInfo;
import org.apache.tapestry5.services.StackTraceElementAnalyzer;
import org.apache.tapestry5.services.StackTraceElementClassConstants;

import java.util.List;

/**
 * Integral part of the default {@link org.apache.tapestry5.corelib.pages.ExceptionReport} page used to break apart and
 * display the properties of the exception.
 *
 * @tapestrydoc
 * @see org.apache.tapestry5.ioc.services.ExceptionAnalyzer
 */
@Import(stylesheet = "ExceptionDisplay.css", module = "t5/core/exception-display")
public class ExceptionDisplay
{
    /**
     * Exception to report.
     */
    @Parameter(required = true, allowNull = false)
    private Throwable exception;

    @Inject
    private ExceptionAnalyzer analyzer;

    @Property
    private ExceptionInfo info;

    @Property
    private String propertyName;

    @Property
    private StackTraceElement frame;

    @Property
    private List<ExceptionInfo> stack;

    private boolean sawDoFilter;

    @Inject
    @Primary
    private StackTraceElementAnalyzer frameAnalyzer;

    void setupRender()
    {
        ExceptionAnalysis analysis = analyzer.analyze(exception);

        stack = analysis.getExceptionInfos();
    }

    public Object getPropertyValue()
    {
        return info.getProperty(propertyName);
    }

    public String getFrameClass()
    {
        if (sawDoFilter)
        {
            return StackTraceElementClassConstants.OMITTED;
        }

        String result = frameAnalyzer.classForFrame(frame);

        sawDoFilter |= frame.getMethodName().equals("doFilter");

        return result;
    }

    public Object getLineNumberForFrame()
    {
        if (frame.getLineNumber() < 1)
        {
            return "";
        }

        return frame.getLineNumber();
    }

    public String getFramePackageName()
    {
        return splitClass()[0];
    }

    public String getFrameClassName()
    {
        return splitClass()[1];
    }

    private String[] splitClass()
    {
        String name = frame.getClassName();

        int dotx = name.lastIndexOf('.');

        // For generated classes in root package:
        if (dotx < 0)
        {
            return new String[]{"", name};
        }

        // For normal classes:

        return new String[]{
                name.substring(0, dotx),
                name.substring(dotx)
        };
    }
}

