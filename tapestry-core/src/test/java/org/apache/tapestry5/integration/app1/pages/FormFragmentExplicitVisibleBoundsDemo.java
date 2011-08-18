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
package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

import javax.inject.Inject;
import javax.persistence.PersistenceProperty;

/**
 * Demos the use of explicit bounds for checking visibility of a form fragment for form submission processing.
 * By default, a FormFragment searches to make sure the containing form is visible via "isDeepVisible".  If
 * no intermediate parent elements are invisible, the fragment is considered visible.  However, there are times when
 * that behavior is not desired; some element other than form should be used as the stopping point for determining
 * visibility.  This page demonstrates that use case.
 */
public class FormFragmentExplicitVisibleBoundsDemo {

    @Property
    @Persist
    private String value1;

    @Property
    @Persist
    private String value2;

    @Property
    @Persist
    private boolean frag1visible;

    @Property
    @Persist
    private boolean frag2visible;

    @Property
    @Persist
    private String activeTab;

    void setupRender()
    {
        if (activeTab == null) {
            activeTab = "tab1";
            frag1visible = true;
            frag2visible = true;
        }
    }

    @Inject
    private ComponentResources resources;

    public String getTab1Link()
    {
        return tabLink("tab1");
    }

    public String getTab2Link()
    {
        return tabLink("tab2");
    }

    private String tabLink(String tab) {
        return resources.createEventLink("showTab", tab).toAbsoluteURI();
    }

    void onShowTab(String tabName)
    {
        activeTab = tabName;
    }

    public String getTab1style()
    {
        return tabStyle("tab1");
    }

    public String getTab2style()
    {
        return tabStyle("tab2");
    }

    private String tabStyle(String tab)
    {
        return tab.equals(activeTab)?"":"display: none";
    }

    void onActionFromClearState()
    {
        resources.discardPersistentFieldChanges();
    }
}
