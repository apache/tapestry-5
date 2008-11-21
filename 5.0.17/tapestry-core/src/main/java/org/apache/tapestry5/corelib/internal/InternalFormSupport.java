//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.services.FormSupport;

/**
 * Additional methods for {@link org.apache.tapestry5.services.FormSupport} used internally by Tapestry.
 *
 * @see org.apache.tapestry5.corelib.components.Form
 * @see org.apache.tapestry5.corelib.components.FormInjector
 */
public interface InternalFormSupport extends FormSupport
{
    /**
     * Executes any deferred callbacks added via {@link org.apache.tapestry5.services.FormSupport#defer(Runnable)}.
     */
    void executeDeferred();

    /**
     * Returns the form encoding type, if one has been set via a call to {@link org.apache.tapestry5.services.FormSupport#setEncodingType(String)}.
     */
    String getEncodingType();
}
