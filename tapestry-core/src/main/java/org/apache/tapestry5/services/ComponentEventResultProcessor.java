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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.io.IOException;

/**
 * Responsible for handling the return value provided by a component event handler method.
 *
 * There are two services built into Tapestry that implement this interface: ComponentEventResultProcessor (used for
 * ordinary page-oriented requests, and distinguished by the @{@link org.apache.tapestry5.services.Traditional}
 * and/or @{@link org.apache.tapestry5.ioc.annotations.Primary} marker annotations) and
 * AjaxComponentEventResultProcessor, used
 * for Ajax requests (which typically return a partially rendered page), distinguished by the @{@link
 * org.apache.tapestry5.services.Ajax} marker annotation.
 *
 * @param <T> the type of the values being handled
 */
@UsesMappedConfiguration(key = Class.class, value = ComponentEventResultProcessor.class)
public interface ComponentEventResultProcessor<T>
{
    /**
     * For a given, non-null return value from a component event method, construct and send a response.
     *
     * Starting in release 5.4, it is recommended that for any response that involves Tapestry pages or components,
     * the implementation should create an {@link org.apache.tapestry5.ioc.IOOperation} to do the rendering, and
     * add the operation to the {@link org.apache.tapestry5.http.services.Request} as attribute
     * {@link org.apache.tapestry5.TapestryConstants#RESPONSE_RENDERER}.
     * This avoids a number of issues related to the {@link org.apache.tapestry5.services.Environment}.
     *
     * @param value
     *         the value returned from a method
     * @throws RuntimeException
     *         if the value can not handled
     */
    void processResultValue(T value) throws IOException;
}
