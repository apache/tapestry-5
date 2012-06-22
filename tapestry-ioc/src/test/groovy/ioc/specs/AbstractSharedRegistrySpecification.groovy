package ioc.specs

import org.apache.tapestry5.ioc.IOCUtilities
import org.apache.tapestry5.ioc.OperationTracker
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * Uses a static, shared instance of the {@link org.apache.tapestry5.ioc.Registry}.
 * All specifications that extend from this class will share
 * a single instance of the Registry; The Registry is created by whatever specification is created first.
 * Missing method invocations are forwarded to the registry instance. */
abstract class AbstractSharedRegistrySpecification extends Specification {

  static Registry registry

  static OperationTracker tracker = new QuietOperationTracker()

  /** Any unrecognized methods are evaluated against the shared Registry instance. */
  def methodMissing(String name, args) {
    registry."$name"(* args)
  }

  /** Creates the Registry if it does not already exist. */
  def setupSpec() {
    if (registry == null) {
      registry = IOCUtilities.buildDefaultRegistry()
    }
  }

  /** Invokes {@link Registry#cleanupThread()}. */
  def cleanupSpec() {
    registry.cleanupThread();
  }

  // TODO: the Registry is never shutdown, since there's no notification
  // that all tests are completing.

  protected Method findMethod(Object subject, String methodName) {
    def method = subject.class.methods.find { it.name == methodName }

    assert method != null

    return method
  }

}
