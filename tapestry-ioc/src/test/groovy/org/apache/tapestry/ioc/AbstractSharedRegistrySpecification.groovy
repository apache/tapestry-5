package org.apache.tapestry.ioc

import org.apache.tapestry5.ioc.IOCUtilities
import org.apache.tapestry5.ioc.Registry
import spock.lang.Specification

/** Uses a static, shared instance of the Registry.  All specifications that extend from this class will share
 * a single instance of the Registry, that is created by whatever specification is created first. */
abstract class AbstractSharedRegistrySpecification extends Specification {

  static Registry registry;

  /** Any unrecognized methods are evaluated against the shared Registry instance. */
  def methodMissing(String name, args) {
    registry."$name"(* args)
  }

  /** Creates the Registry if it does not already exist. */
  def setupSpec() {
    if (registry == null) {
      registry = IOCUtilities.buildDefaultRegistry()
    }

    helpSetupSpec()
  }

  /** Does nothing; override in subclasses to do work that should occur inside setupSpec(). */
  def helpSetupSpec() {

  }

  /** Invokes {@link Registry#cleanupThread()}. */
  def cleanupSpec() {
    registry.cleanupThread();

    helpCleanupSpec()
  }

  /** Does nothing; override in subclasses to do work that should occur inside cleanupSpec(), after {@link Registry#cleanupThread()}. */
  def helpCleanupSpec() {

  }

  // TODO: the Registry is never shutdown, since there's no notification
  // that all tests are completing.

}
