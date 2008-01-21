// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.lang.annotation.*;


/**
 * Marker annotation for a service that should be used for traditional page oriented requests, as opposed to Ajax requests
 * (that send ad-hoc or {@linkplain PartialMarkupRenderer partial page markup} responses.
 *
 * @see org.apache.tapestry.services.ComponentActionRequestHandler
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traditional
{
}
