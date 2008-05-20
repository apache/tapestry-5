// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.internal.parser;

import org.apache.tapestry5.ioc.Resource;

import java.util.List;
import java.util.Set;

/**
 * A parsed component template, containing all the tokens parsed from the template.
 */
public interface ComponentTemplate
{
    /**
     * Returns true if no template could be found for the component.
     */
    boolean isMissing();

    /**
     * Returns the resource that was parsed to form the template.
     */
    Resource getResource();

    /**
     * Returns a list of tokens that were parsed from the template. The caller should not modify this list.
     */
    List<TemplateToken> getTokens();

    /**
     * Returns a set of strings corresponding to {@link org.apache.tapestry5.internal.parser.StartComponentToken}s
     * within the template that have a non-blank id attribute.
     */
    Set<String> getComponentIds();
}
