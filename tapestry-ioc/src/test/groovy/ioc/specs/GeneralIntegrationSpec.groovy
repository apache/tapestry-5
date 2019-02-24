package ioc.specs

import org.apache.tapestry5.ioc.ObjectLocator
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.internal.services.Bean
import org.apache.tapestry5.ioc.services.PropertyAccess
import org.hibernate.Session
import org.hibernate.cfg.Configuration

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
  
  def "Avoiding duplicated method implementation in service proxies"() {
      when:
      Registry registry = RegistryBuilder.buildAndStartupRegistry(TestModule.class);
      then:
      // Throws exception without fix.
      Session session = registry.getService(Session.class);
  }

  public static final class TestModule 
  {
      public static Session buildHibernateSession(
          ObjectLocator objectLocator
      ) {
          return new Configuration()
              .configure("hibernate.cfg.xml")
              .buildSessionFactory()
              .openSession();
      }
  }

}
