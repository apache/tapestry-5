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

package org.apache.tapestry5.internal.services.assets;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.assets.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites the {@code url()} attributes inside a CSS (MIME type "text/css")) resource.
 * Each {@code url} is expanded to a complete path; this allows for CSS aggregation, where the location of the
 * CSS file will change (which would ordinarily break relative URLs), and for changing the relative directories of
 * the CSS file and the image assets it may refer to (useful for incorporating a hash of the resource's content into
 * the exposed URL).
 *
 * @since 5.4
 */
public class CSSURLRewriter extends DelegatingSRS
{
    private final Pattern urlPattern = Pattern.compile("url\\(\\s*\"(.*?)\"\\s*\\)", Pattern.MULTILINE);

    private final OperationTracker tracker;

    private final AssetSource assetSource;

    public CSSURLRewriter(StreamableResourceSource delegate, OperationTracker tracker, AssetSource assetSource)
    {
        super(delegate);
        this.tracker = tracker;
        this.assetSource = assetSource;
    }

    @Override
    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies) throws IOException
    {
        StreamableResource base = delegate.getStreamableResource(baseResource, processing, dependencies);

        if (base.getContentType().equals("text/css"))
        {
            return filter(base, baseResource);
        }

        return base;
    }

    private StreamableResource filter(final StreamableResource base, final Resource baseResource) throws IOException
    {
        return tracker.perform("Rewriting relative URLs in " + baseResource,
                new IOOperation<StreamableResource>()
                {
                    public StreamableResource perform() throws IOException
                    {
                        String baseString = readAsString(base);

                        String filtered = replaceURLs(baseString, baseResource);

                        if (filtered == null)
                        {
                            // No URLs were replaced so no need to create a new StreamableResource
                            return base;
                        }

                        BytestreamCache cache = new BytestreamCache(filtered.getBytes("UTF-8"));

                        return new StreamableResourceImpl(base.getDescription(), "text/css",
                                CompressionStatus.COMPRESSABLE,
                                base.getLastModified(),
                                cache);
                    }
                });
    }

    /**
     * Replaces any relative URLs in the content for the resource and returns the content with
     * the URLs expanded.
     *
     * @param input
     *         content of the resource
     * @param baseResource
     *         resource used to resolve relative URLs
     * @return replacement content, or null if no relative URLs in the content
     */
    private String replaceURLs(String input, Resource baseResource)
    {
        boolean didReplace = false;

        StringBuffer output = new StringBuffer(input.length());

        Matcher matcher = urlPattern.matcher(input);

        while (matcher.find())
        {

            String url = matcher.group(1); // the string inside the quotes

            if (url.startsWith("/"))
            {
                matcher.appendReplacement(output, matcher.group());
                continue;
            }

            Asset asset = assetSource.getAsset(baseResource, url, null);

            matcher.appendReplacement(output, String.format("url(\"%s\")", asset.toClientURL()));

            didReplace = true;
        }

        if (!didReplace)
        {
            return null;
        }

        matcher.appendTail(output);

        return output.toString();
    }


    // TODO: I'm thinking there's an (internal) service that should be created to make this more reusable.
    private String readAsString(StreamableResource resource) throws IOException
    {
        StringBuffer result = new StringBuffer(resource.getSize());
        char[] buffer = new char[5000];

        InputStream is = resource.openStream();

        InputStreamReader reader = new InputStreamReader(resource.openStream(), "UTF-8");

        try
        {

            while (true)
            {
                int length = reader.read(buffer);

                if (length < 0)
                {
                    break;
                }

                result.append(buffer, 0, length);
            }
        } finally
        {
            reader.close();
        }

        return result.toString();
    }
}
