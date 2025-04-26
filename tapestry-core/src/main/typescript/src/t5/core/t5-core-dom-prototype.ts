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

import type { ElementWrapper, EventWrapper, RequestWrapper, ResponseWrapper, ElementOffset, DOM, AddableContent, OnEventHandler, AjaxRequestOptions } from "t5/core/types";
import _ from "underscore";
import utils from "t5/core/utils";
import events from "t5/core/events";

// @ts-ignore
const $ = window.$ as (element: string | HTMLElement) => HTMLElement | null;

// @ts-ignore
const $$ = window.$$ as (selector: string | string[]) => HTMLElement[];

const on = function(selector: string | HTMLElement | Document | HTMLElement[], events: string, match: string | null, handler: OnEventHandler) {
  let elements;
  if (handler == null) {
    // @ts-ignore
    handler = match;
    match = null;
  }
  // @ts-ignore
  elements = parseSelectorToElements(selector);
  // @ts-ignore
  events = utils.split(events);
  // @ts-ignore
  return onevent(elements, events, match, handler);
}

const fireNativeEvent = function(element: HTMLElement, eventName: string) {
  let event;
  // @ts-ignore
  if (document.createEventObject) {
    // @ts-ignore
    event = document.createEventObject();
    // @ts-ignore
    return element.fireEvent("on" + eventName, event);
  }
  event = document.createEvent("HTMLEvents");
  event.initEvent(eventName, true, true);
  element.dispatchEvent(event);
  return !event.defaultPrevented;
};

const parseSelectorToElements = function(selector: string | string[] | HTMLElement | ElementWrapper) {
  if (_.isString(selector)) {
    return $$(selector);
  }
  if (_.isArray(selector)) {
    return selector;
  }
  return [selector];
};

const convertContent = function(content: string | HTMLElement | PrototypeElementWrapper): Element | string {
  if (_.isString(content)) {
    return content;
  }
  // @ts-ignore
  if (_.isElement(content)) {
    return content;
  }
  if (content instanceof PrototypeElementWrapper) {
    return content.element;
  }
  throw new Error("Provided value <" + content + "> is not valid as DOM element content.");
};

class PrototypeEventWrapper implements EventWrapper {
  readonly nativeEvent: Event;
  readonly memo: any;
  readonly type: string;
  readonly char?: string;
  readonly key?: string;

  constructor(event: Event) {
    let i, len, name, ref;
    this.nativeEvent = event;
    // @ts-ignore
    this.memo = event.memo;
    this.type = event.type;
    const o = event as any;
    this.char = o['char'];
    this.key = o['key'];
  }

  stop() {
    // @ts-ignore
    return this.nativeEvent.stop();
  };

}

type PrototypeEvent = {
  findElement: () => HTMLElement;
  stop: () => void;
}

const onevent = function(elements: HTMLElement[], eventNames: string[], match: string | null, handler: OnEventHandler) {
  let element, eventHandlers, eventName, i, j, len, len1, wrapped;
  if (handler == null) {
    throw new Error("No event handler was provided.");
  }
  wrapped = function(prototypeEvent: PrototypeEvent) {
    let elementWrapper, eventWrapper, result;
    elementWrapper = new PrototypeElementWrapper(prototypeEvent.findElement());
    // @ts-ignore
    eventWrapper = new PrototypeEventWrapper(prototypeEvent);
    // @ts-ignore
    result = prototypeEvent.stopped ? false : handler.call(elementWrapper, eventWrapper, eventWrapper.memo);
    if (result === false) {
      prototypeEvent.stop();
    }
  };
  eventHandlers = [];
  for (i = 0, len = elements.length; i < len; i++) {
    element = elements[i];
    for (j = 0, len1 = eventNames.length; j < len1; j++) {
      eventName = eventNames[j];
      // @ts-ignore
      eventHandlers.push(Event.on(element, eventName, match, wrapped));
    }
  }
  return function() {
    let eventHandler, k, len2, results;
    results = [];
    for (k = 0, len2 = eventHandlers.length; k < len2; k++) {
      eventHandler = eventHandlers[k];
      results.push(eventHandler.stop());
    }
    return results;
  };
};

class PrototypeElementWrapper implements ElementWrapper {
  readonly element: HTMLElement;

  constructor(element1: HTMLElement) {
    this.element = element1;
  }

  toStringn() {
    let markup;
    markup = this.element.outerHTML;
    return "ElementWrapper[" + (markup.substring(0, (markup.indexOf(">")) + 1)) + "]";
  };

  hide() {
    // @ts-ignore
    this.element.hide();
    return this;
  };

  show() {
    // @ts-ignore
    this.element.show();
    return this;
  };

  css = function(name: string, value?: string) {
    if (arguments.length === 1) {
      // @ts-ignore
      return this.element.getStyle(name);
    }
    // @ts-ignore
    this.element.setStyle({
      name: value
    });
    // @ts-ignore
    return this;
  };

  offset() {
    // @ts-ignore
    return this.element.viewportOffset();
  };

  remove() {
    // @ts-ignore
    this.element.remove();
    return this;
  };

  attr(name: string, value?: string) {
    let attributeName, current;
    if (_.isObject(name)) {
      // @ts-ignore
      for (attributeName in name) {
        value = name[attributeName];
        this.attr(attributeName, value);
      }
      return this;
    }
    // @ts-ignore
    current = this.element.readAttribute(name);
    if (arguments.length > 1) {
      // @ts-ignore
      this.element.writeAttribute(name, value === void 0 ? null : value);
    }
    return current;
  };

  // @ts-ignore
  focus() {
    this.element.focus();
    return this;
  };

  hasClass(name: string) {
    // @ts-ignore
    return this.element.hasClassName(name);
  };

  removeClass(name: string) {
    // @ts-ignore
    this.element.removeClassName(name);
    return this;
  };

  addClass(name: string) {
    // @ts-ignore
    this.element.addClassName(name);
    return this;
  };

  update(content: AddableContent) {
    // @ts-ignore
    this.element.update(content && convertContent(content));
    return this;
  };

  append(content: AddableContent) {
    // @ts-ignore
    this.element.insert({
      // @ts-ignore
      bottom: convertContent(content)
    });
    return this;
  };

  prepend(content: AddableContent) {
    // @ts-ignore
    this.element.insert({
      // @ts-ignore
      top: convertContent(content)
    });
    return this;
  };

  insertBefore(content: AddableContent) {
    // @ts-ignore
    this.element.insert({
      // @ts-ignore
      before: convertContent(content)
    });
    return this;
  };

  insertAfter(content: AddableContent) {
    // @ts-ignore
    this.element.insert({
      // @ts-ignore
      after: convertContent(content)
    });
    return this;
  };

  findFirst(selector: string) {
    let match;
    // @ts-ignore
    match = this.element.down(selector);
    if (match) {
      return new PrototypeElementWrapper(match);
    } else {
      return null;
    }
  };

  find(selector: string) {
    let e, i, len, matches, results;
    // @ts-ignore
    matches = this.element.select(selector);
    results = [];
    for (i = 0, len = matches.length; i < len; i++) {
      e = matches[i];
      results.push(new PrototypeElementWrapper(e));
    }
    return results;
  };

  findParent(selector: string) {
    let parent;
    // @ts-ignore
    parent = this.element.up(selector);
    if (!parent) {
      return null;
    }
    return new PrototypeElementWrapper(parent);
  };
  
  closest(selector: string) {
    // @ts-ignore
    if (this.element.match(selector)) {
      return this;
    }
    return this.findParent(selector);
  };

  parent() {
    let parent;
    parent = this.element.parentNode;
    if (!parent) {
      return null;
    }
    // @ts-ignore
    return new PrototypeElementWrapper(parent);
  };

  children() {
    let e, i, len, ref, results;
    // @ts-ignore
    ref = this.element.childElements();
    results = [];
    for (i = 0, len = ref.length; i < len; i++) {
      e = ref[i];      
      results.push(new PrototypeElementWrapper(e));
    }
    return results;
  };

  visible() {
    // @ts-ignore
    return this.element.visible();
  };

  deepVisible() {
    let element;
    ({
      element
    } = this);
    return element.offsetWidth > 0 && element.offsetHeight > 0;
  };

  trigger(eventName: string, memo: any) {
    let event;
    if (eventName == null) {
      throw new Error("Attempt to trigger event with null event name");
    }
    if (!((_.isNull(memo)) || (_.isObject(memo)) || (_.isUndefined(memo)))) {
      throw new Error("Event memo may be null or an object, but not a simple type.");
    }
    if ((eventName.indexOf(':')) > 0) {
      // @ts-ignore
      event = this.element.fire(eventName, memo);
      return !event.defaultPrevented;
    }
    if (memo) {
      throw new Error("Memo must be null when triggering a native event");
    }
    // @ts-ignore
    if (!(Prototype.Browser.WebKit && eventName === 'submit' && this.element instanceof HTMLFormElement)) {
      return fireNativeEvent(this.element, eventName);
    } else {
      return this.element.requestSubmit();
    }
  };

  value(newValue?: string) {
    let current;
    // @ts-ignore
    current = this.element.getValue();
    if (arguments.length > 0) {
      // @ts-ignore
      this.element.setValue(newValue);
    }
    return current;
  };

  checked() {
    // @ts-ignore
    return this.element.checked;
  };

  meta(name: string, value?: string) {
    let current;
    // @ts-ignore
    current = this.element.retrieve(name);
    if (arguments.length > 1) {
      // @ts-ignore
      this.element.store(name, value);
    }
    return current;
  };

  on(events: string, match: string | null, handler: OnEventHandler): ElementWrapper {
    // @ts-ignore
    on(this.element, events, match, handler);
    return this;
  };

  text() {
    return this.element.textContent || this.element.innerText;
  };

};

class PrototypeRequestWrapper {
  readonly req: Request;
  constructor(req: Request) {
    this.req = req;
  }

  abort() {
    throw "Cannot abort Ajax request when using Prototype.";
  };

};

class PrototypeResponseWrapper {
  readonly res: Response;
  readonly status: number;
  readonly statusText: string;
  readonly json: any;
  readonly text: any;
  constructor(res: Response) {
    this.res = res;
    this.status = this.res.status;
    this.statusText = this.res.statusText;
    // @ts-ignore
    this.json = this.res.responseJSON;
    // @ts-ignore
    this.text = this.res.responseText;
  }

  header(name: string) {
    // @ts-ignore
    return this.res.getHeader(name);
  };

};

const body = new PrototypeElementWrapper(document.body);

let activeAjaxCount = 0;
const adjustAjaxCount = function(delta: number) {
  activeAjaxCount += delta;
  return body.attr("data-ajax-active", String(activeAjaxCount));
};

const ajaxRequest = function(url: string, options: AjaxRequestOptions | undefined) {
  let finalOptions: AjaxRequestOptions;
  if (options == null) {
    options = {};
  }
  finalOptions = {
    method: options.method || "post",
    contentType: options.contentType || "application/x-www-form-urlencoded",
    // @ts-ignore
    parameters: options.data,
    requestHeaders: options.headers,

    onException(ajaxRequest: any, exception: any) {
      adjustAjaxCount(-1);
      if (options.exception) {
        options.exception(exception);
      } else {
        throw exception;
      }
    },

    onFailure(response: Response) {
      let message, text;
      adjustAjaxCount(-1);
      // @ts-ignore
      message = "Request to " + url + " failed with status " + (response.getStatus());
      // @ts-ignore
      text = response.getStatusText();
      if (!_.isEmpty(text)) {
        message += " -- " + text;
      }
      message += ".";
      if (options.failure) {
        options.failure(new PrototypeResponseWrapper(response), message);
      } else {
        throw new Error(message);
      }
    },

    onSuccess(response: Response) {
      adjustAjaxCount(-1);
      // @ts-ignore
      if ((!response.getStatus()) || (!response.request.success())) {
        // @ts-ignore
        finalOptions.onFailure(new PrototypeResponseWrapper(response));
        return;
      }
      options.success && options.success(new PrototypeResponseWrapper(response));
    }
  };
  adjustAjaxCount(+1);
  // @ts-ignore
  return new PrototypeRequestWrapper(new Ajax.Request(url, finalOptions));
};

type Scanner = (element: ElementWrapper) => void;

let scanners: Scanner[] = [];

let scanner = function(selector: string, callback: (e: ElementWrapper) => void) {
  var scan;
  scan = function(root: ElementWrapper) {
    var el, j, len, ref;
    ref = root.find(selector);
    for (j = 0, len = ref.length; j < len; j++) {
      el = ref[j];
      callback(el);
    }
  };
  scan(body!);
  if (scanners === null) {
    scanners = [];
    body!.on(events.initializeComponents, null, function() {
      var f, j, len;
      for (j = 0, len = scanners.length; j < len; j++) {
        f = scanners[j];
        f(body!);
      }
    });
  }
  scanners.push(scan);
};

const wrapElement = function(element: string | HTMLElement) {
  if (_.isString(element)) {
    // @ts-ignore
    element = $(element);
    if (!element) {
      return null;
    }
  } else {
    if (!element) {
      throw new Error("Attempt to wrap a null DOM element");
    }
  }
  // @ts-ignore
  return new PrototypeElementWrapper(element);
};

const createElement = function(elementName: string, attributes?: object, body?: AddableContent): ElementWrapper {
  let element;
  if (_.isObject(elementName)) {
    // @ts-ignore
    body = attributes;
    attributes = elementName;
    // @ts-ignore
    elementName = null;
  }
  if (_.isString(attributes)) {
    body = attributes;
    // @ts-ignore
    attributes = null;
  }
  element = wrapElement(document.createElement(elementName || "div"));
  if (attributes) {
    // @ts-ignore
    element.attr(attributes);
  }
  if (body) {
    // @ts-ignore
    element.update(body);
  }

  return element!;
};


const getDataAttributeAsObject = function(element: HTMLElement, attribute: string) {
  let value;
  // @ts-ignore
  value = $(element).readAttribute('data-' + attribute);
  if (value !== null) {
    return value = JSON.parse(value);
  } else {
    return value = {};
  }
};

const getEventUrl = function(eventName: string, element?: HTMLElement): string {
  let data, ref, ref1, url;
  if (!(eventName != null)) {
    throw 'dom.getEventUrl: the eventName parameter cannot be null';
  }
  if (!_.isString(eventName)) {
    throw 'dom.getEventUrl: the eventName parameter should be a string';
  }
  eventName = eventName.toLowerCase();
  if (element === null) {
    element = document.body;
  } else if (element instanceof PrototypeElementWrapper) {
    ({
      element
    } = element);
    // @ts-ignore
  } else if (element.jquery != null) {
    // @ts-ignore
    element = element[0];
  }
  url = null;
  // @ts-ignore
  while ((url == null) && (element.previousElementSibling != null)) {
    // @ts-ignore
    data = getDataAttributeAsObject(element, 'component-events');
    url = data != null ? (ref = data[eventName]) != null ? ref.url : void 0 : void 0;
    // @ts-ignore
    element = element.previousElementSibling;
  }
  if (url == null) {
    // @ts-ignore
    while ((url == null) && (element.parentElement != null)) {
      // @ts-ignore
      data = getDataAttributeAsObject(element, 'component-events');
      url = data != null ? (ref1 = data[eventName]) != null ? ref1.url : void 0 : void 0;
      // @ts-ignore
      element = element.parentElement;
    }
  }
  return url;
};

const onDocument = function(events: string, match: string | null, handler: OnEventHandler) {
  // @ts-ignore
  return on(document, events, match, handler);
};

// A bit of a hack to export a function that also provides properties defined in an interface.
// Inspired by https://stackoverflow.com/a/48675307 (yay for Thiagos! hehehe)
const dom = wrapElement as DOM;
dom.getEventUrl = getEventUrl;
dom.wrap = wrapElement;
dom.create = createElement;
dom.ajaxRequest = ajaxRequest;
dom.on = on;
dom.onDocument = onDocument;
dom.body = body!;
dom.scanner = scanner;

const exports_: DOM = dom;

export default exports_;