// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.dom;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

final class DomMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(DomMessages.class);


    static String noRootElement()
    {
        return MESSAGES.get("no-root-element");
    }

    static String namespaceURINotMappedToPrefix(String namespace)
    {
        return MESSAGES.format("namespace-uri-not-mapped-to-prefix", namespace);
    }
}
