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

import java.lang.ref.SoftReference;

/**
 * Defines the types of reference the page cache can use.
 * @see PageCachingReferenceTypeService
 * @since 5.8.3
 */
public enum ReferenceType
{
    /**
     * Use a soft reference ({@linkplain SoftReference}) to cached page instances.
     */
    SOFT, 
    
    /**
     * Use a strong (normal) reference to cached page instances.
     */
    STRONG
}