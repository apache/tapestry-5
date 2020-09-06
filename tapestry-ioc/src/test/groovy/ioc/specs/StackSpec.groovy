package ioc.specs

import spock.lang.Specification
import org.apache.tapestry5.commons.util.Stack

class StackSpec extends Specification {

  def stack = new Stack()

  def "peek in empty stack is failure"() {

    when:

    stack.peek()

    then:

    IllegalStateException e = thrown()

    e.message == "Stack is empty."
  }

  def "pop in empty stack is failure"() {

    when:

    stack.pop()

    then:

    IllegalStateException e = thrown()

    e.message == "Stack is empty."
  }

  def "simple stack operations"() {

    def fred = "fred"
    def barney = "barney"

    expect:

    stack.empty

    when:

    stack.push fred

    then:

    stack.peek().is(fred)
    !stack.empty

    when:

    stack.push barney

    then:

    stack.peek().is(barney)

    stack.toString() == "Stack[barney, fred]"

    stack.depth == 2

    stack.snapshot.equals([fred, barney])

    when:

    def popped = stack.pop()

    then:

    popped.is barney
    stack.peek().is(fred)
    !stack.empty

    when:

    popped = stack.pop()

    then:

    popped.is fred
    stack.empty
  }

  def "force the expansion of the inner data"() {

    def limit = 1000

    when:

    limit.times { stack.push it }

    then:

    limit.downto(1) { stack.pop() == it - 1}
  }

  def "clear the stack"() {

    10.times { stack.push it }

    when:

    stack.clear()

    then:

    stack.empty
  }
}
