package ioc.specs

import org.apache.tapestry5.ioc.services.ThreadLocale


class ThreadLocaleImplSpec extends AbstractSharedRegistrySpecification {

  ThreadLocale threadLocale = getService ThreadLocale

  static final Locale FAKE_LOCALE1 = new Locale("klingon"),
                      FAKE_LOCALE2 = new Locale("ferrengi")

  def cleanup() {
    cleanupThread()
  }

  def "different threads track different values"() {
    def initial = threadLocale.locale
    def perthread

    when:

    threadLocale.locale = FAKE_LOCALE1

    then:

    threadLocale.locale.is(FAKE_LOCALE1)

    when:

    def thread = new Thread({
      perthread = threadLocale.locale
    })

    thread.start()
    thread.join()

    then:

    perthread.is(initial)

  }

  def "per-thread locale reverts after cleanup"() {
    def initial = threadLocale.locale

    threadLocale.locale = FAKE_LOCALE2

    when:

    cleanupThread()

    then:

    threadLocale.locale.is(initial)
  }
}
