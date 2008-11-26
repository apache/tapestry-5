package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.*;


/**
 * Annotation for methods that should be invoked after injection. This occurs last: after constructor injection and
 * after field injection. It should be placed on a <strong>public method</strong>. Any return value from the method is
 * ignored. The order of invocation for classes with multiple marked methods (including methods inherited from
 * super-classes) is not, at this time, defined.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PostInjection
{
}
