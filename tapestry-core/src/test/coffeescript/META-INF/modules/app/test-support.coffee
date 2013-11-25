# Provide test support functions that can be addressed via Selenium.

# TODO: Maybe move this to main, for external re-use?

define ["t5/core/dom"],
  (dom) ->

    exports =
      findCSSMatchCount: (selector) -> dom.body.find(selector).length
      doesNotExist: (elementId) -> (dom elementId) is null

    window.testSupport = exports

    return exports