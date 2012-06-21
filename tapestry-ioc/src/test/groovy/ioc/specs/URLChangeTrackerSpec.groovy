package ioc.specs

import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl
import org.apache.tapestry5.ioc.internal.util.URLChangeTracker
import org.apache.tapestry5.ioc.services.ClasspathURLConverter
import spock.lang.Shared
import spock.lang.Specification

class URLChangeTrackerSpec extends Specification {

  @Shared
  ClasspathURLConverter converter = new ClasspathURLConverterImpl()

  def tracker = new URLChangeTracker(converter)

  def "new instance does not contain changes"() {
    expect:

    !tracker.containsChanges()
  }

  def "adding a null URL returns 0"() {

    expect:
    tracker.add(null) == 0l
  }

  def touch(file) {

    def initial = file.lastModified()
    def index = 0

    while (true) {

      file.lastModified = System.currentTimeMillis()

      if (file.lastModified() != initial) { return }

      Thread.sleep(50 * 2 ^ index++)
    }
  }

  def "add a file, touch it, and ensure that the change is noticed"() {
    def f = newFile()

    when:

    tracker.add(f.toURL())

    then:

    // one for the file, one for its directory

    tracker.trackedFileCount() == 2
    !tracker.containsChanges()

    when:

    touch(f)

    then:

    tracker.containsChanges()
  }

  def File newFile() {
    File.createTempFile("changetracker0", ".tmp")
  }

  def "creating a new file in an existing tracker folder is a change"() {

    def first = newFile()
    def dir = first.getParentFile()

    when:

    tracker.add(first.toURL())

    then:

    !tracker.containsChanges()

    when:

    def initial = dir.lastModified()
    def index = 0

    while (true) {
      newFile()

      if (dir.lastModified() != initial) { break; }
      Thread.sleep(50 * 2 ^ index++)
    }

    then:

    tracker.containsChanges()
  }

  def "non-file URLs are ignored"() {

    when:

    tracker.add(new URL("http://google.com"))

    then:

    tracker.trackedFileCount() == 0
  }

  def "caching of URLs and timestamps"() {

    def file = newFile()
    def url = file.toURL()

    def initial = tracker.add(url)

    when:

    touch(file)

    then:

    tracker.add(url) == initial

    tracker.containsChanges()

    when:

    tracker.clear()

    then:

    tracker.add(url) != initial
  }

  def "deleting a file shows as changes"() {
    def file = newFile()
    def url = file.toURL()

    when:

    def initial = tracker.add(url)

    then:

    initial > 0
    !tracker.containsChanges()

    when:

    file.delete()

    then:

    tracker.containsChanges()
  }

  def "can track changes at a 1-second granularity (rather than millisecond)"() {
    tracker = new URLChangeTracker(converter, true, true)

    def file = newFile()

    when:

    long initial = tracker.add(file.toURL())

    then:

    initial % 1000 == 0

    when:

    Thread.sleep 1500

    touch(file)

    then:

    tracker.containsChanges()

    def updated = tracker.add(file.toURL())

    updated % 1000 == 0
    updated != initial
  }
}
