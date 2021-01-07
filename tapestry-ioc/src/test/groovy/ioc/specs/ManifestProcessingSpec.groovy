package ioc.specs

import com.example.ManifestModule

import org.apache.tapestry5.ioc.IOCUtilities
import org.apache.tapestry5.ioc.RegistryBuilder

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

  def "valid class in manifest"() {

    given:

    File jar = new File("src/test/realjar")

    expect:

    // This is more to verify the module execution environment
    jar.exists()
    jar.isDirectory()

    when:

    URL url = jar.toURL()
    URLClassLoader loader = new URLClassLoader([url] as URL[], Thread.currentThread().contextClassLoader)

    RegistryBuilder builder = new RegistryBuilder(loader)

    IOCUtilities.addDefaultModules(builder)

    def reg = builder.build()
    reg.performRegistryStartup()

    then:

    noExceptionThrown()

    ManifestModule.startupCalled == true

    cleanup:

    ManifestModule.startupCalled = false
  }

  def "blacklisted manifest module not loaded"() {

    given:

    File jar = new File("src/test/realjar")

    def props = System.getProperties()
    props.setProperty("tapestry.manifest-modules-blacklist", "does.not.ExistModule,com.example.ManifestModule")

    expect:

    // This is more to verify the module execution environment
    jar.exists()
    jar.isDirectory()

    when:

    URL url = jar.toURL()
    URLClassLoader loader = new URLClassLoader([url] as URL[], Thread.currentThread().contextClassLoader)

    RegistryBuilder builder = new RegistryBuilder(loader)

    IOCUtilities.addDefaultModules(builder)

    def reg = builder.build()
    reg.performRegistryStartup()

    then:

    noExceptionThrown()

    ManifestModule.startupCalled == false

    cleanup:

    props.remove("tapestry.manifest-modules-blacklist")
    ManifestModule.startupCalled = false
  }

  def "blacklisted manifest module empty"() {

    given:

    File jar = new File("src/test/realjar")

    def props = System.getProperties()
    props.setProperty("tapestry.manifest-modules-blacklist", "")

    expect:

    // This is more to verify the module execution environment
    jar.exists()
    jar.isDirectory()

    when:

    URL url = jar.toURL()
    URLClassLoader loader = new URLClassLoader([url] as URL[], Thread.currentThread().contextClassLoader)

    RegistryBuilder builder = new RegistryBuilder(loader)

    IOCUtilities.addDefaultModules(builder)

    def reg = builder.build()
    reg.performRegistryStartup()

    then:

    noExceptionThrown()

    ManifestModule.startupCalled == true

    cleanup:

    props.remove("tapestry.manifest-modules-blacklist")
    ManifestModule.startupCalled = false
  }

}
