// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http:#www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * ## t5/core/console
 * 
 * A wrapper around the native console, when it exists.
 * @packageDocumentation
 */ 
import dom from "t5/core/dom";
import _ from "underscore";
import bootstrap from "t5/core/bootstrap";
import { ElementWrapper } from "t5/core/types";

let e;
let nativeConsole = null;

try {
  // FireFox will throw an exception if you even access the console object and it does
  // not exist. Wow!
  nativeConsole = window.console || {};
} catch (error) { e = error; }

let floatingConsole: ElementWrapper | null = null;
let messages: ElementWrapper | null = null;

const noFilter = (e: ElementWrapper) => true;

let filter = noFilter;

const updateFilter = function(text: string = "") {
  if (text === "") {
    filter = noFilter;
    return;
  }

  const words = text.toLowerCase().split(/\s+/);

  filter = function(e: ElementWrapper): boolean {
    const content = e.text().toLowerCase();

    for (var word of Array.from(words)) {
      if (content.indexOf(word) < 0) { return false; }
    }

    return true;
  };

};

const consoleAttribute = dom.body.attr("data-floating-console");

const forceFloating = (consoleAttribute === "enabled") || (consoleAttribute === "invisible");

const button = function(action: string, icon:string, label:string, disabled?: boolean) { if (disabled == null) { disabled = false; } return `\
<button data-action="${action}" class="btn btn-default btn-mini">
  ${bootstrap.glyph(icon)} ${label}
</button>\
`; };

// _internal_: displays the message inside the floating console, creating the floating
// console as needed.
const display = function(className: string, message: string) {

  if (!floatingConsole) {
    floatingConsole = dom.create("div",
      {class: "tapestry-console"},
      `\
<div class="message-container"></div>
<div class="row">
  <div class="btn-group btn-group-sm col-md-4">
    ${button("clear", "remove", "Clear Console")}
    ${button("enable", "play", "Enable Console")}
    ${button("disable", "pause", "Disable Console")}
  </div>
  <div class="col-md-8">
    <input class="form-control" size="40" placeholder="Filter console content">
  </div>
</div>\
`
    );

    dom.body.prepend(floatingConsole);

    // Basically, any non-blank value will enable the floating console. In addition, the special
    // value "invisible" will enable it but then hide it ... this is useful in tests, since
    // the console output is captured in the markup, but the visible console can have unwanted interactions
    // (such as obscuring elements that make them unclickable).
    if (consoleAttribute === "invisible") {
      floatingConsole.hide();
    }
  }

  messages = floatingConsole.findFirst(".message-container");

  floatingConsole.findFirst("[data-action=enable]")!.attr("disabled", true);

  floatingConsole.on("click", "[data-action=clear]", function() {
    floatingConsole!.hide();
    return messages!.update("");
  });

  floatingConsole.on("click", "[data-action=disable]", function() {

    floatingConsole!.attr("disabled", true);
    floatingConsole!.findFirst("[data-action=enable]")!.attr("disabled", false);

    messages!.hide();

    return false;
  });

  floatingConsole.on("click", "[data-action=enable]", function() {

    floatingConsole!.attr("disabled", true);
    floatingConsole!.findFirst("[data-action=disable]")!.attr("disabled", false);

    messages!.show();

    return false;
  });

  floatingConsole.on("change keyup", "input", function() {
    var value = floatingConsole!.value();
    updateFilter();

    for (e of Array.from(messages!.children())) {
      var visible = filter(e);

      e[visible ? "show" : "hide"]();
    }

    return false;
  });

  const div = dom.create("div",
    {class: className},
    _.escape(message));

    // Should really filter on original message, not escaped.

  if (!filter(div)) {
    div.hide();
  }

  messages!.append(div);

  // A slightly clumsy way to ensure that the container is scrolled to the bottom.
  return _.delay(() => messages!.element.scrollTop = messages!.element.scrollHeight);
};

const level = (className: string, consolefn?: (m: string) => any) => (function(message: string) {
  // consolefn may be null if there's no console; under IE it may be non-null, but not a function.
  // For some testing, it is nice to force the floating console to always display.

  if (forceFloating || (!consolefn)) {
    // Display it floating. If there's a real problem, such as a failed Ajax request, then the
    // client-side code should be alerting the user in some other way, and not rely on them
    // being able to see the logged console output.
    display(className, message);

    if (!forceFloating) { return; }
  }

  if (window.console && (_.isFunction(consolefn))) {
    // Use the available native console, calling it like an instance method
    consolefn.call(window.console, message);
    return;
  }

  // And IE just has to be different. The properties of console are callable, like functions,
  // but aren't proper functions that work with `call()` either.
  // On IE8, the console object is undefined unless debugging tools are enabled.
  // In that case, nativeConsole will be an empty object.
  if (consolefn) {
    consolefn(message);
  }

});

const noop = function() {};
const debugEnabled = ((document.documentElement.getAttribute("data-debug-enabled")) != null);

let exports_ = {
  info: level("info", nativeConsole!.info),
  warn: level("warn", nativeConsole!.warn),
  error: level("error", nativeConsole!.error),

  // Determine whether debug is enabled by checking for the necessary attribute (which is missing
  // in production mode).
  debugEnabled,

  debug: debugEnabled ?
    // If native console available, go for it.  IE doesn't have debug, so we use log instead.
    // Add a special noop case for IE8, since IE8 is just crazy.
    level("debug", (nativeConsole!.debug || nativeConsole!.log || noop)) : noop
};

declare global {
  interface Window {
    t5console: typeof exports_;
    requirejs: any | null;
  }
}

// This is also an aid to debugging; it allows arbitrary scripts to present on the console; when using Geb
// and/or Selenium, it is very useful to present debugging data right on the page.
window.t5console = exports_;

if (window.requirejs) {
  window.requirejs.onError = function(err: any) {

    let message = `RequireJS error: ${(err != null ? err.requireType : undefined) || 'unknown'}`;

    if (err.message) {
      message += `: ${err.message}`;
    }

    if (err.requireType) {
      const modules = err != null ? err.requireModules : undefined;
      if (modules && (modules.length > 0)) {
        message += `, modules ${modules.join(", ")}`;
      }
    }

    if (err.fileName) {
      message += `, ${err.fileName}`;
    }

    if (err.lineNumber) {
      message += `, line ${err.lineNumber}`;
    }

    if (err.columnNumber) {
      message += `, line ${err.columnNumber}`;
    }

    return exports_.error(message);
  };
};

export default exports_;