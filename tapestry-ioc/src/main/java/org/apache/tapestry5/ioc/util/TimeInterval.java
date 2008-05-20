// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to represent a period of time, specifically as a configuration value. This is often used to specify timeouts.
 * <p/>
 * TimePeriods are parsed from strings.
 * <p/>
 * The string specifys a number of terms. The values of all the terms are summed together to form the total time period.
 * Each term consists of a number followed by a unit. Units (from largest to smallest) are: <dl> <dt>y <dd>year <dt>d
 * <dd>day <dt>h <dd>hour <dt>m <dd>minute <dt>s <dd>second <dt>ms <dd>millisecond </dl> <p>  Example: "2 h 30 m". By
 * convention, terms are specified largest to smallest.  A term without a unit is assumed to be milliseconds.  Units are
 * case insensitive ("h" or "H" are treated the same).
 */
public class TimeInterval
{
    private static final Map<String, Long> UNITS = CollectionFactory.newCaseInsensitiveMap();

    private static final long MILLISECOND = 1000l;

    static
    {
        UNITS.put("ms", 1l);
        UNITS.put("s", MILLISECOND);
        UNITS.put("m", 60 * MILLISECOND);
        UNITS.put("h", 60 * UNITS.get("m"));
        UNITS.put("d", 24 * UNITS.get("h"));
        UNITS.put("y", 365 * UNITS.get("d"));
    }

    private static final Pattern PATTERN = Pattern.compile("\\s*(\\d+)\\s*([a-z]*)", Pattern.CASE_INSENSITIVE);

    private final long milliseconds;

    /**
     * Creates a TimeInterval for a string.
     *
     * @param input the string specifying the amount of time in the period
     */
    public TimeInterval(String input)
    {
        milliseconds = parseMilliseconds(input);
    }

    public long milliseconds()
    {
        return milliseconds;
    }

    public long seconds()
    {
        return milliseconds / MILLISECOND;
    }

    static long parseMilliseconds(String input)
    {
        long milliseconds = 0l;

        Matcher matcher = PATTERN.matcher(input);

        matcher.useAnchoringBounds(true);

        // TODO: Notice non matching characters and reject input, including at end

        int lastMatchEnd = -1;

        while (matcher.find())
        {
            int start = matcher.start();

            if (lastMatchEnd + 1 < start)
            {
                String invalid = input.substring(lastMatchEnd + 1, start);
                throw new RuntimeException(UtilMessages.invalidTimeIntervalInput(invalid, input));
            }

            lastMatchEnd = matcher.end();

            long count = Long.parseLong(matcher.group(1));
            String units = matcher.group(2);

            if (units.length() == 0)
            {
                milliseconds += count;
                continue;
            }

            Long unitValue = UNITS.get(units);

            if (unitValue == null)
                throw new RuntimeException(UtilMessages.invalidTimeIntervalUnit(units, input, UNITS.keySet()));

            milliseconds += count * unitValue;
        }

        if (lastMatchEnd + 1 < input.length())
        {
            String invalid = input.substring(lastMatchEnd + 1);
            throw new RuntimeException(UtilMessages.invalidTimeIntervalInput(invalid, input));
        }

        return milliseconds;
    }

    @Override
    public String toString()
    {
        return String.format("TimeInterval[%d ms]", milliseconds);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (obj instanceof TimeInterval)
        {
            TimeInterval tp = (TimeInterval) obj;

            return milliseconds == tp.milliseconds;
        }

        return false;
    }
}
