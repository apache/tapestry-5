// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.upload.services;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileCleaner;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.ioc.services.RegistryShutdownHub;
import org.apache.tapestry.ioc.services.RegistryShutdownListener;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;
import org.apache.tapestry.services.HttpServletRequestFilter;
import org.apache.tapestry.services.LibraryMapping;

public class UploadModule
{
    private static boolean _shutdownListenerSet;

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        // Add the component to the "core" library.

        configuration.add(new LibraryMapping("core", "org.apache.tapestry.upload"));
    }

    @Scope("perthread")
    public synchronized static MultipartDecoder buildMultipartDecoder(
            ThreadCleanupHub threadCleanupHub,

            RegistryShutdownHub shutdownHub,

            @Inject
            @Symbol(UploadSymbols.REPOSITORY_LOCATION)
            String repositoryPath,

            @Symbol(UploadSymbols.REPOSITORY_THRESHOLD)
            int repositoryThreshold,

            @Symbol(UploadSymbols.REQUESTSIZE_MAX)
            long maxRequestSize,

            @Symbol(UploadSymbols.FILESIZE_MAX)
            long maxFileSize,

            SymbolSource symbolSource)
    {
        MultipartDecoderImpl multipartDecoder = new MultipartDecoderImpl(repositoryPath,
                repositoryThreshold, maxRequestSize, maxFileSize);

        // This is proabably overkill since the FileCleaner should catch temporary files, but lets
        // be safe.
        threadCleanupHub.addThreadCleanupListener(multipartDecoder);

        if (_shutdownListenerSet)
        {
            shutdownHub.addRegistryShutdownListener(new RegistryShutdownListener()
            {
                public void registryDidShutdown()
                {
                    FileCleaner.exitWhenFinished();
                }
            });

            _shutdownListenerSet = true;
        }

        return multipartDecoder;
    }

    public static void contributeHttpServletRequestHandler(
            OrderedConfiguration<HttpServletRequestFilter> configuration,
            MultipartDecoder multipartDecoder)
    {
        configuration.add("MultipartFilter", new MultipartServletRequestFilter(multipartDecoder));
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(UploadSymbols.REPOSITORY_THRESHOLD, Integer
                .toString(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD));
        configuration.add(UploadSymbols.REPOSITORY_LOCATION, System.getProperty("java.io.tmpdir"));
        // No limit
        configuration.add(UploadSymbols.REQUESTSIZE_MAX, "-1");
        // No limit
        configuration.add(UploadSymbols.FILESIZE_MAX, "-1");
    }
}
