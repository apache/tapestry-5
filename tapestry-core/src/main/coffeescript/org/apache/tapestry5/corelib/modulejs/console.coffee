# Copyright 2012 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

define ->
  nativeConsole = {}
  floatingConsole = null

  exports =
    DURATION : 10 # seconds

  try
    nativeConsole = console
  catch e

  display = (className, message) ->
    # TODO: Dependency on Prototype here

    unless floatingConsole
      floatingConsole = new Element "div", class: "t-console"
      $(document.body).insert top: floatingConsole

    div = new Element "div", class: "t-console-entry #{className}"
    div.update(message).hide()
    floatingConsole.insert top:div

    new Effect.Appear div, duration: .25

    fade = new Effect.Fade div,
      delay: exports.DURATION
      afterFinish: -> div.remove()  # was T5.dom.remove(div)

    div.observe "click", ->
      effect.cancel()
      div.remove() # was T5.dom.remove(div)

  level = (className, consolefn) ->
    (message) ->
      # Display it floating
      display className, message

      # If native console available, go for it
      consolefn and consolefn.call(console, message)

  exports[name] = level("t-#{name}", nativeConsole[name]) for name in ["debug", "info", "warn"]
  exports.error = level("t-err", nativeConsole.error)

  # Return the exports; we keep a reference to it, so we can see exports.DURATION, even
  # if someone else modifies it.
  return exports