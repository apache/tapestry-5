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

# ##core/console
#
# A wrapper around the native console, when it exists.
define ["core/spi", "core/builder", "_"], (spi, builder, _) ->
  nativeConsole = {}
  floatingConsole = null

  FADE_DURATION = 0.25

  # module exports are mutable; someone else could
  # require this module to change the default DURATION
  exports =
  # Default duration for floating console is 10 seconds.
    DURATION: 10

  try
  # FireFox will throw an exception if you even access the console object and it does
  # not exist. Wow!
    nativeConsole = console
  catch e

  # _internal_: displays the message inside the floating console, creating the floating
  # console as needed.
  display = (className, message) ->
    unless floatingConsole
      floatingConsole = builder ".t-console"
      spi.body().prepend floatingConsole

    div = builder ".t-console-entry.#{className}", message

    floatingConsole.append div.hide().fadeIn FADE_DURATION

    removed = false

    runFadeout = ->
      div.fadeOut FADE_DURATION, ->
        div.remove() unless removed

    window.setTimeout runFadeout, exports.DURATION * 1000

    div.on "click", ->
      div.remove()
      removed = true

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