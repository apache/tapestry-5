package ioc.specs

import org.apache.tapestry5.commons.ObjectLocator
import org.apache.tapestry5.commons.services.PropertyAccess
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.ServiceBinder
import org.apache.tapestry5.ioc.test.DefaultMethodService
import org.apache.tapestry5.ioc.test.internal.services.Bean
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

  def "Methods overriding default methods should actually be called"() {
      when:
      Registry registry = RegistryBuilder.buildAndStartupRegistry(TestModule.class);
      DefaultMethodService defaultMethodService = registry.getService(DefaultMethodService.class)
      then:
      defaultMethodService.notOverriden() == "Default";
      defaultMethodService.overriden() == "Impl";
  }
  
  public static class DefaultMethodServiceImpl implements DefaultMethodService {
      public String overriden() {
          return "Impl";
      }
  }

  public static final class TestModule
  {
      public static void bind(ServiceBinder binder) {
          binder.bind(DefaultMethodService.class, DefaultMethodServiceImpl.class);
      }
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
