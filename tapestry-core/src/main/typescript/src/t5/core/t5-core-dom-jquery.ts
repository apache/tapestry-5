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
 * jQuery implementation of Tapestry's DOM wrappers.
 */

import type { ElementWrapper, EventWrapper, RequestWrapper, ResponseWrapper, ElementOffset, DOM, AddableContent, OnEventHandler, AjaxRequestOptions } from "t5/core/types";
import _ from "underscore";
import "t5/core/utils";
import events from "t5/core/events";
import $ from "jquery";

const convertContent = function(content: AddableContent) {
  if (_.isString(content)) {
    return content;
  }
  if (_.isElement(content)) {
    return content;
  }
  if (content instanceof JQueryElementWrapper) {
    return content.$;
  }
  throw new Error("Provided value <" + content + "> is not valid as DOM element content.");
};

class JQueryEventWrapper implements EventWrapper {
  readonly nativeEvent: BaseJQueryEventObject;
  readonly memo: any;
  readonly type: string;
  readonly char?: string;
  readonly key?: string;
  constructor(event: BaseJQueryEventObject, memo: any) {
    this.nativeEvent = event;
    this.memo = memo;
    this.type = event.type;
    const o = event as any;
    this.char = o['char'];
    this.key = o['key'];
  }

  stop() {
    this.nativeEvent.preventDefault();
    return this.nativeEvent.stopImmediatePropagation();
  };

};

const onevent = function(jqueryObject: JQuery, eventNames: string, match: string | null, handler: OnEventHandler) {
  
  if (handler == null) {
    throw new Error("No event handler was provided.");
  }
  const wrapped = function(jqueryEvent: BaseJQueryEventObject, memo: any) {
    var elementWrapper, eventWrapper, result;
    var target = (jqueryEvent as any).target;
    elementWrapper = new JQueryElementWrapper($((jqueryEvent as any).target));
    eventWrapper = new JQueryEventWrapper(jqueryEvent, memo);
    // @ts-ignore
    result = handler.call(elementWrapper, eventWrapper, memo);
    if (result === false) {
      eventWrapper.stop();
    }
  };
  jqueryObject.on(eventNames, match, wrapped);
  return function() {
    // @ts-ignore
    return jqueryObject.off(eventNames, match, wrapped);
  };
};

class JQueryElementWrapper implements ElementWrapper {

  readonly $: JQuery;
  readonly element: HTMLElement;

  constructor(query: JQuery) {
    this.$ = query;
    this.element = query[0];
  }

  toString() {
    var markup;
    markup = this.element.outerHTML;
    return "ElementWrapper[" + (markup.substring(0, (markup.indexOf(">")) + 1)) + "]";
  };

  hide(): ElementWrapper {
    this.$.hide();
    return this;
  };

  show(): ElementWrapper {
    this.$.show();
    return this;
  };

  css(name: string, value: string | undefined): ElementWrapper | string {
    if (arguments.length === 1) {
      return this.$.css(name);
    }
    this.$.css(name, value as string);
    return this;
  };

  offset(): ElementOffset {
    return this.$.offset()!;
  };

  remove(): ElementWrapper {
    this.$.detach();
    return this;
  };

  attr(name: string, value?: string | boolean | number): ElementWrapper | string | null {
    var attributeName, current;
    if (_.isObject(name) && name != null) {
      const nameAsObject = name as object;
      for (attributeName in nameAsObject) {
        value = name[attributeName];
        this.attr(attributeName, value);
      }
      return this;
    }
    current = this.$.attr(name);
    if (arguments.length > 1) {
      if (value === null) {
        this.$.removeAttr(name);
      } else {
        this.$.attr(name, _.isString(value) ? value : String(value));
      }
    }
    if (_.isUndefined(current)) {
      current = null;
    }
    return current;
  };

  focus(): ElementWrapper {
    this.$.focus();
    return this;
  };

  hasClass(name: string): boolean {
    return this.$.hasClass(name);
  };

  removeClass(name: string): ElementWrapper {
    this.$.removeClass(name);
    return this;
  };

  addClass(name: string): ElementWrapper {
    this.$.addClass(name);
    return this;
  };

  update(content: AddableContent) {
    this.$.empty();
    if (content) {
      this.$.append(convertContent(content));
    }
    return this;
  };

  append(content: AddableContent) {
    this.$.append(convertContent(content));
    return this;
  };

  prepend(content: AddableContent) {
    this.$.prepend(convertContent(content));
    return this;
  };

  insertBefore(content: AddableContent) {
    this.$.before(convertContent(content));
    return this;
  };

  insertAfter(content: AddableContent) {
    this.$.after(convertContent(content));
    return this;
  };

  findFirst(selector: string): ElementWrapper | null {
    var match;
    match = this.$.find(selector);
    if (match.length) {
      return new JQueryElementWrapper(match.first());
    } else {
      return null;
    }
  };

  find(selector: string): ElementWrapper[] {
    var i, j, matches, ref, results;
    matches = this.$.find(selector);
    results = [];
    for (i = j = 0, ref = matches.length; 0 <= ref ? j < ref : j > ref; i = 0 <= ref ? ++j : --j) {
      results.push(new JQueryElementWrapper(matches.eq(i)));
    }
    return results;
  };

  findParent(selector: string): ElementWrapper | null {
    var parents;
    parents = this.$.parents(selector);
    if (!parents.length) {
      return null;
    }
    return new JQueryElementWrapper(parents.eq(0));
  };

  closest(selector: string): ElementWrapper | null {
    var match;
    match = this.$.closest(selector);
    switch (false) {
      case match.length !== 0:
        return null;
      case match[0] !== this.element:
        return this;
      default:
        return new JQueryElementWrapper(match);
    }
  };

  parent() : ElementWrapper | null  {
    var parent;
    parent = this.$.parent();
    if (!parent.length) {
      return null;
    }
    return new JQueryElementWrapper(parent);
  };

  children(): ElementWrapper[]  {
    var children, i, j, ref, results;
    children = this.$.children();
    results = [];
    for (i = j = 0, ref = children.length; 0 <= ref ? j < ref : j > ref; i = 0 <= ref ? ++j : --j) {
      results.push(new JQueryElementWrapper(children.eq(i)));
    }
    return results;
  };

  visible(): boolean  {
    return this.$.css("display") !== "none";
  };

  deepVisible(): boolean  {
    var element;
    element = this.element;
    return element.offsetWidth > 0 && element.offsetHeight > 0;
  };

  trigger(eventName: string, memo?: any) {
    var jqEvent;
    if (eventName == null) {
      throw new Error("Attempt to trigger event with null event name");
    }
    if (!((_.isNull(memo)) || (_.isObject(memo)) || (_.isUndefined(memo)))) {
      throw new Error("Event memo may be null or an object, but not a simple type.");
    }
    jqEvent = $.Event(eventName);

    // @ts-ignore
    this.$.trigger(jqEvent, memo);
    return !jqEvent.isImmediatePropagationStopped();
  };

  value(newValue?: string) {
    var current;
    current = this.$.val();
    if (arguments.length > 0) {
      this.$.val(newValue!);
    }
    return current;
  };

  checked(): boolean {
    // @ts-ignore
    return this.element.checked;
  };

  meta(name: string, value?: string | null | boolean) {
    var current;
    current = this.$.data(name);
    if (arguments.length > 1) {
      this.$.data(name, value);
    }
    return current;
  };

  on(events: string, match: string | null, handler: OnEventHandler): ElementWrapper {
    on(this.element, events, match, handler);
    return this;
  };

  text(): string {
    return this.$.text();
  };

};

const newElementWrapper = function(element: JQuery) {
  return new JQueryElementWrapper(element);
}

class JQueryRequestWrapper implements RequestWrapper {
  readonly jqxhr: JQueryXHR;

  constructor(jqxhr1: JQueryXHR) {
    this.jqxhr = jqxhr1;
  }

  abort() {
    return this.jqxhr.abort();
  };

}

class JQueryResponseWrapper implements ResponseWrapper {
  readonly jqxhr: JQueryXHR;
  readonly status: number;
  readonly statusText: string;
  readonly options: JQueryAjaxSettings;
  readonly text: string;
  readonly json: any | null;

  constructor(jqxhr1: JQueryXHR, data: JQueryAjaxSettings) {
    this.jqxhr = jqxhr1;
    this.status = this.jqxhr.status;
    this.statusText = this.jqxhr.statusText;
    this.options = data;
    this.text = this.jqxhr.responseText;
    this.json = this.jqxhr.responseJSON;
  }

  header(name: string) {
    return this.jqxhr.getResponseHeader(name);
  };

}

const wrapElement = function(element: HTMLElement | string): ElementWrapper | null {
  let e: Element | null;
  if (_.isString(element)) {
    e = document.getElementById(element);
    if (e == null) {
      return null;
    }
  } else {
    e = element;
    if (!e) {
      throw new Error("Attempt to wrap a null DOM element");
    }
  }
  return newElementWrapper($(e));
};

const body = wrapElement(document.body);

var activeAjaxCount = 0;
const adjustAjaxCount = function(delta: number) {
  activeAjaxCount += delta;
  return body!.attr("data-ajax-active", String(activeAjaxCount));
};

let ajaxRequest = function(url: string, options?: AjaxRequestOptions) {
  var jqxhr, ref;
  if (options == null) {
    options = {};
  }
  jqxhr = $.ajax({
    url: url,
    type: ((ref = options.method) != null ? ref.toUpperCase() : void 0) || "POST",
    contentType: options.contentType,
    traditional: true,
    data: options.data,
    headers: options.headers || {},
    error: function(jqXHR, textStatus, errorThrown) {
      var message, text;
      adjustAjaxCount(-1);
      if (textStatus === "abort") {
        return;
      }
      message = "Request to " + url + " failed with status " + textStatus;
      text = jqXHR.statusText;
      if (!_.isEmpty(text)) {
        message += " -- " + text;
      }
      message += ".";
      if (options.failure) {
        options.failure(new JQueryResponseWrapper(jqXHR, {}), message);
      } else {
        throw new Error(message);
      }
    },
    success: function(data, textStatus, jqXHR) {
      adjustAjaxCount(-1);
      options.success && options.success(new JQueryResponseWrapper(jqXHR, data));
    }
  });
  adjustAjaxCount(+1);
  return new JQueryRequestWrapper(jqxhr);
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

const createElement = function(elementName: string, attributes?: object, body?: AddableContent): ElementWrapper {
  var element: ElementWrapper;
  if (_.isObject(elementName)) {
    // @ts-ignore
    body = attributes;
    attributes = elementName;
    // @ts-ignore    
    elementName = null;
  }
  if (_.isString(attributes)) {
    // @ts-ignore
    body = attributes;
    // @ts-ignore
    attributes = null;
  }
  // @ts-ignore
  element = wrapElement(document.createElement(elementName));
  if (attributes) {
    // @ts-ignore
    element.attr(attributes);
  }
  if (body) {
    element.update(body);
  }
  return element;
};

const getDataAttributeAsObject = function(element: HTMLElement, attribute: string) {
  var value;
  return value = $(element).data(attribute);
};

const getEventUrl = function(eventName: string, element?: HTMLElement): string {
  var data, ref, ref1, url;
  if (!(eventName != null)) {
    throw 'dom.getEventUrl: the eventName parameter cannot be null';
  }
  if (!_.isString(eventName)) {
    throw 'dom.getEventUrl: the eventName parameter should be a string';
  }
  eventName = eventName.toLowerCase();
  if (element === null) {
    element = document.body;
  } else if (element instanceof JQueryElementWrapper) {
    element = element.element;
  } 
  // @ts-ignore
  else if (element.jquery != null) {
    // @ts-ignore
    element = element[0];
  }
  url = null;
  while ((url == null) && (element!.previousElementSibling != null)) {
    data = getDataAttributeAsObject(element!, 'component-events');
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

const on = function(selector: string | HTMLElement | Document | HTMLElement[], events: string, match: string | null, handler: OnEventHandler) {
  var elements;
  if (handler == null) {
    // @ts-ignore
    handler = match;
    match = null;
  }
  elements = $(selector);
  return onevent(elements, events, match, handler);
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
