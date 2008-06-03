// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

class UtilMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(UtilMessages.class);

    private UtilMessages()
    {
    }

    static String noReturnValueAccepted(String eventType, String componentId, Object returnValue)
    {
        return MESSAGES.format("no-return-value-accepted", eventType, componentId, String
                .valueOf(returnValue));
    }
}
