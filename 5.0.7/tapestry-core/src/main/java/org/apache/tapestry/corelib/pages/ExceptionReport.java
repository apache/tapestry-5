// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.corelib.pages;

import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.services.ExceptionAnalysis;
import org.apache.tapestry.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry.ioc.services.ExceptionInfo;
import org.apache.tapestry.services.ExceptionReporter;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Session;

import java.util.List;

/**
 * Responsible for reporting runtime exceptions. This page is quite verbose and is usually
 * overridden in a production application.
 */
public class ExceptionReport implements ExceptionReporter
{
    private List<ExceptionInfo> _stack;

    private ExceptionInfo _info;

    private String _propertyName;

    private String _frame;

    private String _attributeName;

    @Inject
    private ExceptionAnalyzer _analyzer;

    @Inject
    private Request _request;

    public void reportException(Throwable exception)
    {
        ExceptionAnalysis analysis = _analyzer.analyze(exception);

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

    public String getFrame()
    {
        return _frame;
    }

    public void setFrame(String frame)
    {
        _frame = frame;
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

    public boolean getHasSession()
    {
        return _request.getSession(false) != null;
    }

    public Request getRequest()
    {
        return _request;
    }

    public Session getSession()
    {
        return _request.getSession(false);
    }

    public String getAttributeName()
    {
        return _attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        _attributeName = attributeName;
    }

    public Object getAttributeValue()
    {
        return getSession().getAttribute(_attributeName);
    }
}
