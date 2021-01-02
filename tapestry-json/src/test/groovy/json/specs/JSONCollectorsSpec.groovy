package json.specs

import org.apache.tapestry5.json.JSONCollectors
import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException

import java.util.stream.Stream

import spock.lang.Specification

class JSONCollectorsSpec extends Specification {

  def "collect stream to array"() {
    
    given:

    def stringValue = "a string value"
    def longValue = 3L
    
    def stream = Stream.of(stringValue, longValue)
    
    when:

    def collected = stream.collect(JSONCollectors.toArray());

    then:

    collected.size() == 2
    collected.get(0) == stringValue
    collected.get(1) == longValue
  }

  def "collect stream to array invalid type"() {
    
    given:

    def stringValue = "a string value"
    def longValue = 3L
    def invalidValue = new java.util.Date()
    
    def stream = Stream.of(stringValue, longValue, invalidValue)
    
    when:

    def collected = stream.collect(JSONCollectors.toArray());

    then:

    JSONInvalidTypeException e = thrown()
  }

  def "collect stream to map"() {

    given:

    def first = new Tuple("first key", "a string value")
    def second = new Tuple("second key", 3L)

    def stream = Stream.of(first, second)

    when:

    def collected = stream.collect(JSONCollectors.toMap({ t -> t.get(0) }, { t -> t.get(1) }));

    then:

    collected.size() == 2
    collected.get(first.get(0)) == first.get(1)
    collected.get(second.get(0)) == second.get(1)
  }

  def "collect stream to map invalid type"() {
      
    given:
  
    def first = new Tuple("first key", "a string value")
    def second = new Tuple("second key", 3L)
    def third = new Tuple("invalid type", new java.util.Date())

    def stream = Stream.of(first, second, third)

    when:

    def collected = stream.collect(JSONCollectors.toMap({ t -> t.get(0) }, { t -> t.get(1) }));
  
    then:
  
    JSONInvalidTypeException e = thrown()
  }

  def "collect stream to map duplicate key"() {

    when:

    def collected = Stream.of("first", "second", "first").collect(JSONCollectors.toMap({ v -> v }, { v -> v }))

    then:

    IllegalStateException e = thrown()
  }

}
