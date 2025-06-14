// Copyright 2012, 2013, 2025 The Apache Software Foundation
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

/**
 * ## t5/core/tree
 * 
 * Handlers to support to the core/Tree Tapestry component.
 */

import dom from "t5/core/dom";
import ajax from "t5/core/ajax";
import "t5/core/zone";

const TREE = "[data-component-type='core/Tree']";
const NODE_ID = "data-node-id";
const SELECTOR = `${TREE} [${NODE_ID}]`;

const LOADING = "tree-children-loading";
const LOADED = "tree-children-loaded";
const EXPANDED = "tree-expanded";
const SELECTED = "selected-leaf-node";

// @ts-ignore
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

// @ts-ignore
const loadChildren = function(node) {

  // Ignore duplicate requests to load the children.
  if (node.meta(LOADING)) { return; }

  node.meta(LOADING, true);

  node.addClass("empty-node");
  node.update("<span class='tree-ajax-wait'/>");

  // @ts-ignore
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
// @ts-ignore
const toggle = function(node) {
  const sublist = node.findParent("li").findFirst("ul");

  if (node.hasClass(EXPANDED)) {
    node.removeClass(EXPANDED);
    sublist.hide();
    // @ts-ignore
    send(node, "markCollapsed");
    return;
  }

  node.addClass(EXPANDED);
  sublist.show();
  // @ts-ignore
  return send(node, "markExpanded");
};

// The handler is triggered on the `<span data-node-id=''>` directly inside the `<li>`.
const clickHandler = function() {

  // Ignore clicks on leaf nodes, and on folders that are known to be empty.
  // @ts-ignore
  if ((this.parent().hasClass("leaf-node")) || (this.hasClass("empty-node"))) {
    return false;
  }

  // If not already loaded then fire off the Ajax request to load the content.
  // @ts-ignore
  if ((this.meta(LOADED)) || (this.hasClass(EXPANDED))) {
    // @ts-ignore
    toggle(this);
  } else {
    // @ts-ignore
    loadChildren(this);
  }

  return false;
};

const toggleSelection = function() {

  // @ts-ignore
  const selected = this.hasClass(SELECTED);

  // @ts-ignore
  const node = this.findParent("li").findFirst(`[${NODE_ID}]`);

  if (selected) {
    // @ts-ignore
    this.removeClass(SELECTED);
    // @ts-ignore
    send(node, "deselect");
  } else {
    // @ts-ignore
    this.addClass(SELECTED);
    // @ts-ignore
    send(node, "select");
  }

  return false;
};

dom.onDocument("click", SELECTOR, clickHandler);

dom.onDocument("click",
  `${TREE}[data-tree-node-selection-enabled] LI.leaf-node > .tree-label`,
  toggleSelection);
