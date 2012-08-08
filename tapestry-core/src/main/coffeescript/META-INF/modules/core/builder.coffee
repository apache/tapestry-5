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

# core/builder
#
# A system for constructing DOM element nodes for a particular structure in minimal code.  The basic syntax is:
# builder(elementDescription, body...) and the result is a DOM element.  The element description is
# primarily the name of the element.
#
# It may contain sequences of ".name"; these appropriate CSS syntax to describe a CSS class name for the element.  The
# element name may be omitted when CSS class names are specified, in which case "div" is the default. Multiple CSS class
# names are allowed.
#
# The description may also include a '>' character; this represents the start of a nested element; in this way
# a structure can quickly be specified.
#
# The body may consist of:
# Objects: used to specify attributes and event handles of the element
# Strings: literal text
# Array: a nested element definition
# For an Object, each key and value is simply added as an attribute. However, keys that start with "on" are assumed to
# be event handler functions. The special key "on" consists of nested event handlers for the keys. The following are
# equivlent:
# { onclick: -> ... } and
# { on: { click: -> ... }}
define ["_", "core/spi"], (_, spi) ->
  createElement = (elementDescription) ->
    # TODO: Support #id for setting the id of an element, maybe others, such as ?name for the name of an input element.
    # That will require a regex or more sophisticated parsing.
    terms = elementDescription.trim().split(".")

    elementName = terms.shift() or "div"

    element = document.createElement elementName

    element.className = terms.join " "

    return element

  addAttributes = (element, attributes) ->
    return unless attributes

    wrapper = spi.wrap element

    for name, value of attributes
      if name is "on"
        for eventName, handler of value
          wrapper.on eventName, handler
      else if name.startsWith "on"
        wrapper.on (name.substring 2), value
      else
        wrapper.setAttribute name, value

    return null

  addAttributesAndBody = (element, body) ->
    for nested in body
      unless nested?
        # Ignore null nodes
      else if _.isString nested
        element.appendChild document.createTextNode nested
      else if _.isArray nested
        [elementDescription, nestedBody...] = nested
        nestedElement = buildTree elementDescription, nestedBody
        element.appendChild nestedElement
      else if _.isObject nested
        addAttributes element, nested
      else throw new Error "Unexpected body value <#{nested}> while building DOM elements."

    return null

  buildTree = (elementDescription, body) ->
    splitx = elementDescription.indexOf ">"
    currentDescription =
      if splitx is -1
        elementDescription
      else
        elementDescription.substring 0, splitx

    element = createElement currentDescription

    if splitx is -1
      addAttributesAndBody element, body
    else
      nestedDescription = elementDescription.substring splitx + 1
      nestedElement = buildTree nestedDescription, body
      element.appendChild nestedElement

    return element

  # The module exports a single function that builds the tree of elements and returns the top element, wrapped as an
  # spi.ElementWrapper.
  (elementDescription, body...) ->
    element = buildTree elementDescription, body

    spi.wrap element