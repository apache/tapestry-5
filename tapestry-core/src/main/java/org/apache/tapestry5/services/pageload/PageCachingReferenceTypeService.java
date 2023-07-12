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

package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * A service which defines whether cache page instances should have soft references pointing to
 * it (the default) or strong, non-garbage-collectable ones. This service is a chain of command
 * of itself.
 *
 * @see ReferenceType
 * @since 5.8.3
 */
@UsesOrderedConfiguration(PageCachingReferenceTypeService.class)
public interface PageCachingReferenceTypeService
{
    /**
     * Defines which reference type should be used to cache instances of the given page.
     * Returning <code>null</code> means this implementation doesn't handle wit the given page.
     * @param canonicalPageName the name of the page.
     * @return a ReferenceType object or <code>null</code>
     */
    ReferenceType get(String canonicalPageName);
    
}
