package ioc.specs

import org.apache.tapestry5.commons.util.TimeInterval

import spock.lang.Specification
import spock.lang.Unroll

class TimeIntervalSpec extends Specification {

  @Unroll
  def "constructor usage '#input' parses to #milliseconds ms and '#units'"() {
    when:

    TimeInterval ti = new TimeInterval(input)

    then:

    ti.milliseconds() == milliseconds
    ti.toString() == "TimeInterval[$units]"

    ti.toDescription() == description

    where:

    input    | milliseconds            | units          | description

    "30 s"   | 30000                   | "30000 ms"     | "30s"
    "1h 30m" | 90 * 60 * 1000          | "5400000 ms"   | "1h 30m"
    "2d"     | 2 * 24 * 60 * 60 * 1000 | "172800000 ms" | "2d"
    "23ms"   | 23                      | "23 ms"        | "23ms"
    "62s"    | 62 * 1000               | "62000 ms"     | "1m 2s"
  }

  def "invalid units"() {

    when:

    TimeInterval.parseMilliseconds "30s 500mz"

    then:

    RuntimeException e = thrown()

    e.message == "Unknown time interval unit 'mz' (in '30s 500mz').  Defined units: d, h, m, ms, s, y."
  }

  def "unrecognized input"() {

    when:

    TimeInterval.parseMilliseconds "30s z 500ms"

    then:

    RuntimeException e = thrown()

    e.message.contains "Unexpected string 'z'"
  }

  def "unrecognized input at end"() {

    when:

    TimeInterval.parseMilliseconds "30s  500ms xyz"

    then:

    RuntimeException e = thrown()

    e.message.contains "Unexpected string 'xyz'"
  }

}
