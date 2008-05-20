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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.internal.services.ComponentInvocation;

/**
 * Invokes a {@link org.apache.tapestry5.internal.services.ComponentInvocation}.
 */
public interface ComponentInvoker
{
    /**
     * @param invocation The ComponentInvocation object to be invoked.
     * @return The DOM created. Typically you will assert against it.
     */
    Document invoke(ComponentInvocation invocation);

}
