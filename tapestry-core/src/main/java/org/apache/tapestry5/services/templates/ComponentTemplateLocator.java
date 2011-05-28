// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.templates;

import java.util.Locale;

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

/**
 * Chain-of-command interface used to locate page and component templates. Contributions to this service support
 * alternate naming schemes for template files, or alternate locations in which to search for template files.
 * <p>
 * This service was introduced in Tapestry 5.2, but deprecated in Tapestry 5.3. It is utilized by the default
 * implementation of {@link ComponentResourceSelector}.
 * 
 * @see TapestryConstants#TEMPLATE_EXTENSION
 * @since 5.2.0
 * @deprecated Deprecated in 5.3, override or decorate {@link ComponentResourceSelector} instead.
 */
@UsesOrderedConfiguration(ComponentTemplateLocator.class)
public interface ComponentTemplateLocator
{
    /**
     * Locates the template for the given model as a {@link Resource}.
     * 
     * @param model
     *            defines the component, especially its {@linkplain ComponentModel#getBaseResource() base resource}
     * @param locale
     *            to which the
     * @return localized template resource if found, or null if not found
     */
    Resource locateTemplate(ComponentModel model, Locale locale);
}
