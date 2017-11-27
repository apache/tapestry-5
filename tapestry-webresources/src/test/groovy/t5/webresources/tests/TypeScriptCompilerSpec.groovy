package t5.webresources.tests;

import org.apache.tapestry5.SymbolConstants
import org.apache.tapestry5.internal.InternalSymbols
import org.apache.tapestry5.internal.test.PageTesterContext
import org.apache.tapestry5.internal.webresources.TypeScriptCompiler
import org.apache.tapestry5.ioc.MappedConfiguration
import org.apache.tapestry5.ioc.annotations.Autobuild
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.ioc.annotations.SubModule
import org.apache.tapestry5.ioc.internal.util.ClasspathResource
import org.apache.tapestry5.modules.AssetsModule
import org.apache.tapestry5.modules.TapestryModule
import org.apache.tapestry5.services.ApplicationGlobals
import org.apache.tapestry5.webresources.modules.WebResourcesModule

import spock.lang.Shared
import spock.lang.Specification

@SubModule([TapestryModule, WebResourcesModule, TypeScriptCompilerSpec.TestModule, AssetsModule])
class TypeScriptCompilerSpec extends Specification {

  @Autobuild
  private TypeScriptCompiler typeScriptCompiler;

  @Inject
  @Shared
  private ApplicationGlobals applicationGlobals

  def setupSpec(){
    applicationGlobals.storeContext(new PageTesterContext("/test"));
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
  
  public static class TestModule {

    def contributeApplicationDefaults(MappedConfiguration configuration){
      configuration.add(InternalSymbols.APP_NAME, "test")
      configuration.add("tapestry.app-package", "typescript")
      configuration.add(SymbolConstants.MINIFICATION_ENABLED, false)
    }
  }
}
