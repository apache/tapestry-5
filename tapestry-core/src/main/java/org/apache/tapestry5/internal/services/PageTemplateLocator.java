// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.model.ComponentModel;

import java.util.Locale;

/**
 * Responsible for locating page templates in the web application context.
 */
public interface PageTemplateLocator
{
    /**
     * Given model, determines if the model is for a page (rather than a component) and if so, sees if there is a
     * localized template for the page in the web application context.
     *
     * @param model  the component model defining the page to search for
     * @param locale the desired localization of the template
     * @return the template resource, or null if not found or the model is not a page
     */
    Resource findPageTemplateResource(ComponentModel model, Locale locale);
}
