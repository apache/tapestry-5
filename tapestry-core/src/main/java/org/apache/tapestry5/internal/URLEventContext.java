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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.services.ContextValueEncoder;

/**
 * Implementation based on values extracted from the URL (an event context, or a page activation context) that uses a
 * {@link org.apache.tapestry5.services.ContextValueEncoder} to convert from string values to the desired values.
 */
public class URLEventContext implements EventContext
{
    private final ContextValueEncoder valueEncoder;

    private final String[] values;

    public URLEventContext(ContextValueEncoder valueEncoder, String[] values)
    {
        this.valueEncoder = valueEncoder;
        this.values = values;
    }

    public int getCount()
    {
        return values == null ? 0 : values.length;
    }

    public <T> T get(Class<T> desiredType, int index)
    {
        return valueEncoder.toValue(desiredType, values[index]);
    }
}
