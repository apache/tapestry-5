package ioc.specs

import org.apache.tapestry5.commons.util.StrategyRegistry
import org.apache.tapestry5.commons.util.UnknownValueException

import spock.lang.Specification

class StrategyRegistrySpec extends Specification {

  def "check exception when an adaptor is not found"() {
    Runnable r1 = Mock()
    Runnable r2 = Mock()

    StrategyRegistry reg = StrategyRegistry.newInstance(Runnable, [
        (List): r1,
        (Map): r2
    ])

    when:

    reg.get(Set)

    then:

    UnknownValueException e = thrown()

    e.message == "No adapter from type java.util.Set to type java.lang.Runnable is available."
    e.availableValues.toString() == "AvailableValues[registered types: interface java.util.List, interface java.util.Map]"

  }

  def "access to types registered"() {
    Runnable r1 = Mock()
    Runnable r2 = Mock()

    when:

    StrategyRegistry sr = StrategyRegistry.newInstance(Runnable, [
        (List): r1,
        (Map): r2
    ])

    then:

    sr.types.size == 2
    sr.types.containsAll(List, Map)
  }

  def "locate an adapter based on interface inheritance"() {

    Runnable r1 = Mock()
    Runnable r2 = Mock()

    when:

    StrategyRegistry sr = StrategyRegistry.newInstance(Runnable, [
        (List): r1,
        (Map): r2
    ])

    def arrayListAdapter = sr.get(ArrayList)

    then:

    arrayListAdapter.is r1

    when:

    def adapter2 = sr.get(ArrayList)

    then:

    adapter2.is r1

    when:

    sr.clearCache()

    def adapter3 = sr.get(ArrayList)

    then:

    adapter3.is r1
  }

  def "the registration map passed to the constructor is copied"() {

    Runnable r1 = Mock()
    Runnable r2 = Mock()

    def registrations = [
        (List): r1,
        (Map): r2
    ]

    when:

    StrategyRegistry sr = StrategyRegistry.newInstance(Runnable, registrations)

    registrations.clear()

    then:

    sr.get(ArrayList).is(r1)
  }

  def "adapter found from an instance"() {

    Runnable r1 = Mock()
    Runnable r2 = Mock()

    when:

    StrategyRegistry sr = StrategyRegistry.newInstance(Runnable, [
        (List): r1,
        (Map): r2
    ])

    then:

    sr.getByInstance([]).is(r1)
    sr.getByInstance([:]).is(r2)

    when:

    sr.clearCache()

    then:

    sr.getByInstance([]).is(r1)
  }

  def "null instances matches against void.class"() {

    Runnable r1 = Mock()
    Runnable r2 = Mock()

    when:

    def sr = StrategyRegistry.newInstance(Runnable, [
        (void): r1,
        (Map): r2])


    then:

    sr.getByInstance(null).is(r1)
  }
}
