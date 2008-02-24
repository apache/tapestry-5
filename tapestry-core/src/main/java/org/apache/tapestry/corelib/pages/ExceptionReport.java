// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.annotations.ContentType;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.services.ExceptionReporter;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Session;

/**
 * Responsible for reporting runtime exceptions. This page is quite verbose and is usually overridden in a production
 * application. When {@link org.apache.tapestry.TapestryConstants#PRODUCTION_MODE_SYMBOL} is "true", it is very
 * abbreviated.
 *
 * @see org.apache.tapestry.corelib.components.ExceptionDisplay
 */
@ContentType("text/html")
public class ExceptionReport implements ExceptionReporter
{
    private String _attributeName;

    @Inject
    private Request _request;

    @Inject
    @Symbol(TapestryConstants.PRODUCTION_MODE_SYMBOL)
    private boolean _productionMode;

    private Throwable _rootException;

    public void reportException(Throwable exception)
    {
        _rootException = exception;
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

    public boolean isProductionMode()
    {
        return _productionMode;
    }

    public Throwable getRootException()
    {
        return _rootException;
    }
}
