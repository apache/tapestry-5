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
package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * Service that provides information about component libraries.
 * 
 * @since 5.4
 * @see LibraryMapping
 * @see ComponentLibraryInfo
 */
@UsesOrderedConfiguration(ComponentLibraryInfoSource.class)
public interface ComponentLibraryInfoSource
{
    
    /**
     * Finds information about a component library. 
     * @param libraryMapping the {@link LibraryMapping} that defined a component library.
     * @return a {@link ComponentLibraryInfo} or <code>null</code>.
     */
    ComponentLibraryInfo find(LibraryMapping libraryMapping);
    
}
