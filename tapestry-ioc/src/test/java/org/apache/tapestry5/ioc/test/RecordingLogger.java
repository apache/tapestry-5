// Copyright 2026 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.test;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

/**
 * A Logger that records every logging call, for tests that need to assert what was logged. Every level is
 * enabled, so nothing is filtered out before it can be recorded.
 */
public class RecordingLogger extends AbstractLogger
{
    private static final long serialVersionUID = 1L;

    private final List<Event> events = new ArrayList<>();

    /**
     * A single recorded logging call, with its message already formatted.
     */
    public static final class Event
    {
        private final Level level;

        private final String message;

        Event(Level level, String message)
        {
            this.level = level;
            this.message = message;
        }

        public Level getLevel()
        {
            return level;
        }

        public String getMessage()
        {
            return message;
        }

        @Override
        public String toString()
        {
            return level + " " + message;
        }
    }

    public RecordingLogger()
    {
        this.name = RecordingLogger.class.getName();
    }

    /**
     * All recorded events, in the order they were logged.
     */
    public List<Event> getEvents()
    {
        return new ArrayList<>(events);
    }

    /**
     * The formatted messages recorded at the given level, in the order they were logged.
     */
    public List<String> getMessages(Level level)
    {
        List<String> result = new ArrayList<>();

        for (Event event : events)
        {
            if (event.getLevel() == level)
            {
                result.add(event.getMessage());
            }
        }

        return result;
    }

    public void clear()
    {
        events.clear();
    }

    @Override
    protected String getFullyQualifiedCallerName()
    {
        return null;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments,
                                               Throwable throwable)
    {
        String message = arguments == null ? messagePattern : MessageFormatter.basicArrayFormat(messagePattern,
                arguments);

        events.add(new Event(level, message));
    }

    @Override
    public boolean isTraceEnabled()
    {
        return true;
    }

    @Override
    public boolean isTraceEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public boolean isDebugEnabled()
    {
        return true;
    }

    @Override
    public boolean isDebugEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public boolean isInfoEnabled()
    {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public boolean isWarnEnabled()
    {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker)
    {
        return true;
    }

    @Override
    public boolean isErrorEnabled()
    {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker)
    {
        return true;
    }
}
