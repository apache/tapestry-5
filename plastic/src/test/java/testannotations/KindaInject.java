package testannotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Used to test {code @Inject}-like behavior.
 */
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface KindaInject
{
}
