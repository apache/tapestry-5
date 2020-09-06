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

package org.apache.tapestry5.modules;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.pageload.DefaultComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.internal.pageload.DefaultComponentResourceLocator;
import org.apache.tapestry5.internal.pageload.PagePreloaderImpl;
import org.apache.tapestry5.internal.services.ComponentTemplateSource;
import org.apache.tapestry5.internal.services.ComponentTemplateSourceImpl;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.PagePreloader;
import org.apache.tapestry5.services.pageload.PreloaderMode;

/**
 * @since 5.3
 */
@Marker(Core.class)
public class PageLoadModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ComponentRequestSelectorAnalyzer.class, DefaultComponentRequestSelectorAnalyzer.class);
        binder.bind(ComponentResourceLocator.class, DefaultComponentResourceLocator.class);
        binder.bind(ComponentTemplateSource.class, ComponentTemplateSourceImpl.class);
        binder.bind(PagePreloader.class, PagePreloaderImpl.class);
    }

    @Startup
    public static void preloadPages(PagePreloader preloader,
                                    @Symbol(SymbolConstants.PRELOADER_MODE)
                                    PreloaderMode mode,
                                    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                    boolean productionMode)
    {
        if (mode.isEnabledFor(productionMode))
        {
            preloader.preloadPages();
        }
    }
}
