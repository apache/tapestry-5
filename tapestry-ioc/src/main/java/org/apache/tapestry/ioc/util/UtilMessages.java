// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.util;

import java.util.Collection;

import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.MessagesImpl;

/**
 * Static messages
 */
class UtilMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(UtilMessages.class);

    private UtilMessages()
    {
    }

    static String noStrategyAdapter(Class inputType, Class adapterType, Collection<String> catalog)
    {
        return MESSAGES.format(
                "no-strategy-adapter",
                inputType.getName(),
                adapterType.getName(),
                InternalUtils.joinSorted(catalog));
    }

    static String stackIsEmpty()
    {
        return MESSAGES.get("stack-is-empty");
    }
}
