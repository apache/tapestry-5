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

import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.io.IOException;

/**
 * Certain kinds of resources can be minimized: this primarily refers to JavaScript and CSS, both of which contain
 * whitespace, comments and other features that can be reduced.
 *
 * The service configuration maps a MIME content type (e.g., "text/javascript") to an appropriate implementation of this
 * interface. The master service has the @{@link Primary} marker interface.
 * 
 * @since 5.3
 */
@UsesMappedConfiguration(ResourceMinimizer.class)
public interface ResourceMinimizer
{
    /**
     * Checks the {@linkplain StreamableResource#getContentType() content type} of the resource and applies an
     * appropriate minimization to it if possible.
     * 
     * @param resource
     *            to minimize
     * @return the same resource, or a minimized replacement for the resource
     * @throws IOException
     */
    StreamableResource minimize(StreamableResource resource) throws IOException;
}
