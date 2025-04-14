package ioc.specs

import org.apache.tapestry5.ioc.test.internal.DecorateWithSymbolModule
import org.apache.tapestry5.ioc.test.internal.FoeService

import spock.lang.Issue

@Issue("TAP5-2758")
class DecorateWithSymbolSpec extends AbstractRegistrySpecification {


  def "ServiceDef obtainable by service id"() {
    given:
    buildRegistry DecorateWithSymbolModule

    when:

    def service = getService "FoeService", FoeService

    then:
    service.foe() == DecorateWithSymbolModule.SYMBOL_VALUE
    service.foe() != DecorateWithSymbolModule.ORIGINAL_VALUE
    }
}
