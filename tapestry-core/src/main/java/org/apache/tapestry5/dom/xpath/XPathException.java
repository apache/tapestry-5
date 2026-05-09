//  Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.dom.xpath;

/**
 * Unchecked exception thrown when an XPath expression cannot be parsed or evaluated.
 * <p>
 * Wraps Jaxen's checked {@link org.jaxen.JaxenException} so callers do not need to
 * handle it explicitly.
 *
 * @since 5.10
 */
public class XPathException extends RuntimeException {

    /**
     * @param message human-readable description of the failure
     * @param cause   the underlying {@link org.jaxen.JaxenException}
     */
    public XPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
