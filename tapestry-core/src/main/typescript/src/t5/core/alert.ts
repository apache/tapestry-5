// Copyright 2012-2025 The Apache Software Foundation
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
 * ## t5/core/alert
 * 
 * Support for the core/Alerts components.
 */
import dom from "t5/core/dom";
import console from "t5/core/console";
import messages from "t5/core/messages";
import ajax from "t5/core/ajax";
import { ElementWrapper } from "t5/core/types";
import _ from "underscore";
import bootstrap from "t5/core/bootstrap";

const TRANSIENT_DURATION = 5000;
let exports_;
const severityToClass: { [key: string]: string } = {
  info: "alert-info",
  success: "alert-success",
  warn: "alert-warning",
  error: "alert-danger"
};

type Severity = keyof typeof severityToClass;

const getURL = (container: ElementWrapper) => container.attr("data-dismiss-url");

const removeAlert = function(container: ElementWrapper, alert: ElementWrapper) {
  alert.remove();

  if (container.find(".alert").length === 0) {
    return container.update(null);
  }
};

const dismissAll = function(container: ElementWrapper) {

  const alerts = container.find("[data-alert-id]");

  if (alerts.length === 0) {
    container.update(null);
    return;
  }

  return ajax(getURL(container) as string,
    {success() { return container.update(null); }});
};

const dismissOne = function(container: ElementWrapper, button: ElementWrapper) {

  const alert = button.parent()!;

  const id = alert.attr("data-alert-id");

  if (!id) {
    removeAlert(container, alert);
    return;
  }

  return ajax((getURL(container) as string), {
    data: { id },
    success() { return removeAlert(container, alert); }
  }
  );
};

const setupUI = function(outer: ElementWrapper) {

  outer.update(`\
<div data-container-type="inner"></div>\
`
  );

  if ((outer.attr("data-show-dismiss-all")) === "true") {
    outer.append(`\
<div class="pull-right">
<button class="btn btn-xs btn-default" data-action="dismiss-all">
  ${bootstrap.glyph("remove")}
  ${messages("core-dismiss-label")}
</button>
</div>\
`
    );
  }

  outer.on("click", "[data-action=dismiss-all]", function() {
    dismissAll(outer);
    return false;
  });

  return outer.on("click", "button.close", function() {
    // @ts-ignore
    let element: ElementWrapper = this;
    dismissOne(outer, element);
    return false;
  });
};

const findInnerContainer = function() {
  const outer = dom.body.findFirst("[data-container-type=alerts]");

  if (!outer) {
    console.error("Unable to locate alert container element to present an alert.");
    return null;
  }

  // Set up the inner content when needed
  if (!outer.element.firstChild) {
    setupUI(outer);
  }

  return (outer != null ? outer.findFirst("[data-container-type=inner]") : undefined);
};

/**
 * The `data` for the alert has a number of keys to control its behavior:
 * 
 * * severity - used to determine the CSS class, may be "warn", "error", or "info" (the default)
 * * message - message to display to as te alert's body
 * * markup - if true, then the message contains markup that should not be HTML escaped
 */
const alert = function(data: { [x: string]: any; severity: Severity; markup: boolean; message: string; id: any; }) {

  const container = findInnerContainer();

  if (!container) { return; }

  // Map from severity name to a CSS class; using alert-info if no severity, or unknown severity
  const className = severityToClass[data.severity] || "alert-info";

  const content = data.markup ? data.message : _.escape(data.message);

  // Note that `data-dismiss=alert` is purposely excluded
  // - we want to handle closes w/ notifications to the server if not transient
  // - we don't want to rely on bootstrap.js, as that will drag jQuery into the application
  // Also, the <span> tag makes it easier to pull out just the content when doing tests,
  // but we only add this add if the alert doesn't have a message that contains markup (TAP5-1863)
  const element = dom.create("div", {
    "data-alert-id": data.id,
    class: "alert alert-dismissable " + className
  },
    data.markup ?
      `\
<button type="button" class="close">&times;</button>
${content}\
`
    :
      `\
<button type="button" class="close">&times;</button>
<span>${content}</span>\
`
  );

  container.append(element);

  if (data['transient']) {
    const outerContainer = container.findParent('[data-container-type=alerts]');
    return _.delay(removeAlert, TRANSIENT_DURATION, outerContainer, element);
  }
};

alert.TRANSIENT_DURATION = TRANSIENT_DURATION;

export default exports_;
