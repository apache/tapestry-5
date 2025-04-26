Scratch pad for changes destined for the 5.10.0 release notes page.

# Added configuration symbols

* `tapestry.es-module-path-prefix` (`SymbolConstants.ES_MODULE_PATH_PREFIX`)


# Added methods

* `JavaScriptSupport.importEsModule(String moduleName)`
* `JavaScriptSupport.addEsModuleConfigurationCallback(EsModuleConfigurationCallback callback)`
* `org.apache.tapestry5.annotations.Import.esModule()`

# Added types

* `org.apache.tapestry5.services.javascript.EsModuleInitialization`
* `org.apache.tapestry5.services.javascript.ImportPlacement`
* `org.apache.tapestry5.services.javascript.EsModuleConfigurationCallback`
* `org.apache.tapestry5.services.javascript.EsModuleManager`

# Non-backward-compatible changes (but that probably won't cause problems)

# Non-backward-compatible changes

Using Require.js and AMD modules, modules that used to return a function
now return an object with a `default` property with the function.
This is a consequence we couldn't avoid from the CoffeeScript
to JavaScript to TypeScript conversion.


# Overall notes
