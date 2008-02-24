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

import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.ioc.annotations.Inject;
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

    private ExceptionInfo _info;

    private String _propertyName;

    private List<ExceptionInfo> _stack;

    void setupRender()
    {
        ExceptionAnalysis analysis = _analyzer.analyze(_exception);

        _stack = analysis.getExceptionInfos();
    }

    public List<ExceptionInfo> getStack()
    {
        return _stack;
    }

    public ExceptionInfo getInfo()
    {
        return _info;
    }

    public void setInfo(ExceptionInfo info)
    {
        _info = info;
    }

    public String getPropertyName()
    {
        return _propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        _propertyName = propertyName;
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
}
