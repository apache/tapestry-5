package org.apache.tapestry.beaneditor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Placed on either a property getter or a property setter method to control the order in which the
 * properties are presented to the user.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface OrderAfter
{
    /**
     * The name of the other property. This property will be ordered before the other property.
     */
    String value();
}
