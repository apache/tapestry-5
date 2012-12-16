// Copyright 2010, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.corelib;

/**
 * Controls if and how client-side form input validation occurs.
 *
 * @since 5.2.2
 */
public enum ClientValidation
{
    /**
     * Fields validate as the user tabs out of them ("onblur" client side event), as well as when the form submits.
     *
     * @deprecated Deprecated in 5.4, and no longer supported.
     */
    BLUR,

    /**
     * Fields only validate when the form submits (validation errors will prevent the form from actually submitting).
     * This is the default behavior.
     */
    SUBMIT,

    /**
     * Client-side validation is disabled.
     */
    NONE;
}
