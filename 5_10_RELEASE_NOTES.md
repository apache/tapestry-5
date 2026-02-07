# 5.10 Realease Notes Scratchpad

## ES Module Support

### Added configuration symbols

*   `tapestry.require-js-enabled` (`SymbolConstants.REQUIRE_JS_ENABLED`) (default value: `code`false).

### Added methods

*   `JavaScriptSupport.importEsModule(String moduleName)`
*   `JavaScriptSupport.addEsModuleConfigurationCallback(EsModuleConfigurationCallback callback)`
*   `org.apache.tapestry5.annotations.Import.esModule()`

### Added types

*   `org.apache.tapestry5.services.javascript.EsModuleInitialization`
*   `org.apache.tapestry5.services.javascript.ImportPlacement`
*   `org.apache.tapestry5.services.javascript.EsModuleConfigurationCallback`
*   `org.apache.tapestry5.services.javascript.EsModuleManager`
*   `org.apache.tapestry5.services.javascript.ESWrapper`

### Non-backward-compatible changes (but that probably won't cause problems)

n/a

### Non-backward-compatible changes

When using Require.js and AMD modules, from Tapestry 5.10.0 on, the previously returned objects, functions or values are now the `default` property of the object received from `require()`.
This is a consequence we couldn't avoid from the CoffeeScript to JavaScript to TypeScript conversion.

### Notes about Require.js disabled mode

Underscore.js, jQuery and Require.js are not included in the default stack (i.e. the set of JavaScript files which are included in pages by default).
If you need to use Underscore.js or jQuery, they're automatically available for import as `underscore` and `jquery`, respectively.

---

## Gradle Overhaul

### New/Updated Features

*   Gradle 8.5 -> 8.14.2
*   Gradle conventions (`buildSrc`)
*   Versions catalog plus a few project-specific dependencies in their `build.gradle` files
*   Moving (slowly) away from TestNG towards Junit/Jupiter

### Non-backward-compatible changes

*   As all dependencies were updated to the latest available version compatible with the branch's Java version, there might be incompatibilities/dependency mismatch during resolution if a project doesn't match their dependencies.

---

## Selenium / tapestry-test

### Non-backward-compatible changes (but that probably won't cause problems)

*   The `link=` selector used in tests is now converted to XPath to circumvent legacy Selenium behavior.
    Before, Selenium used JS to find the link, which might no longer work in newer versions.
    This is a minimal fix before TAP5-2817 will revamp SeleniumTestCase.

*   Renaming constants to reflect reality.
    As the testing container version dependes on the version catalog / outside context, a version-independent name is more sensible.
    *   `SeleniumTestCase.JETTY_7` ("jetty7") -> `SeleniumTestCase.JETTY` ("jetty")
    *   `SeleniumTestCase.TOMCAT_6` ("tomcat6") -> `SeleniumTestCase.TOMCAT` ("tomcate")
