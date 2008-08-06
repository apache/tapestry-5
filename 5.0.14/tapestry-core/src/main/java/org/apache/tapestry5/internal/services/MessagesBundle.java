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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;

/**
 * Represents a bundle of properties files that can be used to collect properties that are eventually used to form a
 * {@link Messages}.
 */
public interface MessagesBundle
{

    /**
     * Returns an object used to identify this particular bundle; this should be a simple immutable value such as a
     * String.
     */
    Object getId();

    /**
     * Returns the base resource for this bundle of properties files.
     */
    Resource getBaseResource();

    /**
     * Returns a parent bundle for this bundle, or null if this bundle has no parent. Parent bundle provide properties
     * that are overridden by child bundles.
     */
    MessagesBundle getParent();
}
