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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.internal.services.PageLoader;

/**
 * A type of {@link PageElement} that has a body that can be added to. This is part of the
 * constuction phase that is faciliated by the {@link PageLoader}.
 */
public interface BodyPageElement
{
    /**
     * Used during the construction of the page. Adds a page element as part of the body of the
     * component. The body of a component is defined as the portion of the container's template
     * directly enclosed by component's start and end elements.
     */
    void addToBody(PageElement element);
}
