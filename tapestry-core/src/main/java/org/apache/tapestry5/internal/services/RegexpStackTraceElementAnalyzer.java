// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.StackTraceElementAnalyzer;

import java.util.regex.Pattern;

/**
 * Uses a regular expression to identify which CSS class to apply to a frame. The frame's {@code toString()} is used. Uses
 * {@link java.util.regex.Matcher#find()} to search for a subsequence of the frame's description.
 *
 * @since 5.3
 */
public class RegexpStackTraceElementAnalyzer implements StackTraceElementAnalyzer
{
    private final Pattern pattern;

    private final String cssClass;

    public RegexpStackTraceElementAnalyzer(Pattern pattern, String cssClass)
    {
        this.pattern = pattern;
        this.cssClass = cssClass;
    }

    public String classForFrame(StackTraceElement frame)
    {
        if (pattern.matcher(frame.toString()).find())
        {
            return cssClass;
        }

        return null;
    }
}
