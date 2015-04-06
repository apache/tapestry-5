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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;

import java.io.IOException;

/**
 * Responsible for handling the response for a page called with an unknown activation context when the check itself is
 * enabled.
 * The default implementation is to respond with a 404 NOT FOUND.
 *
 * @see <a href="http://issues.apache.org/jira/browse/TAP5-2070">TAP5-2070</a>
 * @see org.apache.tapestry5.annotations.UnknownActivationContextCheck
 * @see org.apache.tapestry5.services.HttpError
 *
 * @since 5.4
 */
public interface UnknownActivationContextHandler
{
    /**
     * Answer the client in the case of a request coming in with an unknown activation context.
     */
    @SuppressWarnings("unchecked")
    void handleUnknownContext(ComponentResources pageResources, EventContext activationContext)
                            throws IOException;
}
