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

package org.apache.tapestry5.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation placed on fields whose value should be retained past the end of the request. This is most often
 * associated with fields that are <em>lazily loaded</em>. By marking such fields with the Retain annotation, the fields
 * will <em>not</em> be discarded at the end of the request.
 * <p/>
 * This is quite different from {@link Persist}, because the value that's allowed to be retained is not stored
 * persistently; it is simply not cleared out. A subsequent request, even from the same user, may be processed by a
 * different instance of the page where the value is still null.
 * <p/>
 * This annotation should only be used with lazily-evaluated objects that contain no client-specific information.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface Retain
{

}
