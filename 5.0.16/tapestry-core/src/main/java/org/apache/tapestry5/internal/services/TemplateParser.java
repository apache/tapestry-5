// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.net.URL;

/**
 * Parses a resource into a {@link org.apache.tapestry5.internal.parser.ComponentTemplate}. The service's configuration
 * is used to map common document types to internal copies of the corresponding DTD.
 *
 * @see org.apache.tapestry5.internal.services.PageLoader
 */
@UsesMappedConfiguration(URL.class)
public interface TemplateParser
{
    /**
     * Parses the given resource into a component template.
     *
     * @param templateResource the path
     * @return the parsed template contents
     * @throws RuntimeException if the resource does not exist, or if there is any kind of parse error
     */
    ComponentTemplate parseTemplate(Resource templateResource);
}
