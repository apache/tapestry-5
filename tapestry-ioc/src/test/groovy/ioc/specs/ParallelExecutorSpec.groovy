package ioc.specs

import org.apache.tapestry5.ioc.Invokable
import org.apache.tapestry5.ioc.services.ParallelExecutor
import org.apache.tapestry5.ioc.test.StringHolder
import org.apache.tapestry5.ioc.test.StringHolderImpl

import spock.lang.Shared

class ParallelExecutorSpec extends AbstractSharedRegistrySpecification {

  @Shared ParallelExecutor executor

  def setupSpec() {
    executor = getService ParallelExecutor
  }

  def "thunks execute in parallel and results are cached"() {

    def thunks = []

    when:

    100.times { i ->

      def value = "Value[$i]"

      def first = true

      def inv = new Invokable() {

        Object invoke() {

          if (!first) { throw new IllegalStateException("Result of Invokable should be cached.") }

          def holder = new StringHolderImpl()

          holder.value = value

          Thread.sleep 10

          first = false

          return holder
        }
      }

      thunks.add executor.invoke(StringHolder, inv)
    }

    then:

    // Not sure how to truly prove that the results are happening in parallel.
    // I think it's basically that by the time we work our way though the list, some values
    // will have been computed ahead.

    thunks.size().times { i ->

      assert thunks[i].value == "Value[$i]"
    }

    then: "a second pass to proove that the thunk caches the result"

    thunks.size().times { i ->

      assert thunks[i].value == "Value[$i]"
    }
  }

  def "toString() of a thunk indicates the interface type"() {

    Invokable inv = Mock()

    when:

    StringHolder thunk = executor.invoke StringHolder, inv

    then:

    thunk.toString() == "FutureThunk[org.apache.tapestry5.ioc.test.StringHolder]"
  }

  def "exception inside the Invokable is rethrown by the thunk"() {

    def inv = new Invokable() {

      Object invoke() { throw new RuntimeException("Future failure!")}
    }

    StringHolder thunk = executor.invoke StringHolder, inv


    when:

    thunk.getValue()

    then:

    RuntimeException e = thrown()

    e.message.contains "Future failure!"
  }


}
