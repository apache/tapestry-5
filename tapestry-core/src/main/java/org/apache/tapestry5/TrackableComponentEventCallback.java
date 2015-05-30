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

package org.apache.tapestry5;

import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Traditional;

import java.io.IOException;

/**
 * Extends {@link ComponentEventCallback} with a way to determine if the underlying event has been aborted
 * due to a some event returning an acceptable, non-null value. The standard implementation of this
 * is a wrapper around either the {@linkplain Traditional traditional} or
 * {@linkplain org.apache.tapestry5.services.Ajax ajax} versions of the {@link ComponentEventResultProcessor}
 * service, i.e., they allow for a navigational result.
 *
 * Instances of this are made available via the {@link Environmental} annotation.
 * 
 * @since 5.2.0
 */
public interface TrackableComponentEventCallback<T> extends ComponentEventCallback<T>
{
    /**
     * Returns true if a return value from an event handler method was processed.
     */
    boolean isAborted();

    /**
     * If processing a return value threw an IOException, invoking this method will rethrow it.
     */
    void rethrow() throws IOException;
}
