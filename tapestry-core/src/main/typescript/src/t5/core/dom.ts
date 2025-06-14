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
 * Stub implemantation of t5/core/dom.js just to allow this project to compile.
 * The actual implementation, t5/core/dom-jquery.js or t5/core/dom-prototype.js
 * will be chosen by Tapestry at runtime according to configuration.
 * @packageDocumentation
 */
import type { ElementWrapper, EventWrapper, RequestWrapper, ResponseWrapper, DOM, AjaxRequestOptions, OnEventHandler, AddableContent, ElementOffset } from "t5/core/types";

const f = function(element: HTMLElement | string): ElementWrapper | null {
  throw new Error("Function not implemented");
}

const dom = f as DOM;

// @ts-ignore
const d: DOM = {

  getEventUrl: function (eventName: string, element?: HTMLElement): string | null {
    throw new Error("Function not implemented.");
  },
  wrap: function (element: HTMLElement | string): ElementWrapper | null {
    throw new Error("Function not implemented.");
  },
  create: function (elementName: string, attributes?: object, body?: AddableContent): ElementWrapper {
    throw new Error("Function not implemented.");
  },
  ajaxRequest: function (url: string, options?: AjaxRequestOptions): RequestWrapper {
    throw new Error("Function not implemented.");
  },
  onDocument: function (events: string, match: string | null, handler: OnEventHandler): () => any {
    throw new Error("Function not implemented.");
  },
  body: {
    element: document.createElement('nothing'),
    hide: function (): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    show: function (): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    css: function (name: string, property: string | undefined): ElementWrapper | string {
      throw new Error("Function not implemented.");
    },
    offset: function (): ElementOffset {
      throw new Error("Function not implemented.");
    },
    remove: function (): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    attr: function (name: string | [key: string, value: string], value?: string | boolean | number): ElementWrapper | string | null {
      throw new Error("Function not implemented.");
    },
    focus: function (): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    hasClass: function (name: string): boolean {
      throw new Error("Function not implemented.");
    },
    removeClass: function (name: string): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    addClass: function (name: string): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    update: function (content: AddableContent): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    append: function (content: AddableContent): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    prepend: function (content: AddableContent): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    insertBefore: function (content: AddableContent): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    insertAfter: function (content: AddableContent): ElementWrapper {
      throw new Error("Function not implemented.");
    },
    find: function (selector: string): ElementWrapper[] {
      throw new Error("Function not implemented.");
    },
    findFirst: function (selector: string): ElementWrapper | null {
      throw new Error("Function not implemented.");
    },
    findParent: function (selector: string): ElementWrapper | null {
      throw new Error("Function not implemented.");
    },
    closest: function (selector: string): ElementWrapper | null {
      throw new Error("Function not implemented.");
    },
    parent: function (): ElementWrapper | null {
      throw new Error("Function not implemented.");
    },
    children: function (): ElementWrapper[] {
      throw new Error("Function not implemented.");
    },
    visible: function (): boolean {
      throw new Error("Function not implemented.");
    },
    deepVisible: function (): boolean {
      throw new Error("Function not implemented.");
    },
    trigger: function (eventName: string, memo: Object | null): void {
      throw new Error("Function not implemented.");
    },
    value: function (newValue?: string | null): String | null {
      throw new Error("Function not implemented.");
    },
    checked: function (): boolean {
      throw new Error("Function not implemented.");
    },
    on: function (events: string, match: string | null, handler: OnEventHandler) {
      throw new Error("Function not implemented.");
    },
    text: function (): string {
      throw new Error("Function not implemented.");
    },
    meta: function (name: string, value?: string): string | null {
      throw new Error("Function not implemented.");
    }
  },
  scanner: function (selector: string, callback: (e: ElementWrapper) => void): void {
    throw new Error("Function not implemented.");
  },
  on: function (selector: string | HTMLElement | Document | HTMLElement[], events: string, match: string | null, handler: OnEventHandler): () => any {
    throw new Error("Function not implemented.");
  }
};

dom.getEventUrl = d.getEventUrl;
dom.wrap = d.wrap;
dom.create = d.create;
dom.ajaxRequest = d.ajaxRequest;
dom.on = d.on;
dom.onDocument = d.onDocument;
dom.body = d.body!;
dom.scanner = d.scanner;

export default dom;