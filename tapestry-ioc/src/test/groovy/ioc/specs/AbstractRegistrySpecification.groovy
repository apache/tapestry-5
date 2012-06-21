package ioc.specs

import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import spock.lang.AutoCleanup
import spock.lang.Specification

/**
 * Base class for Spock specifications that use a new {@link Registry} for each feature method.
 */
abstract class AbstractRegistrySpecification extends Specification {

  @AutoCleanup("shutdown")
  protected Registry registry;

  /**
   * Constructs a new {@link Registry} using the indicated module classes.
   * The Registry will be shutdown after each feature method.
   *
   * @param moduleClasses classes to include when building the Registry
   */
  protected final void buildRegistry(Class... moduleClasses) {

    registry = new RegistryBuilder().add(moduleClasses).build()
  }

  /** Any unrecognized methods are evaluated against the registry. */
  def methodMissing(String name, args) {
    registry."$name"(* args)
  }


}
