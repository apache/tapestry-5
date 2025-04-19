/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
require(["t5/core/dom"], function(dom) {
  module("t5/core/dom");

  test("get wrapped element by id", function() {
    const e = dom("dom-eventelement-native");

    return ok(e !== null, "element found and wrapped");
  });

  test("get wrapped element by unknown id is null", function() {
    const e = dom("dom-does-not-exist-element");

    return ok(e === null, "element not found and null");
  });

  test("trigger native events", function() {

    let clicks = 0;
    const container = dom("dom-eventelement-native");
    const button = container.findFirst("a");

    container.on("click", "a", function() {
      clicks++;
      return false;
    });

    button.trigger("click");

    return equal(clicks, 1, "native event was triggered");
  });

  test("selector used with events filters", function() {

    let clicks = 0;
    const container = dom("dom-eventelement-selector");
    const primary = container.findFirst("a.btn-primary");
    const secondary = container.findFirst("a[data-use=secondary]");

    container.on("x:click", "a.btn-primary", function() {
      clicks++;
      return false;
    });

    primary.trigger("x:click");

    equal(clicks, 1, "click on selected element invokes handler");

    secondary.trigger("x:click");

    return equal(clicks, 1, "click on non-selected element does not invoke handler");
  });

  test("this is matched element in handler", function() {

    const container = dom("dom-eventelement-matched");
    const primary = container.findFirst("a.btn-primary");

    container.on("x:click", "a.btn-primary", function() {

      strictEqual(this.element, primary.element, "this should be the wrapper for element that was matched");

      return false;
    });

    return primary.trigger("x:click");
  });

  return test("visibility, hide(), and show()", function() {

    const e = (dom("dom-visibility")).findFirst("span");

    equal(e.visible(), true, "element is initially visible");

    e.hide();

    equal(e.visible(), false, "element is not visible once hidden");

    e.show();

    return equal(e.visible(), true, "element is visible against once shown");
  });
});
