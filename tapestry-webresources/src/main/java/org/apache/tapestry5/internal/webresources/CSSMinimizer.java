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

package org.apache.tapestry5.internal.webresources;

import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.slf4j.Logger;

import java.io.*;

/**
 * A wrapper around a customized YUI Compressor. This module does not have a dependency on YUICompressor;
 * instead a local copy of the YUICompressor CSS minimizer is kept (because the reset of YUICompressor
 * is painful to mix due to how it attempts to patch Rhino).
 */
public class CSSMinimizer extends AbstractMinimizer
{
    public CSSMinimizer(Logger logger, OperationTracker tracker, AssetChecksumGenerator checksumGenerator)
    {
        super(logger, tracker, checksumGenerator, "text/css");
    }

    @Override
    protected InputStream doMinimize(StreamableResource resource) throws IOException
    {
        InputStream is = resource.openStream();

        try
        {
            String compressed = CssCompressor.compress(is);
            return IOUtils.toInputStream(compressed);
        } finally
        {
            InternalUtils.close(is);
        }
    }
}
