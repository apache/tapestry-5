// Copyright 2010-2013 The Apache Software Foundation
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

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.ResourceStreamer;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseCompressionAnalyzer;
import org.apache.tapestry5.services.assets.AssetRequestHandler;
import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.apache.tapestry5.services.javascript.JavaScriptStackSource;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackAssetRequestHandler implements AssetRequestHandler
{
    private final LocalizationSetter localizationSetter;

    private final ResponseCompressionAnalyzer compressionAnalyzer;

    private final ResourceStreamer resourceStreamer;

    // Group 1: checksum
    // Group 2: locale
    // Group 3: path
    private final Pattern pathPattern = Pattern.compile("^(.+)/(.+)/(.+)\\.js$");

    private final OperationTracker tracker;

    private final JavaScriptStackAssembler javaScriptStackAssembler;

    public StackAssetRequestHandler(LocalizationSetter localizationSetter,
                                    ResponseCompressionAnalyzer compressionAnalyzer,
                                    ResourceStreamer resourceStreamer,
                                    OperationTracker tracker,
                                    JavaScriptStackAssembler javaScriptStackAssembler)
    {
        this.localizationSetter = localizationSetter;
        this.compressionAnalyzer = compressionAnalyzer;
        this.resourceStreamer = resourceStreamer;
        this.tracker = tracker;
        this.javaScriptStackAssembler = javaScriptStackAssembler;
    }

    public boolean handleAssetRequest(Request request, Response response, final String extraPath) throws IOException
    {
        tracker.perform(String.format("Streaming asset stack %s", extraPath),
                new org.apache.tapestry5.ioc.IOOperation<Void>()
                {
                    public Void perform() throws IOException
                    {
                        boolean compress = compressionAnalyzer.isGZipSupported();

                        StreamableResource resource = getResource(extraPath, compress);

                        resourceStreamer.streamResource(resource);

                        return null;
                    }
                });

        return true;
    }

    private StreamableResource getResource(String extraPath, boolean compressed) throws IOException
    {
        Matcher matcher = pathPattern.matcher(extraPath);

        if (!matcher.matches())
        {
            throw new RuntimeException("Invalid path for a stack asset request.");
        }

        // TODO: Extract the stack's aggregate checksum as well

        String localeName = matcher.group(2);
        String stackName = matcher.group(3);

        // Yes, I have a big regret that the JavaScript stack stuff relies on this global, rather than
        // having it passed around properly.

        localizationSetter.setNonPersistentLocaleFromLocaleName(localeName);

        // TODO: Verify request checksum against actual

        return javaScriptStackAssembler.assembleJavaScriptResourceForStack(stackName, compressed);
    }
}
