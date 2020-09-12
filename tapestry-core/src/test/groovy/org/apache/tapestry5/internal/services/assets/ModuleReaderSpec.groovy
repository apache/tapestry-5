package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.internal.services.assets.JavaScriptStackAssemblerImpl.ModuleReader;
import org.apache.tapestry5.test.ioc.TestBase
import org.testng.annotations.Test

class ModuleReaderTest extends TestBase {


    @Test
    void "convert anonymous define into named define"() {
      def moduleContent = '''define([], function(){
  return "constant";
});'''
      ModuleReader moduleReader = new ModuleReader("constants-module")
      def transformed = moduleReader.transform(moduleContent)
      assertEquals(transformed, '''define("constants-module",[], function(){
  return "constant";
});''')

    }

    @Test
    // TAP5-2511
    void "convert anonymous define without dependencies into named define"() {
      def moduleContent = '''define(function(){
  return "constant";
});'''
      ModuleReader moduleReader = new ModuleReader("constants-module")
      def transformed = moduleReader.transform(moduleContent)
      assertEquals(transformed, '''define("constants-module",function(){
  return "constant";
});''')

    }

    @Test
    // TAP5-2511
    void "named define calls are not modified"() {
      def moduleContent = '''define("foobar", [], function(){
  return "constant";
});'''
      ModuleReader moduleReader = new ModuleReader("constants-module")
      def transformed = moduleReader.transform(moduleContent)
      assertEquals(transformed, '''define("foobar", [], function(){
  return "constant";
});''')

    }

}
