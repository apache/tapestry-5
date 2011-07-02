// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

T5.define("console", function() {

  var console;

  function updateConsole(className, message) {
    if (!console) {
      var console = new Element("div", { "class" : "t-console" });

      $(document.body).insert({top: console});
    }

    var div = new Element("div", { 'class' : className }).update(message).hide();

    console.insert({ top: div });

    new Effect.Appear(div, { duration: .25 });

    var effect = new Effect.Fade(div, { delay: T5.console.DURATION,
      afterFinish: function () {
        T5.dom.remove(div);
      }
    });

    div.observe("click", function() {
      effect.cancel();
      T5.dom.remove(div);
    });
  }

  // TODO: replace this with a curry

  function withClassName(className) {
    return function (message) {
      updateConsole(className, message);
    }
  }

  return {
    /** Time, in seconds, that floating console messages are displayed to the user. */
    DURATION  : 10,

    debug : withClassName("t-debug"),
    info : withClassName("t-info"),
    warn : withClassName("t-warn"),
    error : withClassName("t-error")
  };
});