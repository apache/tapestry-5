package org.apache.tapestry5.ioc

import spock.lang.Specification


class ManifestProcessingSpec extends Specification {

  def "invalid class in manifest"() {

    File fakejar = new File("src/test/fakejar")

    expect:

    // This is more to verify the module execution environment
    fakejar.exists()
    fakejar.isDirectory()

    when:

    URL url = fakejar.toURL()
    URLClassLoader loader = new URLClassLoader([url] as URL[], Thread.currentThread().contextClassLoader)

    RegistryBuilder builder = new RegistryBuilder(loader)

    IOCUtilities.addDefaultModules(builder)

    then:

    RuntimeException e = thrown()

    e.message.contains "Exception loading module(s) from manifest"
    e.message.contains "Failure loading Tapestry IoC module class does.not.exist.Module"


  }
}
