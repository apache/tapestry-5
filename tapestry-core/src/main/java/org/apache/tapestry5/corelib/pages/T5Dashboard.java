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

package org.apache.tapestry5.corelib.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.base.AbstractInternalPage;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.dashboard.DashboardManager;

/**
 * @see org.apache.tapestry5.services.dashboard.DashboardManager
 * @since 5.4
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
@ContentType("text/html")
@Import(stylesheet = "dashboard.css")
public class T5Dashboard extends AbstractInternalPage
{
    @Inject
    @Symbol(SymbolConstants.TAPESTRY_VERSION)
    @Property
    private String frameworkVersion;

    @Property
    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    @Inject
    @Property
    private DashboardManager dashboardManager;

    @Property
    private String tabName;

    private String activeTab;

    public String getTabClass()
    {
        return tabName.equalsIgnoreCase(activeTab) ? "active" : null;
    }

    public Block getContent()
    {
        return dashboardManager.getTabContent(activeTab);
    }

    void onActivate()
    {
        activeTab = dashboardManager.getTabNames().get(0);
    }

    boolean onActivate(String tabName)
    {
        activeTab = tabName;

        return true;
    }

    String onPassivate()
    {
        return activeTab;
    }
}
