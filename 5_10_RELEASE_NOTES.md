Scratch pad for changes destined for the 5.10.0 release notes page.

# Added configuration symbols

* `tapestry.require-js-enabled`(`SymbolConstants.REQUIRE_JS_ENABLED`) (default value: `code`false).


# Added methods

* `JavaScriptSupport.importEsModule(String moduleName)`
* `JavaScriptSupport.addEsModuleConfigurationCallback(EsModuleConfigurationCallback callback)`
* `org.apache.tapestry5.annotations.Import.esModule()`

# Added types

* `org.apache.tapestry5.services.javascript.EsModuleInitialization`
* `org.apache.tapestry5.services.javascript.ImportPlacement`
* `org.apache.tapestry5.services.javascript.EsModuleConfigurationCallback`
* `org.apache.tapestry5.services.javascript.EsModuleManager`
* `org.apache.tapestry5.services.javascript.ESWrapper`

# Non-backward-compatible changes (but that probably won't cause problems)

# Non-backward-compatible changes

* When using Require.js and AMD modules, from Tapestry 5.10.0 on,
  the previously returned objects, functions or values are now
  the `default` property of the object received from `require()`.
  This is a consequence we couldn't avoid from the CoffeeScript
  to JavaScript to TypeScript conversion.
  

# Notes about Require.js disabled mode
* Underscore.js, jQuery and Require.js are not included in the default stack 
  (i.e. the set of JavaScript files which are included in pages by default).
  If you need to use Underscore.js or jQuery, they're automatically available for   
  import as `underscore` and `jquery`, respectively.


# Overall notes
