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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.services.AssetNotFoundException;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.assets.AssetChecksumGenerator;
import org.apache.tapestry5.services.assets.CompressionStatus;
import org.apache.tapestry5.services.assets.ResourceDependencies;
import org.apache.tapestry5.services.assets.StreamableResource;
import org.apache.tapestry5.services.assets.StreamableResourceProcessing;
import org.apache.tapestry5.services.assets.StreamableResourceSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rewrites the {@code url()} attributes inside a CSS (MIME type "text/css")) resource.
 * Each {@code url} is expanded to a complete path; this allows for CSS aggregation, where the location of the
 * CSS file will change (which would ordinarily break relative URLs), and for changing the relative directories of
 * the CSS file and the image assets it may refer to (useful for incorporating a hash of the resource's content into
 * the exposed URL).
 *
 *
 * One potential problem with URL rewriting is the way that URLs for referenced resources are generated; we are
 * somewhat banking on the fact that referenced resources are non-compressable images.
 *
 * @see SymbolConstants#STRICT_CSS_URL_REWRITING
 * @since 5.4
 */
public class CSSURLRewriter extends DelegatingSRS
{
    // Group 1 is the optional single or double quote (note the use of backtracking to match it)
    // Group 2 is the text inside the quotes, or inside the parens if no quotes
    // Group 3 is any query parmameters (see issue TAP5-2106)
    private final Pattern urlPattern = Pattern.compile(
            "url" +
                    "\\(" +                 // opening paren
                    "\\s*" +
                    "(['\"]?)" +            // group 1: optional single or double quote
                    "(.+?)" +               // group 2: the main part of the URL, up to the first '#' or '?'
                    "([\\#\\?].*?)?" +      // group 3: Optional '#' or '?' to end of string
                    "\\1" +                 // optional closing single/double quote
                    "\\s*" +
                    "\\)");                 // matching close paren

    // Does it start with a '/' or what looks like a scheme ("http:")?
    private final Pattern completeURLPattern = Pattern.compile("^[#/]|(\\p{Alpha}\\w*:)");

    private final OperationTracker tracker;

    private final AssetSource assetSource;

    private final AssetChecksumGenerator checksumGenerator;

    private final Logger logger = LoggerFactory.getLogger(CSSURLRewriter.class);

    private final boolean strictCssUrlRewriting;

    private final ContentType CSS_CONTENT_TYPE = new ContentType("text/css");

    public CSSURLRewriter(StreamableResourceSource delegate, OperationTracker tracker, AssetSource assetSource,
                          AssetChecksumGenerator checksumGenerator, boolean strictCssUrlRewriting)
    {
        super(delegate);
        this.tracker = tracker;
        this.assetSource = assetSource;
        this.checksumGenerator = checksumGenerator;
        this.strictCssUrlRewriting = strictCssUrlRewriting;
    }

    @Override
    public StreamableResource getStreamableResource(Resource baseResource, StreamableResourceProcessing processing, ResourceDependencies dependencies) throws IOException
    {
        StreamableResource base = delegate.getStreamableResource(baseResource, processing, dependencies);

        if (base.getContentType().equals(CSS_CONTENT_TYPE))
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

                        return new StreamableResourceImpl(base.getDescription(),
                                CSS_CONTENT_TYPE,
                                CompressionStatus.COMPRESSABLE,
                                base.getLastModified(),
                                cache, checksumGenerator, base.getResponseCustomizer());
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
            String url = matcher.group(2); // the string inside the quotes

            // When the URL starts with a slash or a scheme (e.g. http: or data:) , there's no need
            // to rewrite it (this is actually rare in Tapestry as you want to use relative URLs to
            // leverage the asset pipeline.
            Matcher completeURLMatcher = completeURLPattern.matcher(url);
            boolean matchFound = completeURLMatcher.find();
            boolean isAssetUrl = matchFound && "asset:".equals(completeURLMatcher.group(1));
            if (matchFound && !isAssetUrl)
            {
                String queryParameters = matcher.group(3);

                if (queryParameters != null)
                {
                    url = url + queryParameters;
                }

                // This may normalize single quotes, or missing quotes, to double quotes, but is not
                // considered a real change, since all such variations are valid.
                appendReplacement(matcher, output, url);
                continue;
            }

            if (isAssetUrl)
            {
                // strip away the "asset:" prefix
                url = url.substring(6);
            }

            Asset asset;
            
            // TAP5-2656
            try 
            {
                asset = assetSource.getAsset(baseResource, url, null);
            }
            catch (AssetNotFoundException e)
            {
                asset = null;
            }

            if (asset != null)
            {
                String assetURL = asset.toClientURL();

                String queryParameters = matcher.group(3);
                if (queryParameters != null)
                {
                    assetURL += queryParameters;
                }

                appendReplacement(matcher, output, assetURL);

                didReplace = true;

            } else
            {
                final String message = String.format("URL %s, referenced in file %s, doesn't exist.", url, baseResource.toURL(), baseResource);
                if (strictCssUrlRewriting)
                {
                    throw new RuntimeException(message);
                } else if (logger.isWarnEnabled())
                {
                    logger.warn(message);
                }
            }

        }

        if (!didReplace)
        {
            return null;
        }

        matcher.appendTail(output);

        return output.toString();
    }

    private void appendReplacement(Matcher matcher, StringBuffer output, String assetURL)
    {
        matcher.appendReplacement(output, String.format("url(\"%s\")", assetURL));
    }


    // TODO: I'm thinking there's an (internal) service that should be created to make this more reusable.
    private String readAsString(StreamableResource resource) throws IOException
    {
        StringBuffer result = new StringBuffer(resource.getSize());
        char[] buffer = new char[5000];

        InputStream is = resource.openStream();

        InputStreamReader reader = new InputStreamReader(is, "UTF-8");

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
            is.close();
        }

        return result.toString();
    }
}
