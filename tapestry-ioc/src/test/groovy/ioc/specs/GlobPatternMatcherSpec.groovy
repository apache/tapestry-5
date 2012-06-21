package ioc.specs

import org.apache.tapestry5.ioc.internal.GlobPatternMatcher
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class GlobPatternMatcherSpec extends Specification {

  def "input '#input' matches pattern '#pattern'"() {

    def matcher = new GlobPatternMatcher(pattern)

    expect:

    matcher.matches(input)

    where:

    input         | pattern
    "fred"        | "fred"
    "fred"        | "FRED"
    "fred"        | "*"
    ""            | "*"
    "fred.Barney" | "*Barney"
    "fred.Barney" | "*BARNEY"
    "fred.Barney" | "fred*"
    "fred.Barney" | "FRED*"
    "fredBarney"  | "*dB*"
    "fredBarney"  | "*DB*"
    "fred.Barney" | "*Barney*"
    "fred.Barney" | "*fred*"
    "fred.Barney" | "*FRED*"
    "MyEntityDAO" | ".*dao"
    "FredDAO"     | "(fred|barney)dao"
  }

  def "input '#input' does not match pattern '#pattern'"() {

    def matcher = new GlobPatternMatcher(pattern)

    expect:

    !matcher.matches(input)

    where:

    input          | pattern
    "xfred"        | "fred"
    "fredx"        | "fred"
    "fred"         | "xfred"
    "fred"         | "fredx"
    "fred.Barneyx" | "*Barney"
    "fred.Barney"  | "*Barneyx"
    "fred.Barney"  | "*xBarney"
    "xfred.Barney" | "fred*"
    "fred.Barney"  | "fredx*"
    "fred.Barney"  | "xfred*"
    "fred.Barney"  | "*flint*"
    "MyEntityDAL"  | ".*dao"
    "WilmaDAO"     | "(fred|barney)dao"
  }
}
