// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry.ioc.*;
import org.apache.tapestry.ioc.annotations.Scope;
import org.apache.tapestry.ioc.services.PerthreadManager;
import org.apache.tapestry.ioc.services.RegistryShutdownHub;
import org.apache.tapestry.ioc.services.RegistryShutdownListener;
import org.apache.tapestry.services.HttpServletRequestFilter;
import org.apache.tapestry.services.LibraryMapping;
import org.apache.tapestry.upload.internal.services.MultipartDecoderImpl;
import org.apache.tapestry.upload.internal.services.MultipartServletRequestFilter;

import java.util.concurrent.atomic.AtomicBoolean;

public class UploadModule
{
    private static final AtomicBoolean needToAddShutdownListener = new AtomicBoolean(true);
    private static final String NO_LIMIT = "-1";

    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        // Add the component to the "core" library.

        configuration.add(new LibraryMapping("core", "org.apache.tapestry.upload"));
    }

    @Scope(IOCConstants.PERTHREAD_SCOPE)
    public static MultipartDecoder buildMultipartDecoder(PerthreadManager perthreadManager,

                                                         RegistryShutdownHub shutdownHub,

                                                         ObjectLocator locator)
    {
        MultipartDecoderImpl multipartDecoder = locator.autobuild(MultipartDecoderImpl.class);

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

    public static void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(UploadSymbols.REPOSITORY_THRESHOLD, Integer
                .toString(DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD));
        configuration.add(UploadSymbols.REPOSITORY_LOCATION, System.getProperty("java.io.tmpdir"));
        configuration.add(UploadSymbols.REQUESTSIZE_MAX, NO_LIMIT);
        configuration.add(UploadSymbols.FILESIZE_MAX, NO_LIMIT);
    }
}
