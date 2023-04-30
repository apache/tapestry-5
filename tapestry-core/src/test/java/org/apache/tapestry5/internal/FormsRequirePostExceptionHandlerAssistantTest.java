// Copyright 2023 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import java.io.IOException;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.commons.util.FormsRequirePostException;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class FormsRequirePostExceptionHandlerAssistantTest extends TapestryTestCase
{
    @Test
    public void foo()
    {
        ComponentSource componentSource = newMock(ComponentSource.class);
        Component page = newMock(Component.class);
        ComponentResources componentResources = mockComponentResources();
        PageRenderLinkSource linkSource = mockPageRenderLinkSource();
        Link link = mockLink("/foo");

        expect(componentSource.getActivePage()).andReturn(page).atLeastOnce();
        expect(page.getComponentResources()).andReturn(componentResources).atLeastOnce();
        train_getPageName(componentResources, "foo");

        expect(linkSource.createPageRenderLink("foo")).andReturn(link).atLeastOnce();

        replay();

        FormsRequirePostExceptionHandlerAssistant assistant = new FormsRequirePostExceptionHandlerAssistant(componentSource, linkSource);

        FormsRequirePostException exception = new FormsRequirePostException("doesn't matter", null);

        try
        {
            Link l = (Link) assistant.handleRequestException(exception, null);
            assertEquals(l.toURI(), link.toURI());
        }
        catch (IOException e)
        {
            fail();
        }
        
        verify();
    }
}
