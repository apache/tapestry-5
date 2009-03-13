// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.upload.services;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileCleaner;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.apache.tapestry5.services.ComponentEventRequestFilter;
import org.apache.tapestry5.services.HttpServletRequestFilter;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.upload.internal.services.MultipartDecoderImpl;
import org.apache.tapestry5.upload.internal.services.MultipartServletRequestFilter;
import org.apache.tapestry5.upload.internal.services.UploadExceptionFilter;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class UploadModule
{
    private static final String NO_LIMIT = "-1";

    private static final AtomicBoolean needToAddShutdownListener = new AtomicBoolean(true);

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        // Add the component to the "core" library.

        configuration.add(new LibraryMapping("core", "org.apache.tapestry5.upload"));
    }

    @Scope(ScopeConstants.PERTHREAD)
    public static MultipartDecoder buildMultipartDecoder(PerthreadManager perthreadManager,

                                                         RegistryShutdownHub shutdownHub,

                                                         @Autobuild MultipartDecoderImpl multipartDecoder)
    {
        // This is proabably overkill since the FileCleaner should catch temporary files, but lets
        // be safe.
        perthreadManager.addThreadCleanupListener(multipartDecoder);

        if (needToAddShutdownListener.getAndSet(false))
        {
            shutdownHub.addRegistryShutdownListener(new RegistryShutdownListener()
            {
                public void registryDidShutdown()
                {
                    FileCleaner.exitWhenFinished();
                }
            });
        }

        return multipartDecoder;
    }

    /**
     * Contributes a filter, "MultipartFilter" after "IgnoredPaths".
     */
    public static void contributeHttpServletRequestHandler(OrderedConfiguration<HttpServletRequestFilter> configuration,
                                                           MultipartDecoder multipartDecoder)
    {
        configuration.add("MultipartFilter", new MultipartServletRequestFilter(multipartDecoder), "after:IgnoredPaths");
    }

    /**
     * Adds UploadException to the pipeline, between Secure and Ajax (both provided by TapestryModule). UploadException
     * is responsible for triggering the {@linkplain org.apache.tapestry5.upload.services.UploadEvents#UPLOAD_EXCEPTION
     * upload exception event}.
     */
    public static void contributeComponentEventRequestHandler(
            OrderedConfiguration<ComponentEventRequestFilter> configuration)
    {
        configuration.addInstance("UploadException", UploadExceptionFilter.class, "after:Secure",
                                  "before:Ajax");
    }

    /**
     * The default FileItemFactory used by the MultipartDecoder is {@link org.apache.commons.fileupload.disk.DiskFileItemFactory}.
     */
    public static FileItemFactory buildDefaultFileItemFactory(
            @Symbol(UploadSymbols.REPOSITORY_THRESHOLD)
            int repositoryThreshold,

            @Inject @Symbol(UploadSymbols.REPOSITORY_LOCATION)
            String repositoryLocation)
    {
        return new DiskFileItemFactory(repositoryThreshold, new File(repositoryLocation));
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(UploadSymbols.REPOSITORY_THRESHOLD, Integer
                .toString(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD));
        configuration.add(UploadSymbols.REPOSITORY_LOCATION, System.getProperty("java.io.tmpdir"));
        configuration.add(UploadSymbols.REQUESTSIZE_MAX, NO_LIMIT);
        configuration.add(UploadSymbols.FILESIZE_MAX, NO_LIMIT);
    }
}
