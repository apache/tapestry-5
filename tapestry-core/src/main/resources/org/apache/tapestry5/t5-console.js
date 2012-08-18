// Copyright 2011, 2012 The Apache Software Foundation
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

    // FireFox throws an exception is you reference the console when it is not enabled.

    var nativeConsole = {}, floatingConsole;

    try {
        if (console) {
            nativeConsole = console;
        }
    }
    catch (e) {
        // No true native console, the empty nativeConsole object will take its place.
    }

    function display(className, message) {
        if (!floatingConsole) {
            floatingConsole = new Element("div", { "class" : "t-console" });

            $(document.body).insert({top: floatingConsole});
        }

        var div = new Element("div", { 'class' : "t-console-entry " + className }).update(message).hide();

        floatingConsole.insert({ top: div });

        new Effect.Appear(div, { duration: .25 });

        var effect = new Effect.Fade(div, { delay:  T5.console.DURATION,
            afterFinish: function () {
                T5.dom.remove(div);
            }
        });

        div.observe("click", function() {
            effect.cancel();
            T5.dom.remove(div);
        });
    }

    function level(className, consolefn) {
        return function (message) {
            display(className, message);

            // consolefn may be null when there is no native console, in which case
            // do nothing more. It may be a non-function under IE.
            T5._.isFunction(consolefn) && consolefn.call(console, message);
        }
    }

    return {
        /** Time, in seconds, that floating console messages are displayed to the user. */
        DURATION  : 10,

        debug : level("t-debug", nativeConsole.debug),
        info : level("t-info", nativeConsole.info),
        warn : level("t-warn", nativeConsole.warn),
        error : level("t-err", nativeConsole.error)
    };
});
