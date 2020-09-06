package ioc.specs

import org.apache.tapestry5.commons.AnnotationProvider
import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.ObjectProvider
import org.apache.tapestry5.ioc.OperationTracker
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import org.apache.tapestry5.ioc.internal.services.MasterObjectProviderImpl
import org.apache.tapestry5.ioc.services.MasterObjectProvider

import spock.lang.Shared
import spock.lang.Specification

class MasterObjectProviderImplSpec extends Specification {

  @Shared
  OperationTracker tracker = new QuietOperationTracker()

  def "found match via first provider"() {
    ObjectProvider prov1 = Mock()
    ObjectProvider prov2 = Mock()
    AnnotationProvider ap = Mock()
    ObjectLocator locator = Mock()
    Runnable expected = Mock()

    MasterObjectProvider mop = new MasterObjectProviderImpl([prov1, prov2], tracker)

    when:

    assert mop.provide(Runnable, ap, locator, true).is(expected)

    then:

    1 * prov1.provide(Runnable, ap, locator) >> expected
    0 * _
  }

  def "found match after first provider"() {
    ObjectProvider prov1 = Mock()
    ObjectProvider prov2 = Mock()
    AnnotationProvider ap = Mock()
    ObjectLocator locator = Mock()
    Runnable expected = Mock()

    MasterObjectProvider mop = new MasterObjectProviderImpl([prov1, prov2], tracker)

    when:

    assert mop.provide(Runnable, ap, locator, true).is(expected)

    then:

    1 * prov1.provide(Runnable, ap, locator) >> null

    then:

    1 * prov2.provide(Runnable, ap, locator) >> expected
    0 * _
  }

  def "no match found on optional search returns null"() {
    ObjectProvider prov1 = Mock()
    ObjectProvider prov2 = Mock()
    AnnotationProvider ap = Mock()
    ObjectLocator locator = Mock()

    MasterObjectProvider mop = new MasterObjectProviderImpl([prov1, prov2], tracker)

    when:

    assert mop.provide(Runnable, ap, locator, false) == null

    then:

    1 * prov1.provide(Runnable, ap, locator) >> null

    then:

    1 * prov2.provide(Runnable, ap, locator) >> null
    0 * _
  }

  def "no match for a required search delegates to the ObjectLocator.getService(Class)"() {
    ObjectProvider prov1 = Mock()
    ObjectProvider prov2 = Mock()
    AnnotationProvider ap = Mock()
    ObjectLocator locator = Mock()
    Runnable expected = Mock()

    MasterObjectProvider mop = new MasterObjectProviderImpl([prov1, prov2], tracker)

    when:

    assert mop.provide(Runnable, ap, locator, true).is(expected)

    then:

    1 * prov1.provide(Runnable, ap, locator) >> null

    then:

    1 * prov2.provide(Runnable, ap, locator) >> null

    then:

    1 * locator.getService(Runnable) >> expected

    0 * _

  }
}
