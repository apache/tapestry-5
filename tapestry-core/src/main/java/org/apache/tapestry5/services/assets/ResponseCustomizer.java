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

package org.apache.tapestry5.services.assets;

import java.io.IOException;

import org.apache.tapestry5.http.services.Response;

/**
 * Used to customize the response prior to streaming content to the client; typically this is used to
 * set special headers.
 *
 * @see org.apache.tapestry5.services.assets.StreamableResource#addResponseCustomizer(ResponseCustomizer)
 * @since 5.4
 */
public interface ResponseCustomizer
{
    /**
     * Invoked to customize the response; these are invoked in the order they are added to the
     * StreamableResource.
     */
    void customizeResponse(StreamableResource resource, Response response) throws IOException;
}
