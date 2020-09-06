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

package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.ioc.services.ThreadLocale;

/**
 * Determines the {@link ComponentResourceSelector} for the current request. This is often based on cookies, query
 * parameters, or other details available in the {@link org.apache.tapestry5.http.services.Request}. The default implementation simply wraps the
 * {@linkplain ThreadLocale current locale} as a ComponentResourceSelector. A custom implementation may
 * {@linkplain ComponentResourceSelector#withAxis(Class, Object) add additional axes} to the selector.
 * 
 * @since 5.3
 */
public interface ComponentRequestSelectorAnalyzer
{
    /**
     * Constructs a selector for locating or loading pages in the current request.
     */
    ComponentResourceSelector buildSelectorForRequest();
}
