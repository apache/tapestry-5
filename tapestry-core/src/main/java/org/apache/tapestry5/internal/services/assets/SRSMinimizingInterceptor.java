// Copyright 2011, 2012 The Apache Software Foundation
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
import org.apache.tapestry5.services.assets.*;

import java.io.IOException;

/**
 * Loops the result through the {@link ResourceMinimizer} service.
 */
public class SRSMinimizingInterceptor extends DelegatingSRS
{
    private final ResourceMinimizer minimizer;

    public SRSMinimizingInterceptor(StreamableResourceSource delegate, ResourceMinimizer minimizer)
    {
        super(delegate);
        this.minimizer = minimizer;
    }

    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies)
            throws IOException
    {
        StreamableResource streamable = delegate.getStreamableResource(baseResource, processing, dependencies);

        if (processing != StreamableResourceProcessing.FOR_AGGREGATION)
        {
            return minimizer.minimize(streamable);
        }

        return streamable;
    }

}
