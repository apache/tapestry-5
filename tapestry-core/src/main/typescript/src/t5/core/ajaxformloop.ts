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
 * ## t5/core/ajaxformloop
 * 
 * Provides handlers related to the core/AjaxFormLoop component (as well as core/AddRowLink and
 * core/RemoveRowLink).
 */
import dom from "t5/core/dom";
import events from "t5/core/events";
import console from "t5/core/console"
import ajax from  "t5/core/ajax";
import { ElementWrapper, ResponseWrapper } from "t5/core/types";

// "afl" is short for "AjaxFormLoop".
const AFL_SELECTOR = "[data-container-type='core/AjaxFormLoop']";
const FRAGMENT_TYPE = "core/ajaxformloop-fragment";

dom.onDocument("click", `${AFL_SELECTOR} [data-afl-behavior=remove]`, function() {

  // @ts-ignore
  let element: ElementWrapper = this;
  const afl = element.findParent(AFL_SELECTOR);

  if (!afl) {
    console.error("Enclosing element for AjaxFormLoop remove row link not found.");
    return false;
  }

  const url = afl.attr("data-remove-row-url") as string;

  ajax(url!, {
    data: {
      "t:rowvalue": (element.closest("[data-afl-row-value]"))!.attr("data-afl-row-value")
    },
    success: () => {
      // The server has removed the row from persistent storage, lets
      // do the same on the UI.

      const fragment = element.findParent(`[data-container-type='${FRAGMENT_TYPE}']`);

      // TODO: Fire some before & after events, to allow for animation.

      // The fragment takes with it the hidden fields that control form submission
      // for its portion of the form.
      return fragment!.remove();
    }
  }
  );

  return false;
});

dom.onDocument("click", `${AFL_SELECTOR} [data-afl-behavior=insert-before] [data-afl-trigger=add]`, function() {

  // @ts-ignore
  let element: ElementWrapper = this;

  const afl = element.findParent(AFL_SELECTOR);

  const insertionPoint = element.findParent("[data-afl-behavior=insert-before]")!;

  const url = afl!.attr("data-inject-row-url") as string;

  ajax(url, {
    success(response: ResponseWrapper) {
      const content = (response.json != null ? response.json.content : undefined) || "";

      // Create a new element with the same type (usually "div") and class as this element.
      // It will contain the new content.

      const newElement = dom.create(insertionPoint.element.tagName,
                              {'class': insertionPoint.element.className, 'data-container-type': FRAGMENT_TYPE},
                              content);


      insertionPoint.insertBefore(newElement);

      // Initialize components inside the new row
      newElement.trigger(events.initializeComponents);

      // Trigger this event, to inform the world that the zone-like new element has been updated
      // with content.
      insertionPoint.trigger(events.zone.didUpdate);

    }
  }
  );

  return false;
});

// This module is all event handlers, and no exported functions.