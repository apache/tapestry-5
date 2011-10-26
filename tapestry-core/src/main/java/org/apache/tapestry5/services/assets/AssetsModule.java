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

package org.apache.tapestry5.services.assets;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.assets.*;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.Core;

/**
 * @since 5.3
 */
@Marker(Core.class)
public class AssetsModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(StreamableResourceSource.class, StreamableResourceSourceImpl.class);
        binder.bind(CompressionAnalyzer.class, CompressionAnalyzerImpl.class);
        binder.bind(ContentTypeAnalyzer.class, ContentTypeAnalyzerImpl.class);
        binder.bind(ResourceChangeTracker.class, ResourceChangeTrackerImpl.class);
        binder.bind(ResourceMinimizer.class, MasterResourceMinimizer.class);
    }

    @Contribute(SymbolProvider.class)
    @FactoryDefaults
    public static void setupSymbols(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.MINIFICATION_ENABLED, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, "true");
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, SymbolConstants.PRODUCTION_MODE_VALUE);
        configuration.add(SymbolConstants.ASSET_URL_FULL_QUALIFIED, "false");
    }

    // The use of decorators is to allow third-parties to get their own extensions
    // into the pipeline.

    @Decorate(id = "GZipCompression", serviceInterface = StreamableResourceSource.class)
    public StreamableResourceSource enableCompression(StreamableResourceSource delegate,
                                                      @Symbol(SymbolConstants.GZIP_COMPRESSION_ENABLED)
                                                      boolean gzipEnabled, @Symbol(SymbolConstants.MIN_GZIP_SIZE)
    int compressionCutoff)
    {
        return gzipEnabled ? new SRSCompressingInterceptor(compressionCutoff, delegate) : null;
    }

    @Decorate(id = "CacheCompressed", serviceInterface = StreamableResourceSource.class)
    @Order("before:GZIpCompression")
    public StreamableResourceSource enableCompressedCaching(StreamableResourceSource delegate,
                                                            @Symbol(SymbolConstants.GZIP_COMPRESSION_ENABLED)
                                                            boolean gzipEnabled, ResourceChangeTracker tracker)
    {
        if (!gzipEnabled)
            return null;

        SRSCompressedCachingInterceptor interceptor = new SRSCompressedCachingInterceptor(delegate);

        tracker.addInvalidationListener(interceptor);

        return interceptor;
    }

    @Decorate(id = "Cache", serviceInterface = StreamableResourceSource.class)
    @Order("after:GZipCompression")
    public StreamableResourceSource enableUncompressedCaching(StreamableResourceSource delegate,
                                                              ResourceChangeTracker tracker)
    {
        SRSCachingInterceptor interceptor = new SRSCachingInterceptor(delegate);

        tracker.addInvalidationListener(interceptor);

        return interceptor;
    }

    @Decorate(id = "Minification", serviceInterface = StreamableResourceSource.class)
    @Order("after:Cache")
    public StreamableResourceSource enableMinification(StreamableResourceSource delegate, ResourceMinimizer minimizer,
                                                       @Symbol(SymbolConstants.MINIFICATION_ENABLED)
                                                       boolean enabled)
    {
        if (enabled)
            return new SRSMinimizingInterceptor(delegate, minimizer);

        return null;
    }

    /**
     * Adds content types:
     * <dl>
     * <dt>css</dt>
     * <dd>text/css</dd>
     * <dt>js</dt>
     * <dd>text/javascript</dd>
     * <dt>jpg, jpeg</dt>
     * <dd>image/jpeg</dd>
     * <dt>gif</dt>
     * <dd>image/gif</dd>
     * <dt>png</dt>
     * <dd>image/png</dd>
     * <dt>svg</dt>
     * <dd>image/svg+xml</dd>
     * <dt>swf</dt>
     * <dd>application/x-shockwave-flash</dd>
     * </dl>
     */
    @Contribute(ContentTypeAnalyzer.class)
    public void setupDefaultContentTypeMappings(MappedConfiguration<String, String> configuration)
    {
        configuration.add("css", "text/css");
        configuration.add("js", "text/javascript");
        configuration.add("gif", "image/gif");
        configuration.add("jpg", "image/jpeg");
        configuration.add("jpeg", "image/jpeg");
        configuration.add("png", "image/png");
        configuration.add("swf", "application/x-shockwave-flash");
        configuration.add("svg", "image/svg+xml");
    }

    /**
     * Disables compression for the following content types:
     * <ul>
     * <li>image/jpeg</li>
     * <li>image/gif</li>
     * <li>image/png</li>
     * <li>application/x-shockwave-flash</li>
     * </ul>
     */
    @Contribute(CompressionAnalyzer.class)
    public void disableCompressionForImageTypes(MappedConfiguration<String, Boolean> configuration)
    {
        configuration.add("image/jpeg", false);
        configuration.add("image/gif", false);
        configuration.add("image/png", false);
        configuration.add("application/x-shockwave-flash", false);
    }
}
