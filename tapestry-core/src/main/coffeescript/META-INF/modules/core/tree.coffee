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

# ## core/tree
#
# Handlers to support to the core/Tree Tapestry component
define ["core/dom", "core/ajax", "core/zone"],
  (dom, ajax) ->

    TREE = "[data-component-type=core/Tree]"
    NODE_ID = "data-node-id"
    SELECTOR = "#{TREE} [#{NODE_ID}]"

    LOADING = "tree-children-loading"
    LOADED = "tree-children-loaded"

    loadChildren = (node) ->

      # Ignore duplicate requests to load the children.

      return if node.meta LOADING

      container = node.findContainer TREE
      url = container.attribute "data-tree-action-url"

      node.meta LOADING, true

      node.addClass "t-empty-node"
      node.update "<span class='t-ajax-wait'/>"

      ajax url,
        parameters:
          "t:action": "expand"
          "t:nodeid": node.attribute NODE_ID
        onsuccess: (reply) ->
          node.update("").removeClass "t-empty-node"

          label = node.findContainer("li").findFirst(".t-tree-label")

          label.insertAfter reply.responseJSON.content

          node.meta LOADING, false
          node.meta LOADED, true

    clickHandler = ->

      # First case is dynamically loaded due to user action; second case
      # is rendered with overall page due to server-side expansion model.
      loaded = (this.meta LOADED) or (this.attribute "data-node-expanded")

      if (not loaded) and (not this.hasClass "t-empty-node")
        loadChildren this
        return false

      return false

    dom.onDocument "click", SELECTOR, clickHandler


    return null
  
