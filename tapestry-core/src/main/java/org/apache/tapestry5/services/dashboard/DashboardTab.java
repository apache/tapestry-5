// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.services.dashboard;

import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Identifies a tab that will be present on the Tapestry Dashboard page.
 *
 * @since 5.4
 */
public class DashboardTab
{
    /**
     * Title for the tab, to be displayed on the Dashboard page.
     */
    public final String tabName;

    /**
     * Name of Tapestry page that provides the "content" block.
     */
    public final String pageName;

    public DashboardTab(String tabName, String pageName)
    {
        assert InternalUtils.isNonBlank(tabName);
        assert InternalUtils.isNonBlank(pageName);

        this.tabName = tabName;
        this.pageName = pageName;
    }
}
