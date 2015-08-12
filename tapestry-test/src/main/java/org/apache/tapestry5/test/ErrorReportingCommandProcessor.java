// Copyright 2007, 2009, 2012 The Apache Software Foundation
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
 * A wrapper around a standard command processor that adds additional exception reporting when a
 * failure occurs.
 */
public class ErrorReportingCommandProcessor implements CommandProcessor
{
    private final CommandProcessor delegate;

    private final ErrorReporter errorReporter;

    public ErrorReportingCommandProcessor(CommandProcessor delegate, ErrorReporter errorReporter)
    {
        this.delegate = delegate;
        this.errorReporter = errorReporter;
    }

    private static final String BORDER = "**********************************************************************";

    private void reportError(String command, String[] args, RuntimeException ex)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("Selenium failure processing command ");
        builder.append(command);
        builder.append('(');

        for (int i = 0; i < args.length; i++)
        {
            if (i > 0)
                builder.append(", ");
            builder.append('"');
            builder.append(args[i]);
            builder.append('"');
        }

        builder.append("): ");
        builder.append(ex.toString());

        try
        {
            String logs = delegate.getString("retrieveLastRemoteControlLogs", new String[]{});

            if (logs != null && logs.length() > 0)
            {

                builder.append('\n');
                builder.append(BORDER);
                builder.append('\n');

                builder.append(logs);
            }

        } catch (Exception ex2)
        {
            // Skip the logs.
        }


        String report = builder.toString();

        System.err.println(BORDER);
        System.err.println(report);
        System.err.println(BORDER);

        errorReporter.writeErrorReport(report);
    }

    @Override
    public String doCommand(String command, String[] args)
    {
        try
        {
            return delegate.doCommand(command, args);
        } catch (RuntimeException ex)
        {
            reportError(command, args, ex);
            throw ex;
        }
    }

    @Override
    public boolean getBoolean(String string, String[] strings)
    {
        try
        {
            return delegate.getBoolean(string, strings);
        } catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    @Override
    public boolean[] getBooleanArray(String string, String[] strings)
    {
        try
        {
            return delegate.getBooleanArray(string, strings);
        } catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    @Override
    public Number getNumber(String string, String[] strings)
    {
        try
        {
            return delegate.getNumber(string, strings);
        } catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    @Override
    public Number[] getNumberArray(String string, String[] strings)
    {
        try
        {
            return delegate.getNumberArray(string, strings);
        } catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    @Override
    public String getString(String string, String[] strings)
    {
        try
        {
            return delegate.getString(string, strings);
        } catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    @Override
    public String[] getStringArray(String string, String[] strings)
    {
        try
        {
            return delegate.getStringArray(string, strings);
        } catch (RuntimeException ex)
        {
            reportError(string, strings, ex);
            throw ex;
        }
    }

    @Override
    public void start()
    {
        delegate.start();
    }

    @Override
    public void stop()
    {
        delegate.stop();
    }

    /**
     * @since 5.1.0.0
     */
    @Override
    public String getRemoteControlServerLocation()
    {
        return delegate.getRemoteControlServerLocation();
    }

    /**
     * @since 5.1.0.0
     */
    @Override
    public void setExtensionJs(String extensionJs)
    {
        delegate.setExtensionJs(extensionJs);
    }

    /**
     * @since 5.1.0.0
     */
    @Override
    public void start(String optionsString)
    {
        delegate.start(optionsString);
    }

    /**
     * @since 5.1.0.0
     */
    @Override
    public void start(Object optionsObject)
    {
        delegate.start(optionsObject);
    }
}
