// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.io.IOException;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

/**
 * Responsible for firing the {@linkplain EventConstants#ACTIVATE} event on the page.
 * 
 * @since 5.2.0
 */
public interface PageActivator
{
    /**
     * Activates the page.
     * 
     * @param pageResources
     *            resource for the page
     * @param activationContext
     *            the page activation context
     * @param resultProcessor
     *            responsible for handling the value returned from the event handler method
     * @return true if result processor received a non-null value (indicating that the processing of the request
     *         should terminate)
     */
    @SuppressWarnings("unchecked")
    boolean activatePage(ComponentResources pageResources, EventContext activationContext,
            ComponentEventResultProcessor resultProcessor) throws IOException;
}
