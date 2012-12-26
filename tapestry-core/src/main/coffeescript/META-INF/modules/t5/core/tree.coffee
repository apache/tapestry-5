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

# ## t5/core/tree
#
# Handlers to support to the core/Tree Tapestry component
define ["./dom", "./ajax", "./zone"],
  (dom, ajax) ->
    TREE = "[data-component-type=core/Tree]"
    NODE_ID = "data-node-id"
    SELECTOR = "#{TREE} [#{NODE_ID}]"

    LOADING = "tree-children-loading"
    LOADED = "tree-children-loaded"
    EXPANDED = "t-tree-expanded"
    SELECTED = "t-selected-leaf-node"

    send = (node, action, onsuccess) ->
      container = node.findContainer TREE
      url = container.attribute "data-tree-action-url"

      ajax url,
        parameters:
          "t:action": action
          "t:nodeid": node.attribute NODE_ID
        onsuccess: onsuccess

    loadChildren = (node) ->

      # Ignore duplicate requests to load the children.
      return if node.meta LOADING

      node.meta LOADING, true

      node.addClass "t-empty-node"
      node.update "<span class='t-ajax-wait'/>"

      send node, "expand", (reply) ->
        # Remove the Ajax spinner and  mark the node as expanded (it will have a "-"
        # icon instead of a "+" icon)
        node.update("").addClass(EXPANDED).removeClass("t-empty-node")

        label = node.findContainer("li").findFirst(".t-tree-label")

        label.insertAfter reply.responseJSON.content

        node.meta LOADING, false
        node.meta LOADED, true

    toggle = (node) ->
      sublist = node.findContainer("li").findFirst("ul")

      if node.hasClass EXPANDED
        node.removeClass EXPANDED
        sublist.hide()
        send node, "markCollapsed"
        return

      node.addClass EXPANDED
      sublist.show()
      send node, "markExpanded"

    clickHandler = ->

      # First case is dynamically loaded due to user action; second case
      # is rendered with overall page due to server-side expansion model.
      loaded = (this.meta LOADED) or (this.hasClass EXPANDED)

      if (not loaded) and (not this.hasClass "t-empty-node")
        loadChildren this
        return false

      unless this.hasClass "t-leaf-node"
        toggle this
        return false

      return false

    toggleSelection = ->

      selected = this.hasClass SELECTED

      node = this.findContainer("li").findFirst("[#{NODE_ID}]")

      if selected
        this.removeClass SELECTED
        send node, "deselect"
      else
        this.addClass SELECTED
        send node, "select"

      return false

    dom.onDocument "click", SELECTOR, clickHandler

    dom.onDocument "click",
      "#{TREE}[data-tree-node-selection-enabled] LI.t-leaf-node > .t-tree-label",
      toggleSelection


    return null
  
