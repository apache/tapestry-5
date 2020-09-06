package ioc.specs

import org.apache.tapestry5.ioc.services.PipelineBuilder
import org.apache.tapestry5.ioc.test.internal.services.StandardFilter
import org.apache.tapestry5.ioc.test.internal.services.StandardService
import org.slf4j.Logger

import spock.lang.Shared

class PipelineBuilderImplSpec extends AbstractSharedRegistrySpecification {

  @Shared
  PipelineBuilder builder

  def setupSpec() { builder = getService PipelineBuilder }

  Logger logger = Mock()

  def "standard pipeline with filters"() {

    // For some reason, this didn't work with closures, just with actual inner classes

    StandardFilter subtracter = new StandardFilter() {

      @Override
      int run(int i, StandardService service) {
        service.run(i) - 2
      }
    }

    StandardFilter multiplier = new StandardFilter() {

      @Override
      int run(int i, StandardService service) {
        2 * service.run(i)
      }
    }

    StandardFilter adder = new StandardFilter() {

      @Override
      int run(int i, StandardService service) {
        service.run(i + 3)
      }
    }

    StandardService terminator = new StandardService() {

      @Override
      int run(int i) {
        i
      }
    }

    when:

    StandardService pipeline = builder.build logger, StandardService, StandardFilter, [subtracter, multiplier, adder], terminator

    then:

    pipeline.run(5) == 14
    pipeline.run(10) == 24
  }

  def "a pipeline without filters is simply the temrinator"() {

    StandardService terminator = Mock()

    when:

    StandardService pipeline = builder.build logger, StandardService, StandardFilter, [], terminator

    then:

    pipeline.is terminator
  }

  def "a pipeline with no filters and no terminator does nothing"() {
    when:

    StandardService pipeline = builder.build logger, StandardService, StandardFilter, []

    then:

    pipeline.run(99) == 0

  }
}
