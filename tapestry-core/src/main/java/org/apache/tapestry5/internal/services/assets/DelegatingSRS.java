// Copyright 2012 The Apache Software Foundation
//
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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;

import java.io.IOException;
import java.util.Set;

/**
 * Base class for {@link StreamableResourceSource} implementations.
 *
 * @since 5.4
 */
public abstract class DelegatingSRS implements StreamableResourceSource
{
    protected final StreamableResourceSource delegate;

    protected DelegatingSRS(StreamableResourceSource delegate)
    {
        this.delegate = delegate;
    }

    public Set<String> fileExtensionsForContentType(ContentType contentType)
    {
        return delegate.fileExtensionsForContentType(contentType);
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies) throws IOException
    {
        return delegate.getStreamableResource(baseResource, processing, dependencies);
    }
}
