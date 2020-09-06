package ioc.specs

import org.apache.tapestry5.commons.services.PlasticProxyFactory
import org.apache.tapestry5.ioc.AdvisorDef2
import org.apache.tapestry5.ioc.def.*
import org.apache.tapestry5.ioc.internal.*
import org.apache.tapestry5.ioc.test.BlueMarker
import org.apache.tapestry5.ioc.test.RedMarker
import org.apache.tapestry5.ioc.test.internal.FieService
import org.apache.tapestry5.ioc.test.internal.ModuleImplTestModule
import org.apache.tapestry5.ioc.test.internal.services.ToStringService
import org.slf4j.Logger

import spock.lang.Specification

class ModuleImplSpec extends Specification {

  Logger logger = Mock()
  InternalRegistry registry = Mock()
  PlasticProxyFactory proxyFactory = Mock()
  ServiceActivityTracker tracker = Mock()

  def "findServiceIdsForInterface() test"() {

    ModuleDef md = new DefaultModuleDefImpl(ModuleImplTestModule, logger, proxyFactory)

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    def serviceIds = module.findServiceIdsForInterface(FieService)

    then:

    serviceIds.size() == 2
    serviceIds.containsAll(["Fie", "OtherFie"])
  }

  def "findMatchingDecoratorDefs() with exact DecoratorDef match"() {
    ServiceDef sd = Mock()
    DecoratorDef2 def1 = Mock()
    DecoratorDef def2 = Mock()
    ModuleDef md = Mock()

    def decoratorDefs = [def1, def2] as Set

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    then:

    1 * md.serviceIds >> Collections.EMPTY_SET

    when:

    def matches = module.findMatchingDecoratorDefs(sd)

    then:

    matches.size() == 1
    matches.contains def2

    1 * md.decoratorDefs >> decoratorDefs

    1 * sd.serviceInterface >> Runnable

    1 * def1.matches(sd) >> false

    // Maybe not a complete match, so does it match by type & markers?
    1 * def1.serviceInterface >> ToStringService

    // An exact match
    1 * def2.matches(sd) >> true

    0 * _
  }

  def "findDecoratorDefs() with matching service but non-matching marker annotations"() {
    ServiceDef sd = Mock()
    DecoratorDef2 def1 = Mock()
    DecoratorDef def2 = Mock()
    ModuleDef md = Mock()

    def decoratorDefs = [def1, def2] as Set
    def def1markers = [BlueMarker] as Set
    def sdmarkers = [RedMarker] as Set
    def registrymarkers = [RedMarker, BlueMarker] as Set

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    then:

    1 * md.serviceIds >> Collections.EMPTY_SET

    when:

    def matches = module.findMatchingDecoratorDefs(sd)

    then:

    matches.size() == 1
    matches.contains def2

    1 * md.decoratorDefs >> decoratorDefs

    1 * def1.matches(sd) >> false
    1 * def1.serviceInterface >> Object
    _ * sd.serviceInterface >> Runnable
    1 * def1.markers >> def1markers
    1 * sd.markers >> sdmarkers

    1 * def2.matches(sd) >> true

    1 * registry.markerAnnotations >> registrymarkers

    0 * _
  }

  def "findMatchingServiceAdvisors() where the advise is for a different interface than the service"() {
    AdvisorDef2 def1 = Mock()
    AdvisorDef2 def2 = Mock()
    ModuleDef2 md = Mock()
    ServiceDef sd = Mock()

    def advisors = [def1, def2] as Set

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    then:

    1 * md.serviceIds >> Collections.EMPTY_SET

    when:

    def matches = module.findMatchingServiceAdvisors(sd)

    then:

    matches.size() == 1
    matches.contains def2

    1 * md.advisorDefs >> advisors

    1 * def1.matches(sd) >> false
    1 * def1.serviceInterface >> ToStringService

    1 * sd.serviceInterface >> Runnable

    1 * def2.matches(sd) >> true

    0 * _
  }

  def "findMatchingServiceAdvisors() where the advice is for a matching service type but non-matching marker annotations"() {
    AdvisorDef2 def1 = Mock()
    AdvisorDef2 def2 = Mock()
    ModuleDef2 md = Mock()
    ServiceDef sd = Mock()

    def advisors = [def1, def2] as Set
    def def1markers = [BlueMarker] as Set
    def registrymarkers = [BlueMarker, RedMarker] as Set
    def servicemarkers = [RedMarker] as Set

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    then:

    1 * md.serviceIds >> Collections.EMPTY_SET

    when:

    def matches = module.findMatchingServiceAdvisors(sd)

    then:

    matches.size() == 1
    matches.contains def2

    1 * registry.markerAnnotations >> registrymarkers

    1 * md.advisorDefs >> advisors

    1 * def1.matches(sd) >> false
    1 * def1.serviceInterface >> Object

    1 * sd.serviceInterface >> Runnable
    1 * sd.markers >> servicemarkers

    1 * def1.markers >> def1markers

    1 * def2.matches(sd) >> true

    0 * _
  }

  def "findMatchingServiceAdvisors() match on type and marker annotations"() {
    AdvisorDef2 ad = Mock()
    ModuleDef2 md = Mock()
    ServiceDef sd = Mock()

    def advisors = [ad] as Set
    def admarkers = [RedMarker] as Set
    def registrymarkers = [BlueMarker, RedMarker] as Set
    def servicemarkers = [RedMarker] as Set

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    then:

    1 * md.serviceIds >> Collections.EMPTY_SET

    when:

    def matches = module.findMatchingServiceAdvisors(sd)

    then:

    matches.size() == 1
    matches.contains ad

    1 * registry.markerAnnotations >> registrymarkers

    1 * md.advisorDefs >> advisors

    1 * ad.matches(sd) >> false
    1 * ad.serviceInterface >> Object

    1 * sd.serviceInterface >> Runnable
    1 * sd.markers >> servicemarkers

    1 * ad.markers >> admarkers

    0 * _
  }


  def "findMatchingServiceAdvisors() where there are no marker annotations at all"() {
    AdvisorDef2 ad = Mock()
    ModuleDef2 md = Mock()
    ServiceDef sd = Mock()

    def advisors = [ad] as Set

    when:

    Module module = new ModuleImpl(registry, tracker, md, proxyFactory, logger)

    then:

    1 * md.serviceIds >> Collections.EMPTY_SET

    when:

    def matches = module.findMatchingServiceAdvisors(sd)

    then:

    matches.size() == 0

    1 * registry.markerAnnotations >> Collections.EMPTY_SET

    1 * md.advisorDefs >> advisors

    1 * ad.matches(sd) >> false
    1 * ad.serviceInterface >> Object

    1 * sd.serviceInterface >> Runnable

    1 * ad.markers >> Collections.EMPTY_SET

    0 * _
  }

}
