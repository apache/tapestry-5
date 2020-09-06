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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.pageload.PagePreloader;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

public class PagePreloaderImpl implements PagePreloader
{
    private final Logger logger;

    private final List<String> pageNames = CollectionFactory.newList();

    private final OperationTracker tracker;

    private final ComponentSource componentSource;

    private final ThreadLocale threadLocale;

    private final LocalizationSetter localizationSetter;

    public PagePreloaderImpl(Logger logger,
                             OperationTracker tracker,
                             ComponentSource componentSource, Collection<String> configuration,
                             ThreadLocale threadLocale,
                             LocalizationSetter localizationSetter)
    {
        this.tracker = tracker;
        this.componentSource = componentSource;
        this.logger = logger;
        this.threadLocale = threadLocale;
        this.localizationSetter = localizationSetter;

        pageNames.addAll(configuration);
    }

    @Override
    public void preloadPages()
    {
        if (pageNames.isEmpty())
        {
            return;
        }

        logger.info(String.format("Preloading %,d pages.", pageNames.size()));

        threadLocale.setLocale(localizationSetter.getSupportedLocales().get(0));

        final long startNanos = System.nanoTime();

        try
        {
            for (final String pageName : pageNames)
            {
                tracker.run(String.format("Preloading page '%s'.", pageName), new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                componentSource.getPage(pageName);
                            }
                        }
                );
            }
        } catch (Exception ex)
        {
            // Report the exception, and just give up at this point.
            logger.error(ExceptionUtils.toMessage(ex), ex);

            return;
        }

        final double elapsedNanos = System.nanoTime() - startNanos;

        logger.info(String.format("Preloaded %,d pages in %.2f seconds.",
                pageNames.size(),
                elapsedNanos * 10E-10d));
    }
}
