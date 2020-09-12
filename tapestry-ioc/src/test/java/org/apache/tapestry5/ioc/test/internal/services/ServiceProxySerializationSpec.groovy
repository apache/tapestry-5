package org.apache.tapestry5.ioc.test.internal.services

import org.apache.tapestry5.commons.services.TypeCoercer

import ioc.specs.AbstractRegistrySpecification


class ServiceProxySerializationSpec extends AbstractRegistrySpecification {

  def "test serialization and deserialization of a service"() {
    buildRegistry()

    TypeCoercer proxy1 = getService TypeCoercer

    def serialized = serialize proxy1

    when:

    TypeCoercer proxy2 = deserialize serialized

    then:

    proxy1.is(proxy2)

    when:

    shutdown()
    buildRegistry()

    TypeCoercer proxy3 = deserialize serialized

    then:

    !proxy1.is(proxy3)
  }

  def "deserialize with no registry identifies the service which can not be de-serialized"() {
    buildRegistry()

    TypeCoercer proxy1 = getService TypeCoercer

    def serialized = serialize proxy1


    when:

    shutdown()

    registry = null

    deserialize(serialized)

    then:

    Exception e = thrown()

    e.message.contains "Service token for service 'TypeCoercer' can not be converted back into a proxy because no proxy provider has been registered"
  }

  def serialize(object) {
    def baos = new ByteArrayOutputStream()

    baos.withObjectOutputStream {
      it << object
      it.close()
    }

    return baos.toByteArray()
  }

  def deserialize(bytes) {
    return new ByteArrayInputStream(bytes).newObjectInputStream().readObject()
  }
}
