package org.apache.tapestry5.ioc.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Registry {
	Class<?>[] modules();
	RegistryShutdownType shutdown() default RegistryShutdownType.AFTER_CLASS;
}
