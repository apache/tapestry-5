package ioc.specs

import org.apache.tapestry5.ioc.services.SymbolSource
import org.apache.tapestry5.ioc.test.BarneyModule
import org.apache.tapestry5.ioc.test.CaseInsensitiveContributeMethodModule
import org.apache.tapestry5.ioc.test.ConfigurationOverrideModule
import org.apache.tapestry5.ioc.test.ContributeByClassModule
import org.apache.tapestry5.ioc.test.ContributedValueCoercionModule
import org.apache.tapestry5.ioc.test.ContributionOrderModule
import org.apache.tapestry5.ioc.test.ContributionOrderModule2
import org.apache.tapestry5.ioc.test.ContributionOrderModule3
import org.apache.tapestry5.ioc.test.ContributionOrderModule4
import org.apache.tapestry5.ioc.test.DuplicateConfigurationOverrideModule
import org.apache.tapestry5.ioc.test.FailedConfigurationOverrideModule
import org.apache.tapestry5.ioc.test.FredModule
import org.apache.tapestry5.ioc.test.InjectionCheck
import org.apache.tapestry5.ioc.test.InjectionCheckModule
import org.apache.tapestry5.ioc.test.InvalidContributeDefModule
import org.apache.tapestry5.ioc.test.InvalidContributeDefModule2
import org.apache.tapestry5.ioc.test.InvalidContributeDefModule3
import org.apache.tapestry5.ioc.test.NameListHolder
import org.apache.tapestry5.ioc.test.NameListHolder2
import org.apache.tapestry5.ioc.test.OptionalContributionModule
import org.apache.tapestry5.ioc.test.OrderedService
import org.apache.tapestry5.ioc.test.Sizer
import org.apache.tapestry5.ioc.test.StringLookup
import org.apache.tapestry5.ioc.test.StringTransformer
import org.apache.tapestry5.ioc.test.internal.AlphabetModule
import org.apache.tapestry5.ioc.test.internal.AlphabetModule2
import org.apache.tapestry5.ioc.test.util.NonmatchingMappedConfigurationOverrideModule

/** Integration tests for various types of service configurations. */
class ConfigurationsSpec extends AbstractRegistrySpecification {

  def "all contributions to unordered configuration are collected"() {

    buildRegistry FredModule, BarneyModule

    when:

    def holder = getService "UnorderedNames", NameListHolder

    then:

    // We don't know the actual contribution order, the impl sorts them. Just
    // check they are all present.

    holder.names == ["Beta", "Gamma", "UnorderedNames"]
  }

  def "all contributions to order configuration are collected"() {
    buildRegistry FredModule, BarneyModule

    when:

    def holder = getService "OrderedNames", NameListHolder

    then:

    holder.names == ["BARNEY", "FRED"]
  }

  def "all contribution to mapped configuration are collected"() {

    buildRegistry FredModule, BarneyModule

    def sizer = getService "Sizer", Sizer

    expect:

    // The contributions map different Classes to strategies; this demonstrates
    // that all contributions have been mapped and provided to Sizer service impl.

    sizer.size(null) == 0

    sizer.size([1, 2, 3]) == 3

    sizer.size([fred: "flintstone", barney: "rubble"]) == 2

    sizer.size(this) == 1
  }

  def "can contribute a class to an unordered configuration"() {
    buildRegistry ContributeByClassModule

    when:

    def tx = getService "MasterStringTransformer", StringTransformer

    then:

    tx.transform("Tapestry") == "TAPESTRY"
  }

  def "can contribute a class to an ordered configuration"() {
    buildRegistry ContributeByClassModule

    when:

    def tx = getService "StringTransformerChain", StringTransformer

    then:

    tx.transform("Tapestry") == "TAPESTRY"
  }

  def "can contribute class to a mapped configuration"() {
    buildRegistry ContributeByClassModule

    when:

    def tx = getService "MappedStringTransformer", StringTransformer

    then:

    tx.transform("Tapestry") == "TAPESTRY"
  }

  def "contribution to an unknown configuration is detected as an exception"() {
    when:
    buildRegistry InvalidContributeDefModule
    then:
    IllegalArgumentException e = thrown()

    e.message.contains "Contribution org.apache.tapestry5.ioc.test.InvalidContributeDefModule.contributeDoesNotExist(Configuration)"
    e.message.contains "is for service 'DoesNotExist', which does not exist."
  }

  def "a value in an ordered configuration may be overridden"() {
    buildRegistry FredModule, BarneyModule, ConfigurationOverrideModule

    when:

    def holder = getService "OrderedNames", NameListHolder

    then:

    holder.names == ["BARNEY", "WILMA", "Mr. Flintstone"]
  }

  def "an override value in an ordered configuration must match a normally contributed value"() {
    buildRegistry FredModule, BarneyModule, FailedConfigurationOverrideModule

    def holder = getService "OrderedNames", NameListHolder

    when:


    holder.names

    then:

    RuntimeException e = thrown()

    e.message.contains "Failure processing override from org.apache.tapestry5.ioc.test.FailedConfigurationOverrideModule.contributeOrderedNames(OrderedConfiguration)"
    e.message.contains "Override for object 'wilma' is invalid as it does not match an existing object."
  }

  def "a contribution to an ordered configuration may only be overridden once"() {
    buildRegistry FredModule, BarneyModule, ConfigurationOverrideModule, DuplicateConfigurationOverrideModule

    def holder = getService "OrderedNames", NameListHolder

    when:


    holder.names

    then:

    RuntimeException e = thrown()

    e.message.contains "Error invoking service contribution method"
    e.message.contains "Contribution 'fred' has already been overridden"
  }

  def "contributions to mapped configurations may be overridden"() {
    buildRegistry FredModule, BarneyModule, ConfigurationOverrideModule

    when:

    def sl = getService StringLookup

    then:

    sl.keys() == ["barney", "betty", "fred"]
    sl.lookup("fred") == "Mr. Flintstone"
  }

  def "mapped configuration overrides must match an existing value"() {
    buildRegistry FredModule, BarneyModule, NonmatchingMappedConfigurationOverrideModule

    def sl = getService StringLookup

    when:

    sl.keys()

    then:

    RuntimeException e = thrown()

    e.message.contains "Override for key alley cat (at org.apache.tapestry5.ioc.test.util.NonmatchingMappedConfigurationOverrideModule.contributeStringLookup(MappedConfiguration)"
    e.message.contains "does not match an existing key"
  }

  def "a contribution to a mapped configuration may only be overridden once"() {
    buildRegistry FredModule, BarneyModule, ConfigurationOverrideModule, DuplicateConfigurationOverrideModule

    def sl = getService StringLookup

    when:

    sl.keys()

    then:

    RuntimeException e = thrown()

    e.message.contains "Error invoking service contribution method"
    e.message.contains "Contribution key fred has already been overridden"
  }

  def "support for @Contribute annotation"() {

    buildRegistry AlphabetModule, AlphabetModule2

    when:

    def greek = getService "Greek", NameListHolder

    then:

    greek.names == ["Alpha", "Beta", "Gamma", "Delta"]

    when:

    def anotherGreek = getService "AnotherGreek", NameListHolder

    then:

    anotherGreek.names == ["Alpha", "Beta", "Gamma", "Delta", "Epsilon"]

    when:

    def hebrew = getService "Hebrew", NameListHolder

    then:

    hebrew.names == ["Alef", "Bet", "Gimel", "Dalet", "He", "Vav"]

    when:

    def holder = getService "ServiceWithEmptyConfiguration", NameListHolder2

    then:

    holder.names.empty
  }

  def "contribute by @Contribute annotation to non-existent service"() {
    when:

    buildRegistry InvalidContributeDefModule2

    then:

    RuntimeException e = thrown()

    ["Contribution org.apache.tapestry5.ioc.test.InvalidContributeDefModule2.provideConfiguration(OrderedConfiguration)",
        "is for service 'interface org.apache.tapestry5.ioc.test.NameListHolder'",
        "qualified with marker annotations [",
        "interface org.apache.tapestry5.ioc.test.BlueMarker",
        "interface org.apache.tapestry5.ioc.test.RedMarker",
        "], which does not exist."].every { e.message.contains it}
  }

  def "contribute using @Contribute using invalid marker annotation is an exception"() {
    when:
    buildRegistry InvalidContributeDefModule3
    then:
    RuntimeException e = thrown()

    ["Contribution org.apache.tapestry5.ioc.test.InvalidContributeDefModule3.provideConfiguration(OrderedConfiguration)",
        "is for service 'interface org.apache.tapestry5.ioc.test.NameListHolder'",
        "qualified with marker annotations [interface org.apache.tapestry5.ioc.test.BlueMarker], which does not exist."].every
        { e.message.contains it}
  }

  def "ServiceResources are available to contribution methods"() {
    buildRegistry InjectionCheckModule

    when:

    def s = getService InjectionCheck
    def il = s.getValue "indirect-resources"

    then:

    s.logger.is(s.getValue("logger"))

    s.logger.is il.logger
    s.logger.is il.resources.logger
  }


  def "service id in contribute method is matched caselessly"() {
    buildRegistry CaseInsensitiveContributeMethodModule

    when:

    def ss = getService SymbolSource

    then:

    ss.valueForSymbol("it") == "works"
  }

  def "contributed values may be coerced to the correct type"() {
    buildRegistry ContributedValueCoercionModule

    when:

    def ss = getService SymbolSource

    then:

    ss.valueForSymbol("bool-true") == "true"
    ss.valueForSymbol("bool-false") == "false"
    ss.valueForSymbol("num-12345") == "12345"
  }

  def "@Optional contribution to an unknown service is not an error"() {
    when:
    buildRegistry OptionalContributionModule

    then:
    noExceptionThrown()
  }
  
  // TAP5-2358
  def "OrderedConfiguration should have consistent ordering"() {

      when:
      buildRegistry ContributionOrderModule, ContributionOrderModule2, ContributionOrderModule3, ContributionOrderModule4
      def configuration1 = getService(OrderedService).getContributions();
      
      buildRegistry ContributionOrderModule4, ContributionOrderModule3, ContributionOrderModule2, ContributionOrderModule
      def configuration2 = getService(OrderedService).getContributions();

      then:
      configuration1.equals(configuration2)
          
  }
  
  // TAP5-2649
  def "Configuration should have consistent ordering"() {

      when:
      buildRegistry ContributionOrderModule, ContributionOrderModule2, ContributionOrderModule3, ContributionOrderModule4
      def configuration1 = getService(OrderedService).getContributions();
      
      buildRegistry ContributionOrderModule4, ContributionOrderModule3, ContributionOrderModule2, ContributionOrderModule
      def configuration2 = getService(OrderedService).getContributions();

      then:
      configuration1.equals(configuration2)
          
  }

  
}
