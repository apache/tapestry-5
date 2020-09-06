package ioc.specs

import org.apache.tapestry5.commons.Resource
import org.apache.tapestry5.ioc.internal.services.ResourceSymbolProvider

import spock.lang.Specification

class ResourceSymbolProviderSpec extends Specification {

  static final CONTENT = 'homer=simpson\r\nmonty=burns'

  def "access to contents of stream"() {
    Resource resource = Mock()

    when:

    ResourceSymbolProvider provider = new ResourceSymbolProvider(resource)

    then:

    1 * resource.openStream() >> { new ByteArrayInputStream(CONTENT.bytes) }

    expect:

    provider.valueForSymbol("homer") == "simpson"
    provider.valueForSymbol("monty") == "burns"

    provider.valueForSymbol("HOMER") == "simpson"

    provider.valueForSymbol("marge") == null

  }
}
