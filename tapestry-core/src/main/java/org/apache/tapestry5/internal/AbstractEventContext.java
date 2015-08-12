// Copyright 2010, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.EventContext;

public abstract class AbstractEventContext implements EventContext
{
    public String[] toStrings()
    {
        int count = getCount();

        String[] result = new String[count];

        for (int i = 0; i < count; i++)
        {
            result[i] = get(String.class, i);
        }

        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("<EventContext");

        String[] values = toStrings();

        String sep = ": ";

        for (int i = 0; i < values.length; i++)
        {
            builder.append(sep).append(values[i]);
            sep = ", ";
        }

        return builder.append('>').toString();
    }
}
