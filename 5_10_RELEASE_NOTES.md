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

# Non-backward-compatible changes (but that probably won't cause problems)

# Non-backward-compatible changes

* When using Require.js and AMD modules, from Tapestry 5.10.0 on,
  the previously returned objects, functions or values are now
  the `default` property of the object received from `require()`.
  This is a consequence we couldn't avoid from the CoffeeScript
  to JavaScript to TypeScript conversion.
  

# Notes about Require.js disabled mode
* When using Bootstrap 3, the `t5/bootstrap/*` modules had automatic dependency
  management (for example, if you `bootstrap/tooltip`, `bootstrap/transition`
  would automatically be included too through Require.js). This doesn't happen
  when Require.js is disabled. So, for example, when importing `bootstrap/tooltip`,
  you should import `bootstrap/trasition` first. Notice Bootstrap 3 JavaScript 
  files don't have any module management code on it (Require.js nor ES modules)
  


# Overall notes
