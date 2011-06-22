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

package org.apache.tapestry5.upload.internal.services;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;

import java.util.List;

/**
 * Holds single or multivalued values.
 */
public class ParameterValue
{
    private final List<String> values = newList();

    public static final ParameterValue NULL = new ParameterValue()
    {
        @Override
        public String single()
        {
            return null;
        }

        @Override
        public String[] multi()
        {
            return null;
        }
    };

    public ParameterValue(String value)
    {
        values.add(value);
    }

    public ParameterValue(String... values)
    {
        for (String v : values)
        {
            add(v);
        }
    }

    /**
     * @return Single value of parameter (or first value if there are multiple values)
     */
    public String single()
    {
        return values.get(0);
    }

    /**
     * @return All values of parameter
     */
    public String[] multi()
    {
        return values.toArray(new String[values.size()]);
    }

    public void add(String value)
    {
        values.add(value);
    }

    public boolean isMulti()
    {
        return values.size() > 1;
    }
}
