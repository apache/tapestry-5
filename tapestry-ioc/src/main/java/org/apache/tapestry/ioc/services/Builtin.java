package org.apache.tapestry.ioc.services;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks services provided by this module that may need to be unambiguously referenced.
 * Injecting with this marker annotation and the correct type ensure that the version defined in
 * this module is used, even if another module provides a service with the same service
 * interface.
 */
@Target(
{ PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Builtin
{

}