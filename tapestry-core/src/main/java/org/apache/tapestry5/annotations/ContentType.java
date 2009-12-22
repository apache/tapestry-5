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

package org.apache.tapestry5.annotations;

import java.lang.annotation.*;


/**
 * An annotation on a page component used to identify the content type the page returns. An alternative to the {@link
 * org.apache.tapestry5.annotations.Meta} annotation with the {@link org.apache.tapestry5.MetaDataConstants#RESPONSE_CONTENT_TYPE}
 * key.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ContentType
{
    /**
     * The content type for the page.  Typically, something like "text/html".
     */
    String value();
}
