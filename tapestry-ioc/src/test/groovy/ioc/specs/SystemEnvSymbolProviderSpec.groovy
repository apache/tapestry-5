package ioc.specs

import org.apache.tapestry5.ioc.internal.services.SystemEnvSymbolProvider
import org.apache.tapestry5.ioc.services.SymbolProvider
import spock.lang.Specification

class SystemEnvSymbolProviderSpec extends Specification {

  SymbolProvider provider = new SystemEnvSymbolProvider()

  def "key exists"() {
    expect:
    provider.valueForSymbol("env.home") == System.getenv("HOME")
  }

  def "key missing"() {
    expect: provider.valueForSymbol("env.does-not-exist") == null

  }

}
