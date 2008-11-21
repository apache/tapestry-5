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
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * A marker annotation applied to a mixin to indicate that the mixin's render state behavior is deferred until after the
 * the behavior of the component to which the mixin is attached. Normally, mixins occur before the component. This
 * divides each phase in the render state machine into three virtual phases: before the component, the component itself,
 * and after the component.
 */
@Target(TYPE)
@Documented
@Retention(RUNTIME)
@Inherited
public @interface MixinAfter
{

}
