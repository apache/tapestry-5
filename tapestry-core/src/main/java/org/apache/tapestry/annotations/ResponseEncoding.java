// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.annotations;

import java.lang.annotation.*;


/**
 * An annotation on a page component used to identify the respones encoding (the character set of the text sent in the
 * response). An alternative to the {@link org.apache.tapestry.annotations.Meta} annotation with the {@link
 * org.apache.tapestry.TapestryConstants#RESPONSE_ENCODING} key.
 *
 * @see org.apache.tapestry.annotations.ContentType
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseEncoding
{
    /**
     * The response encoding, a value such as "utf-8".
     */
    String value();
}
