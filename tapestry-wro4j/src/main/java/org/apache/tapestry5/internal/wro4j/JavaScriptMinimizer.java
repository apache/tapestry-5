// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.wro4j;

import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.wro4j.services.ResourceProcessor;
import org.apache.tapestry5.wro4j.services.ResourceProcessorSource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class JavaScriptMinimizer extends AbstractMinimizer
{
    private final ResourceProcessor processor;

    public JavaScriptMinimizer(Logger logger, OperationTracker tracker, AssetChecksumGenerator checksumGenerator, ResourceProcessorSource processorSource)
    {
        super(logger, tracker, checksumGenerator, "text/javascript");

        processor = processorSource.getProcessor("JavaScriptMinimizer");
    }

    @Override
    protected InputStream doMinimize(StreamableResource resource) throws IOException
    {
        return processor.process("Minimizing " + resource, resource.getDescription(), resource.openStream(), "text/javascript");
    }
}
