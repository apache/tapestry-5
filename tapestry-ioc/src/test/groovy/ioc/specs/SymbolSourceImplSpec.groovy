package ioc.specs

import org.apache.tapestry5.ioc.internal.services.SymbolSourceImpl
import org.apache.tapestry5.ioc.services.SymbolProvider
import org.apache.tapestry5.ioc.services.SymbolSource
import spock.lang.Specification

class SymbolSourceImplSpec extends Specification {

  def "expand symbols when no symbols are present in the input"() {
    SymbolSource source = new SymbolSourceImpl([])
    def input = "A jolly good time"

    expect:

    source.expandSymbols(input).is(input)
  }

  def "process simple expansions"() {
    SymbolProvider provider = Mock()

    SymbolSource source = new SymbolSourceImpl([provider])

    when:

    def expanded = source.expandSymbols('${barney} and ${dino}')

    then:

    expanded == "Barney and Dino"

    1 * provider.valueForSymbol("barney") >> "Barney"
    1 * provider.valueForSymbol("dino") >> "Dino"
  }

  def "providers are consulted in order"() {
    SymbolProvider provider1 = Mock()
    SymbolProvider provider2 = Mock()

    SymbolSource source = new SymbolSourceImpl([provider1, provider2])

    when:

    def expanded = source.expandSymbols('${barney} and ${dino}')

    then:

    1 * provider1.valueForSymbol("barney") >> "Barney"

    then:

    1 * provider1.valueForSymbol("dino") >> null

    then:

    1 * provider2.valueForSymbol("dino") >> "Dino"

    expanded == "Barney and Dino"
  }

  def "exception when no provider can expand a symbol"() {
    SymbolProvider provider = Mock()

    SymbolSource source = new SymbolSourceImpl([provider])

    when:

    source.expandSymbols('${barney} and ${dino}')

    then:

    RuntimeException e = thrown()

    e.message == "Symbol 'barney' is not defined."
  }

  def "missing closing brace on symbol expansion"() {

    SymbolSource source = new SymbolSourceImpl([]);

    when:

    source.expandSymbols('Unmatched ${this')

    then:

    RuntimeException e = thrown()

    e.message == '''Input string 'Unmatched ${this' is missing a symbol closing brace.'''
  }

  def "missing closing brace from expanded value of symbol"() {
    SymbolProvider provider = Mock()
    SymbolSource source = new SymbolSourceImpl([provider]);

    when:

    source.valueForSymbol("barney")

    then:

    1 * provider.valueForSymbol("barney") >> '''Barney (whose friends are ${barney.friends})'''
    1 * provider.valueForSymbol("barney.friends") >> '''${fred} and ${betty'''
    1 * provider.valueForSymbol("fred") >> "Fred"

    RuntimeException e = thrown()

    e.message == '''Input string '${fred} and ${betty' is missing a symbol closing brace (in barney --> barney.friends).'''

  }

  def "expansion values may themselves contain further expansions"() {
    SymbolProvider provider = Mock()

    SymbolSource source = new SymbolSourceImpl([provider])

    when:

    def result = source.expandSymbols('''Fred's friends are ${fred.friends}.''')

    then:

    1 * provider.valueForSymbol("fred.friends") >> '''${barney} and ${dino}'''
    1 * provider.valueForSymbol("barney") >> "Barney"
    1 * provider.valueForSymbol("dino") >> "Dino"

    result == '''Fred's friends are Barney and Dino.'''
  }

  def "unknown symbol when expanding a symbol value is properly tracked and reported"() {
    SymbolProvider provider = Mock()

    SymbolSource source = new SymbolSourceImpl([provider])

    when:

    def result = source.valueForSymbol("fred.message")

    then:

    1 * provider.valueForSymbol("fred.message") >> '''Fred's friends are ${fred.friends}.'''
    1 * provider.valueForSymbol("fred.friends") >> '''${barney} and ${dino}'''
    1 * provider.valueForSymbol("barney") >> "Barney"

    RuntimeException e = thrown()

    e.message == "Symbol 'dino' is not defined (in fred.message --> fred.friends --> dino)."
  }

  def "expanded values for symbols are cached"() {

    SymbolProvider provider = Mock()

    SymbolSource source = new SymbolSourceImpl([provider])

    when:

    def first = source.valueForSymbol("fred")

    then:

    1 * provider.valueForSymbol("fred") >> '''Fred's friends are ${barney} and ${dino}.'''
    1 * provider.valueForSymbol("barney") >> "Barney"
    1 * provider.valueForSymbol("dino") >> "Dino"

    first == "Fred's friends are Barney and Dino."

    when:

    def second = source.valueForSymbol("fred")

    then:

    0 * _

    second.is(first)
  }

  def "recursive expansions are identified and reported as an exception"() {

    SymbolProvider provider = Mock()

    SymbolSource source = new SymbolSourceImpl([provider])

    when:

    source.valueForSymbol("fred")

    then:

    1 * provider.valueForSymbol("fred") >> '''Fred's friends are ${fred.friends}.'''
    1 * provider.valueForSymbol("fred.friends") >> '''${barney} and ${dino}'''
    1 * provider.valueForSymbol("barney") >> '''Barney (whose friends are ${barney.friends})'''
    1 * provider.valueForSymbol("barney.friends") >> '''${fred} and ${betty}'''

    RuntimeException e = thrown()

    e.message == "Symbol 'fred' is defined in terms of itself (fred --> fred.friends --> barney --> barney.friends --> fred)."

  }
}
