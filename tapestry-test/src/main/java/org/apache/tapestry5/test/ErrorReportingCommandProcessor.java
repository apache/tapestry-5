// Copyright 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.test;

import com.thoughtworks.selenium.CommandProcessor;

/**
 * A wrapper around a standard command processor that adds additional exception reporting when a failure occurs.
 */
public class ErrorReportingCommandProcessor implements CommandProcessor
{
    private final CommandProcessor delegate;

    public ErrorReportingCommandProcessor(final CommandProcessor delegate)
    {
        this.delegate = delegate;
    }

    private static final String BORDER = "**********************************************************************";

    private void reportError(String command, String[] args, RuntimeException ex)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(BORDER);
        builder.append("\nSelenium failure processing command ");
        builder.append(command);
        builder.append("(");

        for (int i = 0; i < args.length; i++)
        {
            if (i > 0) builder.append(", ");
            builder.append('"');
            builder.append(args[i]);
            builder.append('"');
        }

        builder.append("): ");
        builder.append(ex.toString());

        builder.append("\n\nPage source:\n\n");

        builder.append(delegate.getString("getHtmlSource", new String[] { }));

        builder.append("\n");
        builder.append(BORDER);

        System.err.println(builder.toString());
    }

    public String doCommand(String command, String[] args)
    {
        try
        {
            return delegate.doCommand(command, args);
        }
        catch (RuntimeException ex)
        {
            reportError(command, args, ex);
            throw ex;
        }
    }

    public boolean getBoolean(String string, String[] strings)
    {
        try
        {
            return delegate.getBoolean(string, strings);
        }
        catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    public boolean[] getBooleanArray(String string, String[] strings)
    {
        try
        {
            return delegate.getBooleanArray(string, strings);
        }
        catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    public Number getNumber(String string, String[] strings)
    {
        try
        {
            return delegate.getNumber(string, strings);
        }
        catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    public Number[] getNumberArray(String string, String[] strings)
    {
        try
        {
            return delegate.getNumberArray(string, strings);
        }
        catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    public String getString(String string, String[] strings)
    {
        try
        {
            return delegate.getString(string, strings);
        }
        catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    public String[] getStringArray(String string, String[] strings)
    {
        try
        {
            return delegate.getStringArray(string, strings);
        }
        catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    public void start()
    {
        delegate.start();
    }

    public void stop()
    {
        delegate.stop();
    }

    /**
     * @since 5.1.0.0
     */
    public String getRemoteControlServerLocation()
    {
        return delegate.getRemoteControlServerLocation();
    }

    /**
     * @since 5.1.0.0
     */
    public void setExtensionJs(String extensionJs)
    {
        delegate.setExtensionJs(extensionJs);
    }

    /**
     * @since 5.1.0.0
     */
    public void start(String optionsString)
    {
        delegate.start(optionsString);
    }

    /**
     * @since 5.1.0.0
     */
    public void start(Object optionsObject)
    {
        delegate.start(optionsObject);
    }
}
