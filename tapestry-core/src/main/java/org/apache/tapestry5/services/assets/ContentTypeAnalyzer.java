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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used to determine the MIME content type for a resource. The service configuration is the first step,
 * followed by {@link Context#getMimeType(String)}, and then (finally) "application/octet-stream"
 * as a stop-gap.
 *
 * The service configuration maps the file extension (e.g., "png") to its corresponding MIME type (e.g., "image/png");
 */
@UsesMappedConfiguration(String.class)
public interface ContentTypeAnalyzer
{
    /**
     * Analyze the resource to determine its content type.
     * 
     * @param resource
     *            to analyze
     * @return a MIME content type
     */
    String getContentType(Resource resource);
}
