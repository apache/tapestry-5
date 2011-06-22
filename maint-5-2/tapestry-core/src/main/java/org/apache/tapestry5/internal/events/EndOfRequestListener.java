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

package org.apache.tapestry5.internal.events;

/**
 * Event listener interface for objects that need to know when the current request finishes.
 *
 * @see org.apache.tapestry5.internal.services.EndOfRequestEventHub
 */
public interface EndOfRequestListener
{
    /**
     * Notified at the end of the request.  This notification occurs after the response has been sent to the client,
     * which means that it is to late to (for example) create a new HttpSession.
     */
    void requestDidComplete();
}
