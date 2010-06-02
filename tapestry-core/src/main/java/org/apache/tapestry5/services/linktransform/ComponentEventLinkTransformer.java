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

package org.apache.tapestry5.services.linktransform;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.services.ComponentEventRequestParameters;

/**
 * Allows for selective replacement of the default {@link Link} used to represent a component event request.
 * This is a service, but also the contribution to the service, as a chain of command.
 * 
 * @since 5.2.0
 */
@UsesOrderedConfiguration(ComponentEventLinkTransformer.class)
public interface ComponentEventLinkTransformer
{
    /**
     * Allows the default Link created for the component event request to be replaced.
     * 
     * @param defaultLink
     *            the default Link generated for a component event request
     * @param parameters
     *            used to create the default Link
     * @return a replacement Link, or null
     */
    Link transformComponentEventLink(Link defaultLink, ComponentEventRequestParameters parameters);
}
