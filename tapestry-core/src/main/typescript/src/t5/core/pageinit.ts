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
 * ## t5/core/pageinit
 *
 * Module that defines functions used for page initialization.
 * The initialize function is passed an array of initializers; each initializer is itself
 * an array. The first value in the initializer defines the name of the module to invoke.
 * The module name may also indicate the function exported by the module, as a suffix following a colon:
 * e.g., "my/module:myfunc".
 * Any additional values in the initializer are passed to the function. The context of the function (this) is null.
 */

import _, { partial } from "underscore";
import console from "t5/core/console";
import dom from "t5/core/dom";
import events from "t5/core/events";
import { data } from "jquery";

let exports_: any;
let pathPrefix: String | null = null;

// Borrowed from Prototype:
// @ts-ignore
const isOpera = Object.prototype.toString.call(window.opera) === '[object Opera]';
// @ts-ignore
const isIE = !!window.attachEvent && !isOpera;

const requireJsEnabled = "true" == document.querySelector("html")?.dataset['requireJsEnabled'];

const rebuildURL = function(path: string) {
  if (path.match(/^https?:/)) { return path; }

  // See Tapestry.rebuildURL() for an error about the path not starting with a leading '/'
  // We'll assume that doesn't happen.

  if (!pathPrefix) {
    const l = window.location;
    pathPrefix = `${l.protocol}//${l.host}`;
  }

  return pathPrefix + path;
};

const rebuildURLOnIE =
  isIE ? rebuildURL : _.identity;

type StylesheetLink = {
  href: string;
  media?: string;
}

const addStylesheets = function(newStylesheets: StylesheetLink) {
  if (!newStylesheets) { return; }

  // Figure out which stylesheets are loaded; adjust for IE, and especially, IE 9
  // which can report a stylesheet's URL as null (not empty string).
  const loaded = _.chain(document.styleSheets)
  .pluck("href")
  .without("")
  .without(null)
  // @ts-ignore
  .map(rebuildURLOnIE);

  const insertionPoint = _.find(document.styleSheets, function(ss) {
    // @ts-ignore
    const parent = ss.ownerNode || ss.owningElement;
    return parent.rel === "stylesheet ajax-insertion-point";
});

  // Most browsers support document.head, but older IE doesn't:
  const head = document.head || document.getElementsByTagName("head")[0];

  _.chain(newStylesheets)
  .map(ss => ({
    // @ts-ignore
    href: rebuildURL(ss.href),
    // @ts-ignore
    media: ss.media
  }))
  .reject(ss => loaded.contains(ss.href).value())
  .each(function(ss) {
    const element = document.createElement("link");
    element.setAttribute("type", "text/css");
    element.setAttribute("rel", "stylesheet");
    element.setAttribute("href", ss.href);
    if (ss.media) {
      element.setAttribute("media", ss.media);
    }

    if (insertionPoint) {
      head.insertBefore(element, insertionPoint.ownerNode);
    } else {
      head.appendChild(element);
    }

    return console.debug(`Added stylesheet ${ss.href}`);
  });

};

const invokeInitializer = function(tracker: () => any, qualifiedName: string, initArguments: any[], requireJsModule: boolean) {
  const [moduleName, functionName] = Array.from(qualifiedName.split(':'));

  function executeInitializer(moduleLib: any) {

    if (!requireJsModule && moduleLib['default'] != null && _.isFunction(moduleLib['default'])) {
      moduleLib = moduleLib['default'];
    }

    try {
      // Some modules export nothing but do some full-page initialization, such as adding
      // event handlers to the body.
      if (!functionName &&
        (initArguments.length === 0) &&
        !_.isFunction(moduleLib)) {
          console.debug(`Loaded module ${moduleName}`);
          return;
        }

      if (!moduleLib) {
        throw new Error(`require('${moduleName}') returned ${moduleLib} when not expected`);
      }

      const fn = (functionName != null) ? moduleLib[functionName] : moduleLib;

      if (fn == null) {
        if (functionName) {
          console.error(`Could not locate function \`${qualifiedName}' in ${JSON.stringify(moduleLib)}`);
          console.error(moduleLib);
        }
        throw new Error(`Could not locate function \`${qualifiedName}'.`);
      }

      if (console.debugEnabled) {
        const argsString = (Array.from(initArguments).map((arg) => JSON.stringify(arg))).join(", ");
        console.debug(`Invoking ${qualifiedName}(${argsString})`);
      }

      return fn.apply(null, initArguments);
    } finally {
      tracker();
    }
    
  }

  if (requireJsModule) {

    // @ts-ignore
    return require([moduleName], function(moduleLib: any) {

      // If it's an AMD module generated by TypeScript and it has a default export,
      // it gets wrapped, so we try to unwrap it here.
      if (moduleLib != null && moduleLib.__esModule && moduleLib["default"] != null) {
        moduleLib = moduleLib["default"];
      }

      return executeInitializer(moduleLib);
    
    });

  } else {

    return import(moduleName).then(function(moduleLib: any) {
      return executeInitializer(moduleLib);
    });

  }
};

  // Pre-loads a number of libraries in order. When the last library is loaded,
  // invokes the callback (with no parameters).
function loadLibraries(libraries: string[], requireJsModules: boolean, callback: () => any) {
  // @ts-ignore
  const reducer = (callback, library) => (function() {
    console.debug(`Loading library ${library}`);
    if (requireJsModules) {
      // @ts-ignore
      return require([library], callback);
    }
    else {
      return import(library).then(callback);
    }
  });

  const finalCallback = _.reduceRight(libraries, reducer, callback);

  return finalCallback.call(null);
};

  // Passed a list of initializers, executes each initializer in order. Due to asynchronous loading
  // of modules, the exact order in which initializer functions are invoked is not predictable.
  // @ts-ignore
function initialize(inits, requireJsModules, callback) {
  if (inits == null) { inits = []; }
  console.debug(`Executing ${inits.length} ${requireJsModules ? "AMD" : "ES module"} inits`);
  let callbackCountdown = inits.length + 1;

  // tracker gets invoked once after each require/callback, plus once extra
  // (to handle the case where there are no inits). When the count hits zero,
  // it invokes the callback (if there is one).
  const tracker = function() {
    callbackCountdown--;

    if (callbackCountdown === 0) {
      console.debug(`All ${requireJsModules ? "AMD" : "ES"} inits executed`);
      if (callback) { return callback(); }
    }
  };

  // First value in each init is the qualified module name; anything after
  // that are arguments to be passed to the identified function. A string
  // is the name of a module to load, or function to invoke, that
  // takes no parameters.
  for (var init of Array.from(inits)) {
    if (_.isString(init)) {
      invokeInitializer(tracker, init, [], requireJsModules);
    } else {
      // @ts-ignore
      var [qualifiedName, ...initArguments] = Array.from(init);
      // @ts-ignore
      invokeInitializer(tracker, qualifiedName, initArguments, requireJsModules);
    }
  }

  return tracker();
}

// Loads all specified libraries in order (this includes the core stack, other stacks, and
// any free-standing libraries). It then executes the initializations. Once all initializations have
// completed (which is usually an asynchronous operation, as initializations may require the loading
// of further modules), then the `data-page-initialized` attribute of the `<body>` element is set to
// 'true'.
//
// This is the main export of the module; other functions are attached as properties.
// @ts-ignore
const loadLibrariesAndInitialize = function(libraries, inits, requireJsModules) {
  console.debug(`Loading ${(libraries != null ? libraries.length : undefined) || 0} libraries`);

  const dataAttribute = "data-page-initialized";
  const partialValue = "partial";
  const hasEsModulePageInit = document.querySelector("script[id='__tapestry-es-module-pageinit__']") != null;

  // @ts-ignore
  return loadLibraries(libraries, requireJsModules,
    // @ts-ignore
    () => initialize(inits, requireJsModules,
      function() {
        // At this point, all libraries have been loaded, and all inits should have executed. Unless some of
        // the inits triggered Ajax updates (such as a core/ProgressiveDisplay component), then the page should
        // be ready to go. We set a flag, mostly used by test suites, to ensure that all is ready.
        // Later Ajax requests will cause the data-ajax-active attribute to be incremented (from 0)
        // and decremented (when the requests complete).

        // If Require.js is enabled, this function may be called twice: once for the
        // Require.js initializations, another for the ES module ones.
        // That way, we need to only set the data-page-initialized attribute to
        // true when this function is called by the second time.
        if (requireJsEnabled && hasEsModulePageInit) {
          var value = dom.body.attr(dataAttribute);
          if (value !== partialValue) {
            dom.body.attr(dataAttribute, "partial");
            return;
          }
        }

        dom.body.attr("data-page-initialized", "true");

        return Array.from(dom.body.find(".pageloading-mask")).map((mask) =>
          mask.remove());
    }));
};

export default exports_ = _.extend(loadLibrariesAndInitialize, {

  initialize: initialize,

  loadLibraries: loadLibraries,

  evalJavaScript(js: string) {
    console.debug(`Evaluating: ${js}`);
    return eval(js);
  },

  // Triggers the focus event on the field, if the field exist. Focus occurs delayed 1/8th of a
  // second, which helps ensure that other initializions on the page are in place.
  //
  // * fieldId - element id of field to focus on
  focus: function(fieldId: string) {
    const field = dom(fieldId);

    if (field) {
      return _.delay((() => field.focus()), 125);
    }
  },

  // Passed the response from an Ajax request, when the request is successful.
  // This is used for any request that attaches partial-page-render data to the main JSON object
  // response.  If no such data is attached, the callback is simply invoked immediately.
  // Otherwise, Tapestry processes the partial-page-render data. This may involve loading some number
  // of JavaScript libraries and CSS style sheets, and a number of direct updates to the DOM. After DOM updates,
  // the callback is invoked, passed the response (with any Tapestry-specific data removed).
  // After the callback is invoked, page initializations occur.  This method returns null.

  // * response - the Ajax response object
  // * callback - invoked after scripts are loaded, but before page initializations occur (may be null)
  // @ts-ignore
  handlePartialPageRenderResponse(response, callback) {

    // Capture the partial page response portion of the overall response, and
    // then remove it so it doesn't interfere elsewhere.
    const responseJSON = response.json || {};
    const partial = responseJSON._tapestry;
    delete responseJSON._tapestry;

    // Extreme case: the data has a redirectURL which forces an immediate redirect to the URL.
    // No other initialization or callback invocation occurs.
    if (partial != null ? partial.redirectURL : undefined) {
      if (window.location.href === partial.redirectURL) {
        // @ts-ignore
        window.location.reload(true);
      } else {
        window.location.href = partial.redirectURL;
      }
      return;
    }

    addStylesheets(partial != null ? partial.stylesheets : undefined);

    // Make sure all libraries are loaded
    exports_.loadLibraries(partial != null ? partial.libraries : undefined, function() {

      // After libraries are loaded, update each zone:
      _(partial != null ? partial.content : undefined).each(function(...args) {
        const [id, content] = Array.from(args[0]);
        console.debug(`Updating content for zone ${id}`);

        // @ts-ignore
        const zone = dom.wrap(id);

        if (zone) {
          return zone.trigger(events.zone.update, { content });
        }});

      // Invoke the callback, if present.  The callback may do its own content updates.
      callback && callback(response);

      // Lastly, perform initializations from the partial page render response.
      return exports_.initialize(partial != null ? partial.inits : undefined);
    });

  }
})