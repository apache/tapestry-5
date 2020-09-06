package ioc.specs

import org.apache.tapestry5.commons.Messages
import org.apache.tapestry5.commons.internal.util.MessagesImpl
import org.apache.tapestry5.ioc.test.internal.util.TargetMessages

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class MessagesImplSpec extends Specification {

  @Shared
  Messages messages = MessagesImpl.forClass(TargetMessages)

  @Unroll
  def "contains key: #desc"() {

    expect:

    messages.contains(key) == expectation

    where:

    key       | expectation | desc
    "no-args" | true        | "base case"
    "xyzzyz"  | false       | "key not present"
    "No-Args" | true        | "case insensitive"
  }

  @Unroll
  def "get message from catalog: #desc"() {
    expect:

    messages.get(key) == expectation

    where:

    key                | expectation                       | desc

    "no-args"          | "No arguments."                   | "base case"
    "something-failed" | "Something failed: %s"            | "does not attempt to expand conversions"
    "No-Args"          | "No arguments."                   | "access is case insensitive"
    "does-not-exist"   | "[[missing key: does-not-exist]]" | "fake value supplied for missing key"
  }

  @Unroll
  def "format message:#desc"() {
    expect:

    messages.format(key, value) == expectation

    where:

    key              | value    | expectation                       | desc
    "result"         | "good"   | "The result is 'good'."           | "standard"
    "Result"         | "best"   | "The result is 'best'."           | "lookup is case insensitive"
    "does-not-exist" | "xyzzyz" | "[[missing key: does-not-exist]]" | "fake value supplied for missing key"
  }

  def "access a MesageFormatter to format content"() {
    def mf = messages.getFormatter("result")

    expect:

    mf.format("cool") == "The result is 'cool'."
  }

  def "MessageFormatters are cached"() {
    def mf1 = messages.getFormatter("result")
    def mf2 = messages.getFormatter("result")

    expect:

    mf1.is(mf2)
  }
}
