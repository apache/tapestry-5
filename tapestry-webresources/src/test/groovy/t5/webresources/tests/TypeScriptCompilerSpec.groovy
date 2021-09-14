package t5.webresources.tests;

import org.apache.tapestry5.internal.webresources.TypeScriptCompiler
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import org.apache.tapestry5.ioc.internal.util.ClasspathResource

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class TypeScriptCompilerSpec extends Specification {

  @Shared
  private TypeScriptCompiler typeScriptCompiler;


  def setupSpec(){
    def resource = new ClasspathResource("org/apache/tapestry5/webresources/internal/typescript.js")
    def op = new QuietOperationTracker()
    typeScriptCompiler = new TypeScriptCompiler(op, resource)
  }

  def "Compile Hello World example"(){
    when:
    def resource = new ClasspathResource("t5/webresources/greeter.ts")

    def compiled = typeScriptCompiler.transform(resource, null)
    then:
    compiled.text == TypeScriptCompilerSpec.class.getResourceAsStream('/t5/webresources/greeter-compiled.js').text
  }

  def "Type information is preserved"(){
    when:
    def resource = new ClasspathResource("t5/webresources/park-example.ts")
    def compiled = typeScriptCompiler.transform(resource, null)
    then:
    compiled.text == TypeScriptCompilerSpec.class.getResourceAsStream('/t5/webresources/park-example-compiled.js').text
  }

  // TAP5-2691: Rhino has (sometimes) problems with ES6 code
  // Ignored for now, because this works on my machine, but can be problematic
  // on others
  @Ignore
  def "reserved keynamed as properties"(){
    when:
    def resource = new ClasspathResource("t5/webresources/keywords.ts")
    def compiled = typeScriptCompiler.transform(resource, null)
    then:
    compiled.text == TypeScriptCompilerSpec.class.getResourceAsStream('/t5/webresources/keywords-compiled.js').text
  }
}
