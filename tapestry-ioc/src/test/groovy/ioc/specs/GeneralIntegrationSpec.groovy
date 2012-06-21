package ioc.specs

import org.apache.tapestry5.ioc.internal.services.Bean
import org.apache.tapestry5.ioc.services.PropertyAccess

class GeneralIntegrationSpec extends AbstractSharedRegistrySpecification {

  def "PropertyAccess service is available"() {

    PropertyAccess pa = getService "PropertyAccess", PropertyAccess

    Bean b = new Bean()

    when:

    pa.set(b, "value", 99)

    then:

    b.value == 99
    pa.get(b, "value") == 99
  }


}
