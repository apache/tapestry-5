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

# ## core/pubsub
#
# Framework/application publish and subscribe that is independent of
# the DOM. Parts of this are modelled on how jQuery DOM events work.
define ["_"],
  (_) ->

    exports = {}

    subscribers = { }

    kullSubs = (name, namespaces, responder) ->
      list = subscribers[name]
      return if list is null

      # Work backwards to avoid excessive copies
      for i in [list.length - 1 .. 0]
        subscriber = list[i]
        # TODO: _.intersection is expensive
        if (!responder or subscriber.fn is responder) and
           (namespaces.length == 0 or _.intersection(namespaces, subscriber.namespaces).length > 0)
          if list.iterating
            subscribers[name] = list = list[..]
          list.splice i, 1

      return

    addSub = (stimulusName, addFirst, responder) ->
      [simpleName, namespaces...] = stimulusName.split '.'

      subscriber =
        stimulus: simpleName
        namespaces: namespaces
        fn: responder

      list = subscribers[simpleName]

      if not list
        subscribers[simpleName] = [subscriber]
      else
        # If iterating the list (during a publish), then do a copy-on-write.
        # This allows a clean way to have listeners unsubscribe one first notification, or
        # otherwise change the subscriptions.
        if list.iterating
          subscribers[simpleName] = list = list.slice[..]

        list[if addFirst then "unshift" else "push"] subscriber

      return

    # Adds a responder for a stimulus.  The stimulus name consists of a simple topic name
    # (used when publishing a message), and optional set of namespaces; each namespace
    # is preceded by a '.'.  The namespaces make it easier to identify specific responders
    # later, such as when removing a responder.
    # The ordering identifies where the listener will be placed.
    # The listenerfn is a function to be invoked via the publisher. A topic publisher
    # will publish a message with an application-specific memo.  The listenerfn receives
    # the memo parameter, and an event parameter.  The event parameter allows the
    # message to be terminated early.
    #
    # * stimulus - name of stimulus to respond to, with namespaces appended
    # * responder - function to be invoked when the stimulus is fired. The responder
    # is passed a memo object, and an event. The event can be used to cancel or
    # pause the stimulus.
    #
    # Returns this module's exports, for easy chaining of calls.
    exports.respondTo = exports.on = (stimulusName, responder) ->

      addSub stimulusName, false, responder

      return exports

    # Adds a responder for a stimulus, but the responder is added first in the list.
    # Otherwise, the same as `respondTo()`.
    # Returns this module's exports, for easy chaining of calls.
    exports.respondFirst = exports.first = (stimulusName, responder) ->
      addSub stimulusName, true, responder

      return exports

    # Stops responding to a stimulus. The parameters identify zero or more responders
    # that will no longer be triggered.
    # Only previously added responders that match a specifically as possible will be removed.
    # If a responder is provided, it must match exactly.
    # If a stimulus name is provided, the name must match exactly.
    # If any namespaces were provided, then at least ONE namespace must match a namespace provided when
    # the responder was added.
    #
    # * stimulusName - optional, identifies a stimulus and optional namespaces
    # * responder - optional, identifies a specific responder to remove
    #
    # Returns this module's exports, to support chaining.
    exports.stopResponding = exports.off = (stimulusName = "", responder) ->
      if _.isFunction stimulusName
        responder = stimulusName
        stimulusName = ""

      [simpleName, namespaces...] = stimulusName.split '.'

      if simpleName isnt ""
        kullSubs simpleName, namespaces, responder
      else
        kullSubs name, namespaces, responder for name of subscribers

      return exports


    # Fires the stimulus, passing the memo and an event to each responder that was previously added.
    # The event can be used to terminate the stimular prematurely, via its stop() method.
    # The context object defines the value of this for
    # the invoked responders; it is often null (or omitted).
    # stimulusName - simple name of stimulus to fire (it should not contain namespaces)
    #
    # * memo - object to be passed to each responder (as the first parameter)
    # * context - context value (this) used when invoking responders
    #
    # Returns this module's exports, to support chaining.
    exports.fire = (stimulusName, memo, context) ->
      list = subscribers[stimulusName]

      return exports if not list

      event =
        memo: memo
        running: true
        stimulus: stimulusName
        stop: -> running = false
        # TODO: pause and resume

      try
        wasIterating = list.iterating
        list.iterating = true

        for subscriber in list when event.running
          # Pass the memo and the event to the responder.
          subscriber.fn.call context, memo, event

      finally
        list.iterating = wasIterating

      return exports

    # Result of define:
    return exports
