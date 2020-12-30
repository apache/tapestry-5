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

import org.apache.tapestry5.commons.Resource;

/**
 * Class that represents the exception of an asset not being found.
 */
public class AssetNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    final private Resource resource;

    /**
     * {@inheritDoc}
     */
    public AssetNotFoundException(String message) {
        super(message);
        resource = null;
    }

    /**
     * Constructs an exception with message and a {@link Resource}.
     * @param message a <code>String</code>.
     * @param resource a {@link Resource}.
     */
    public AssetNotFoundException(String message, Resource resource) {
        super(message);
        this.resource = resource;
    }
    
    /**
     * The resource which wasn't found. It may be null.
     * @return a {@link Resource}.
     */
    public Resource getResource() {
        return resource;
    }

}
