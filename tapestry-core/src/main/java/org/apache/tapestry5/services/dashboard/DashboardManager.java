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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

import java.util.List;

/**
 * Organizes the tabs on the {@link org.apache.tapestry5.corelib.pages.T5Dashboard} page.
 *
 * @since 5.4
 */
@UsesOrderedConfiguration(DashboardTab.class)
public interface DashboardManager
{
    List<String> getTabNames();

    Block getTabContent(String tabName);
}
