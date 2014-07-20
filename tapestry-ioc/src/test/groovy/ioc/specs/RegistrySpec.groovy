package ioc.specs

import org.apache.tapestry5.ioc.BarneyModule;
import org.apache.tapestry5.ioc.CatchAllServiceConfigurationListener;
import org.apache.tapestry5.ioc.Greeter
import org.apache.tapestry5.ioc.FredModule
import org.apache.tapestry5.ioc.GreeterModule
import org.apache.tapestry5.ioc.HelterModule
import org.apache.tapestry5.ioc.NameListHolder;
import org.apache.tapestry5.ioc.StringLookup;
import org.apache.tapestry5.ioc.internal.services.StartupModule2

class RegistrySpec extends AbstractRegistrySpecification {

  def "symbol in Registry.getService() is expanded"() {

    buildRegistry GreeterModule

    when:

    def greeter = getService '${greeter}', Greeter

    then:

    greeter.greeting == "Hello"
    greeter.toString() == "<Proxy for HelloGreeter(org.apache.tapestry5.ioc.Greeter)>"
  }

  def "circular module references are ignored"() {
    buildRegistry HelterModule

    when:

    def helter = getService "Helter", Runnable
    def skelter = getService "Skelter", Runnable

    then:

    !helter.is(skelter)
  }

  def "@Startup annotation support"() {
    when:

    buildRegistry StartupModule2

    then:

    !StartupModule2.staticStartupInvoked
    !StartupModule2.instanceStartupInvoked

    when:

    performRegistryStartup()

    then:

    StartupModule2.staticStartupInvoked
    StartupModule2.instanceStartupInvoked
  }
  
  def "ServiceConfigurationListener"() {
      
    buildRegistry FredModule, BarneyModule
    
    // for a given service, its configuration is only notified to the ServiceConfigurationListeners 
    // when the service itself is realized, so we call one method of each to force their realization.
    getService(StringLookup).keys()
    getService("OrderedNames", NameListHolder).getNames()
    getService("UnorderedNames", NameListHolder).getNames()
      
    when:
      
    def listener = getService CatchAllServiceConfigurationListener
    def mappedConfiguration = listener.getMappedConfigurations().get("StringLookup")
    def orderedConfiguration = listener.getOrderedConfigurations().get("OrderedNames");
    def unorderedConfiguration = listener.getUnorderedConfigurations().get("UnorderedNames");
      
    then:
    mappedConfiguration.size() == 4
    mappedConfiguration.get("fred").equals("FRED")
    mappedConfiguration.get("wilma").equals("WILMA")
    mappedConfiguration.get("barney").equals("BARNEY")
    mappedConfiguration.get("betty").equals("BETTY")
    orderedConfiguration == ['BARNEY', 'FRED']
    unorderedConfiguration.size() == 3
    unorderedConfiguration.contains 'UnorderedNames'
    unorderedConfiguration.contains 'Beta'
    unorderedConfiguration.contains 'Gamma'
    
  }
      
}
