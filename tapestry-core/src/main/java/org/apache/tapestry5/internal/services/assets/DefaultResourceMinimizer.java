// Copyright 2011 The Apache Software Foundation
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

import java.io.IOException;

import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.assets.StreamableResource;

/**
 * Default implementation that simply returns the resource unchanged.
 */
public class DefaultResourceMinimizer implements ResourceMinimizer
{
    /** Does nothing; an override of this service can be installed to provide minimization. */
    public StreamableResource minimize(StreamableResource resource) throws IOException
    {
        return resource;
    }
}
