/*
 * decaffeinate suggestions:
 * DS102: Remove unnecessary code created because of implicit returns
 * Full docs: https://github.com/decaffeinate/decaffeinate/blob/main/docs/suggestions.md
 */
// Copyright 2012, 2013 The Apache Software Foundation
//
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

// ## t5/core/tree
//
// Handlers to support to the core/Tree Tapestry component.
define(["t5/core/dom", "t5/core/ajax", "t5/core/zone"],
  function(dom, ajax) {
    const TREE = "[data-component-type='core/Tree']";
    const NODE_ID = "data-node-id";
    const SELECTOR = `${TREE} [${NODE_ID}]`;

    const LOADING = "tree-children-loading";
    const LOADED = "tree-children-loaded";
    const EXPANDED = "tree-expanded";
    const SELECTED = "selected-leaf-node";

    const send = function(node, action, success) {
      const container = node.findParent(TREE);
      const url = container.attr("data-tree-action-url");

      return ajax(url, {
        data: {
          "t:action": action,
          "t:nodeid": node.attr(NODE_ID)
        },
        success
      }
      );
    };

    const loadChildren = function(node) {

      // Ignore duplicate requests to load the children.
      if (node.meta(LOADING)) { return; }

      node.meta(LOADING, true);

      node.addClass("empty-node");
      node.update("<span class='tree-ajax-wait'/>");

      return send(node, "expand", function(response) {
        // Remove the Ajax spinner and  mark the node as expanded (it will have a "-"
        // icon instead of a "+" icon)
        node.update("").addClass(EXPANDED).removeClass("empty-node");

        const label = node.findParent("li").findFirst(".tree-label");

        label.insertAfter(response.json.content);

        node.meta(LOADING, false);
        return node.meta(LOADED, true);
      });
    };

    // toggles a folder in the tree between expanded and collapsed (once data for the folder
    // has been loaded).
    const toggle = function(node) {
      const sublist = node.findParent("li").findFirst("ul");

      if (node.hasClass(EXPANDED)) {
        node.removeClass(EXPANDED);
        sublist.hide();
        send(node, "markCollapsed");
        return;
      }

      node.addClass(EXPANDED);
      sublist.show();
      return send(node, "markExpanded");
    };

    // The handler is triggered on the `<span data-node-id=''>` directly inside the `<li>`.
    const clickHandler = function() {

      // Ignore clicks on leaf nodes, and on folders that are known to be empty.
      if ((this.parent().hasClass("leaf-node")) || (this.hasClass("empty-node"))) {
        return false;
      }

      // If not already loaded then fire off the Ajax request to load the content.
      if ((this.meta(LOADED)) || (this.hasClass(EXPANDED))) {
        toggle(this);
      } else {
        loadChildren(this);
      }

      return false;
    };

    const toggleSelection = function() {

      const selected = this.hasClass(SELECTED);

      const node = this.findParent("li").findFirst(`[${NODE_ID}]`);

      if (selected) {
        this.removeClass(SELECTED);
        send(node, "deselect");
      } else {
        this.addClass(SELECTED);
        send(node, "select");
      }

      return false;
    };

    dom.onDocument("click", SELECTOR, clickHandler);

    dom.onDocument("click",
      `${TREE}[data-tree-node-selection-enabled] LI.leaf-node > .tree-label`,
      toggleSelection);


    return null;
});
  
