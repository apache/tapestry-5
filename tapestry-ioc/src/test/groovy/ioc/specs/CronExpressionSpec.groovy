package ioc.specs

import org.apache.tapestry5.ioc.internal.services.cron.CronExpression
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.text.ParseException

@Unroll
class CronExpressionSpec extends Specification {

  // This allows the any of the constants defined on Calendar to be used
  // without qualification.
  def propertyMissing(String name) { Calendar[name] }

  def "isSatisfiedBy(#year, #month, #day, #hour, #minute, #second ) should be #satisfied for expression '#expr'"() {

    def cal = Calendar.getInstance();

    def exp = new CronExpression(expr)

    cal.set year, month, day, hour, minute, second

    expect:

    exp.isSatisfiedBy(cal.time) == satisfied

    where:
    expr                    | year | month   | day | hour | minute | second | satisfied
    "0 15 10 * * ? 2005"    | 2005 | JUNE    | 1   | 10   | 15     | 0      | true
    "0 15 10 * * ? 2005"    | 2006 | JUNE    | 1   | 10   | 15     | 0      | false
    "0 15 10 * * ? 2005"    | 2005 | JUNE    | 1   | 10   | 16     | 0      | false
    "0 15 10 * * ? 2005"    | 2005 | JUNE    | 1   | 10   | 14     | 0      | false
    "0 15 10 L-2 * ? 2010"  | 2010 | OCTOBER | 29  | 10   | 15     | 0      | true
    "0 15 10 L-2 * ? 2010"  | 2010 | OCTOBER | 28  | 10   | 15     | 0      | false
    "0 15 10 L-5W * ? 2010" | 2010 | OCTOBER | 26  | 10   | 15     | 0      | true
    "0 15 10 L-1 * ? 2010"  | 2010 | OCTOBER | 30  | 10   | 15     | 0      | true
    "0 15 10 L-1W * ? 2010" | 2010 | OCTOBER | 29  | 10   | 15     | 0      | true
  }

  def cloneViaSerialize(obj) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream()

    baos.withObjectOutputStream { it.writeObject(obj) }

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())
    ObjectInputStream ois = new ObjectInputStream(bais)

    ois.readObject()
  }

  // This test is in the original TestNG test but failed there (making me think that the test case was probably
  // not being run). It's clear that CronExpressions do not deserialize correctly by looking at the source!
  @Ignore
  def "check that CronExpressions serialize and deserialize"() {

    CronExpression original = new CronExpression("19 15 10 4 Apr ? ")

    when:

    CronExpression cloned = cloneViaSerialize original

    then:

    cloned.cronExpression == original.cronExpression
    cloned.getNextValidTimeAfter(new Date()) != null
  }

  def "Parse failure: parse of '#expr' should fail with '#err'"() {

    when:
    new CronExpression(expr)

    then:
    def e = thrown(ParseException)

    assert e.message.startsWith(err)

    where:
    expr                   | err
    "* * * * Foo ? "       | "Invalid Month value:"
    "* * * * Jan-Foo ? "   | "Invalid Month value:"
    "0 0 * * * *"          | "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."
    "0 0 * 4 * *"          | "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."
    "0 0 * * * 4"          | "Support for specifying both a day-of-week AND a day-of-month parameter is not implemented."
    "0 43 9 1,5,29,L * ?"  | "Support for specifying 'L' and 'LW' with other days of the month is not implemented"
    "0 43 9 ? * SAT,SUN,L" | "Support for specifying 'L' with other days of the week is not implemented"
    "0 43 9 ? * 6,7,L"     | "Support for specifying 'L' with other days of the week is not implemented"
    "0/5 * * 32W 1 ?"      | "The 'W' option does not make sense with values larger than"
  }

  def "Expression '#expr' is valid"() {
    when:
    new CronExpression(expr)

    then:
    noExceptionThrown()

    where:
    expr << ["0 43 9 ? * 5L"]
  }

}