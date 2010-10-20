// Copyright 2010 The Apache Software Foundation
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
 * Controls if and how client-side form input validation occurs. For compatibility with Tapestry 5.1,
 * a coercion from "true" to {@link #BLUR} and from "false" to {@link #NONE} are added (though these
 * may be removed some time after Tapestry 5.2).
 * 
 * @since 5.2.2
 */
public enum ClientValidation
{
    /**
     * Fields validate as the user tabs out of them ("onblur" client side event), as well as when the form submits. This
     * is the default behavior.
     */
    BLUR,

    /**
     * Fields only validate when the form submits (validation errors will prevent the form from actually submitting).
     */
    SUBMIT,

    /**
     * Client-side validation is disabled.
     */
    NONE;
}
