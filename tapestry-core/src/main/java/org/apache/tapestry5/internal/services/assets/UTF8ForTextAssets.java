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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import java.io.IOException;

/**
 * Adds ;charset=utf-8 for text/* resources with no specific character set. This assumes that all test resources are
 * in UTF-8.
 *
 * @since 5.4
 */
public class UTF8ForTextAssets extends DelegatingSRS
{
    public UTF8ForTextAssets(StreamableResourceSource delegate)
    {
        super(delegate);
    }


    @Override
    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies) throws IOException
    {
        StreamableResource resource = delegate.getStreamableResource(baseResource, processing, dependencies);

        if (resource.getContentType().startsWith("text/")
                && !resource.getContentType().contains(";")
                && processing != StreamableResourceProcessing.FOR_AGGREGATION)
        {
            return resource.withContentType(resource.getContentType() + ";charset=utf-8");
        }

        return resource;
    }
}
