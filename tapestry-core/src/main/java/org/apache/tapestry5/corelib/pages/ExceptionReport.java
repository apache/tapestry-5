// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.ContentType;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ExceptionReporter;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Responsible for reporting runtime exceptions. This page is quite verbose and is usually overridden in a production
 * application. When {@link org.apache.tapestry5.SymbolConstants#PRODUCTION_MODE} is "true", it is very abbreviated.
 *
 * @see org.apache.tapestry5.corelib.components.ExceptionDisplay
 */
@ContentType("text/html")
public class ExceptionReport implements ExceptionReporter
{
    private static final String PATH_SEPARATOR_PROPERTY = "path.separator";

    // Match anything ending in .(something?)path.

    private static final Pattern PATH_RECOGNIZER = Pattern.compile("\\..*path$");

    @Property
    private String attributeName;

    @Inject
    @Property
    private Request request;

    @Inject
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    @Property(write = false)
    private boolean productionMode;

    @Inject
    @Symbol(SymbolConstants.TAPESTRY_VERSION)
    @Property(write = false)
    private String tapestryVersion;

    @Inject
    @Symbol(SymbolConstants.APPLICATION_VERSION)
    @Property(write = false)
    private String applicationVersion;

    @Property(write = false)
    private Throwable rootException;

    @Property
    private String propertyName;

    private final String pathSeparator = System.getProperty(PATH_SEPARATOR_PROPERTY);

    public void reportException(Throwable exception)
    {
        rootException = exception;
    }

    public boolean getHasSession()
    {
        return request.getSession(false) != null;
    }

    public Session getSession()
    {
        return request.getSession(false);
    }

    public Object getAttributeValue()
    {
        return getSession().getAttribute(attributeName);
    }

    /**
     * Returns a <em>sorted</em> list of system property names.
     */
    public List<String> getSystemProperties()
    {
        return InternalUtils.sortedKeys(System.getProperties());
    }

    public String getPropertyValue()
    {
        return System.getProperty(propertyName);
    }

    public boolean isComplexProperty()
    {
        return PATH_RECOGNIZER.matcher(propertyName).find() && getPropertyValue().contains(pathSeparator);
    }

    public String[] getComplexPropertyValue()
    {
        // Neither : nor ; is a regexp character.

        return getPropertyValue().split(pathSeparator);
    }
}
