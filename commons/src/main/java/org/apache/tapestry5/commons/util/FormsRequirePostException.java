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

package org.apache.tapestry5.commons.util;

import org.apache.tapestry5.commons.internal.util.TapestryException;

/**
 * Exception thrown by Tapestry's {@link org.apache.tapestry5.corelib.components.Form} component
 * when the request method is other than POST.
 * 
 * @see org.apache.tapestry5.corelib.components.Form
 * @see org.apache.tapestry5.services.RequestExceptionHandler
 * 
 * @since 5.8.3
 */
public class FormsRequirePostException extends TapestryException {

    private static final long serialVersionUID = 1L;

    public FormsRequirePostException(String message, Throwable cause) 
    {
        super(message, cause);
    }

	
}
