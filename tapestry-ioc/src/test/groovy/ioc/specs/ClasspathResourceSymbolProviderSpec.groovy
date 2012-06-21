package ioc.specs

import org.apache.tapestry5.ioc.internal.services.ClasspathResourceSymbolProvider
import spock.lang.Shared
import spock.lang.Specification

class ClasspathResourceSymbolProviderSpec extends Specification {

  static final String PATH = "org/apache/tapestry5/ioc/internal/services/foo.properties"

  @Shared
  def provider = new ClasspathResourceSymbolProvider(PATH)

  def "access properties"() {

    expect:
    provider.valueForSymbol("homer") == "simpson"
    provider.valueForSymbol("monty") == "burns"
  }

  def "keys are case insensitive"() {
    expect:
    provider.valueForSymbol("HOMER") == "simpson"
  }

  def "non-existent keys should return null"() {
    expect:
    provider.valueForSymbol("marge") == null
  }

}
