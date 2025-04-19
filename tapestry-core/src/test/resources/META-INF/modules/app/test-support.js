// Provide test support functions that can be addressed via Selenium.

// TODO: Maybe move this to main, for external re-use?

define(["t5/core/dom"],
  function(dom) {

    const exports = {
      findCSSMatchCount(selector) { return dom.body.find(selector).length; },
      doesNotExist(elementId) { return (dom(elementId)) === null; }
    };

    window.testSupport = exports;

    return exports;
});