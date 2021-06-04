# Copyright 2012, 2013 The Apache Software Foundation
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
# Handlers to support to the core/Tree Tapestry component.
define ["t5/core/dom", "t5/core/ajax", "t5/core/zone"],
  (dom, ajax) ->
    TREE = "[data-component-type='core/Tree']"
    NODE_ID = "data-node-id"
    SELECTOR = "#{TREE} [#{NODE_ID}]"

    LOADING = "tree-children-loading"
    LOADED = "tree-children-loaded"
    EXPANDED = "tree-expanded"
    SELECTED = "selected-leaf-node"

    send = (node, action, success) ->
      container = node.findParent TREE
      url = container.attr "data-tree-action-url"

      ajax url,
        data:
          "t:action": action
          "t:nodeid": node.attr NODE_ID
        success: success

    loadChildren = (node) ->

      # Ignore duplicate requests to load the children.
      return if node.meta LOADING

      node.meta LOADING, true

      node.addClass "empty-node"
      node.update "<span class='tree-ajax-wait'/>"

      send node, "expand", (response) ->
        # Remove the Ajax spinner and  mark the node as expanded (it will have a "-"
        # icon instead of a "+" icon)
        node.update("").addClass(EXPANDED).removeClass("empty-node")

        label = node.findParent("li").findFirst(".tree-label")

        label.insertAfter response.json.content

        node.meta LOADING, false
        node.meta LOADED, true

    # toggles a folder in the tree between expanded and collapsed (once data for the folder
    # has been loaded).
    toggle = (node) ->
      sublist = node.findParent("li").findFirst("ul")

      if node.hasClass EXPANDED
        node.removeClass EXPANDED
        sublist.hide()
        send node, "markCollapsed"
        return

      node.addClass EXPANDED
      sublist.show()
      send node, "markExpanded"

    # The handler is triggered on the `<span data-node-id=''>` directly inside the `<li>`.
    clickHandler = ->

      # Ignore clicks on leaf nodes, and on folders that are known to be empty.
      if (@parent().hasClass "leaf-node") or (@hasClass "empty-node")
        return false

      # If not already loaded then fire off the Ajax request to load the content.
      if (@meta LOADED) or (@hasClass EXPANDED)
        toggle this
      else
        loadChildren this

      return false

    toggleSelection = ->

      selected = @hasClass SELECTED

      node = @findParent("li").findFirst("[#{NODE_ID}]")

      if selected
        @removeClass SELECTED
        send node, "deselect"
      else
        @addClass SELECTED
        send node, "select"

      return false

    dom.onDocument "click", SELECTOR, clickHandler

    dom.onDocument "click",
      "#{TREE}[data-tree-node-selection-enabled] LI.leaf-node > .tree-label",
      toggleSelection


    return null
  
