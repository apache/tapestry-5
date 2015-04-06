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

package org.apache.tapestry5.annotations;

import org.apache.tapestry5.internal.transform.HeartbeatDeferredWorker;
import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.services.Heartbeat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Marks a component method as deferred until the end of the {@link Heartbeat}. This
 * is only allowed on void methods that do not throw exceptions. The invocation is captured
 * and will execute at the end of the current Heartbeat.
 *
 * This annotation should be used with care, since deferring the invocation can change its semantics. For example, the
 * value stored in instance variables may change between the time the method is invoked and the time it eventually
 * executes. Likewise, runtime exceptions thrown by the method can not be caught by the invoking method.
 *
 * Annotated methods must return void, and not declare any checked exceptions.
 * 
 * @since 5.2.0
 * @see HeartbeatDeferredWorker
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
@UseWith(
{ COMPONENT, MIXIN, PAGE })
public @interface HeartbeatDeferred
{

}
