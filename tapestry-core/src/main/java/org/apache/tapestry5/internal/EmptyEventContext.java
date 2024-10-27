// Copyright 2008, 2010 The Apache Software Foundation
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

import java.io.Serializable;
import java.util.Optional;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.commons.util.CommonsUtils;

/**
 * Placeholder used when no context is available.
 */
public class EmptyEventContext implements EventContext, Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Always returns zero.
     */
    public int getCount()
    {
        return 0;
    }

    /**
     * This should never be called because the count is always zero.
     */
    public <T> T get(Class<T> desiredType, int index)
    {
        return null;
    }

    public String[] toStrings()
    {
        return CommonsUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public <T> Optional<T> tryGet(Class<T> desiredType, int index)
    {
        return Optional.empty();
    }
}
