package ioc.specs


import org.apache.tapestry5.commons.util.CaseInsensitiveMap

import spock.lang.Issue;
import spock.lang.Specification

class CaseInsensitiveMapSpec extends Specification {

  CaseInsensitiveMap map = new CaseInsensitiveMap([fred: "flintstone", barney: "rubble", wilma: "flinstone", betty: "rubble"])

  def "get() is case insensitive"() {
    def map = new CaseInsensitiveMap()

    def value = "flintstone"

    when:

    map.put("fred", value)

    then:

    map.get("fred").is(value)
    map.get("Fred").is(value)
  }

  def "containsKey() is case insensitive"() {

    expect:

    map.containsKey("fred")
    map.containsKey("Fred")
    map.containsKey("barney")
    map.containsKey("wilma")
    !map.containsKey("dino")
  }

  def "remove() is case insensitive"() {
    expect:

    map.containsKey("fred")
    !map.isEmpty()

    when:

    map.remove("FrED")

    then:

    map.keySet() == ["barney", "wilma", "betty"] as Set
  }

  def "copying Map constructor"() {
    def standard = [fred: "flintstone", barney: "rubble", wilma: "flintstone"]

    when:

    def original = new CaseInsensitiveMap(standard)

    then:

    original == standard

    when:

    def copy = new CaseInsensitiveMap(original)

    then:

    copy == original
  }

  def "comparison of two CaseInsensitiveMaps ignores case"() {
    def lower = new CaseInsensitiveMap([fred: "flintstone", barney: "rubble"])
    def upper = new CaseInsensitiveMap([Fred: "flintstone", Barney: "rubble"])

    expect:

    upper == lower
  }

  def "put with different case replaces the old key"() {

    expect:

    map.keySet() == ["fred", "barney", "betty", "wilma"] as Set


    when:

    map.put("FRED", "flintstone")

    then:

    map.keySet() == ["FRED", "barney", "betty", "wilma"] as Set
  }

  def "get with missing key is null"() {
    expect:

    map.notFound == null
  }

  def "get with non-string key is null"() {
    expect:

    map.get(this) == null
  }

  def "expansion of the internal entry array"() {

    def count = 2000

    def map = new CaseInsensitiveMap()

    count.times { it ->
      assert map.put("key_$it" as String, it) == null
    }

    when:

    count.times { it ->
      assert map.get("key_$it" as String) == it
    }

    then:

    map.size() == count
    map.entrySet().size() == count

    when:

    map.clear()

    then:

    map.size() == 0

  }

  def "change value via entrySet()"() {
    def map = new CaseInsensitiveMap()

    map.put("fred", "flintstone")

    when:

    map.entrySet().each { entry -> entry.value = "murray" }

    then:

    map.get("fred") == "murray"
  }

  def "entrySet iterator fails fast after remove"() {

    def i = map.entrySet().iterator()

    i.next()
    map.remove("betty")

    when:

    i.next()

    then:

    thrown(ConcurrentModificationException)
  }

  def "entrySet iterator fails fast after put"() {

    def i = map.entrySet().iterator()

    i.next()
    map.put("zaphod", "breeblebrox")

    when:

    i.next()

    then:

    thrown(ConcurrentModificationException)
  }

  def "iterator may remove without concurrent exception"() {

    def i = map.entrySet().iterator()

    while (i.hasNext()) {
      if (i.next().key == "wilma") { i.remove() }
    }

    expect:

    map.keySet() == ["barney", "betty", "fred"] as Set
  }

  def "contains via entrySet"() {

    def set = map.entrySet()

    expect:

    set.contains(newMapEntry("fred", "flintstone"))
    set.contains(newMapEntry("Fred", "flintstone"))

    !set.contains(newMapEntry("Zaphod", "Breeblebox"))
    !set.contains(newMapEntry("fred", "murray"))
  }

  def "remove via entrySet"() {

    def set = map.entrySet()

    when:

    assert set.remove(newMapEntry("Zaphod", "Breeblrox")) == false
    assert set.remove(newMapEntry("fred", "murray")) == false

    assert set.remove(newMapEntry("fred", "flintstone")) == true

    then:

    map.keySet() == ["barney", "wilma", "betty"] as Set
  }

  def newMapEntry(key, value) {
    return new Map.Entry() {

      @Override
      Object getKey() {
        return key
      }

      @Override
      Object getValue() {
        return value;
      }

      @Override
      Object setValue(Object newValue) {
        value = newValue
      }
    }
  }

  def "null is a valid key"() {
    when:

    map.put(null, "NULL")

    then:

    map.get(null) == "NULL"
  }

  def "clearing the entrySet clears the map"() {
    expect:

    !map.isEmpty()
    !map.entrySet().isEmpty()

    when:

    map.entrySet().clear()

    then:

    map.isEmpty()
  }

  def "next() after last entry in entrySet is a failure"() {
    Iterator i = map.entrySet().iterator()

    while (i.hasNext()) { i.next() }

    when:

    i.next()

    then:

    thrown(NoSuchElementException)
  }

  def "serialize/deserialize copies all data"() {

    def baos = new ByteArrayOutputStream()
    def oos = new ObjectOutputStream(baos)

    oos.writeObject(map)
    oos.close()

    def bais = new ByteArrayInputStream(baos.toByteArray())
    ObjectInputStream ois = new ObjectInputStream(bais)

    def copy = ois.readObject()

    expect:

    copy == map
  }
  
  @Issue('https://issues.apache.org/jira/browse/TAP5-2452')
  def "Modifications to key set are not allowed"(){
    setup:
    def map = new CaseInsensitiveMap<String>()
    map.put('1', '1')
    map.put('2', '2')
    map.put('3', '3')
    def keysToRetain = ['3', '4', '5']
    expect:
    map.keySet().size() == 3
    map.keySet() == ['1', '2', '3'] as Set
    when:
    map.keySet().retainAll(keysToRetain)
    then:
    thrown(UnsupportedOperationException)
    when:
    map.keySet().remove("Zaphod")
    then:
    thrown(UnsupportedOperationException)
  }
}
