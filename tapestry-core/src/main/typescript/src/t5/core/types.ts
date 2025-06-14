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
 * Module defining some types used specially on `t5/core/dom` and `t5/core/zone`, among others.
 * @packageDocumentation
  */

/**
 * Superinterface of event wrapper types. Exists just to keep the compiler happy.
 * @internal
 */
export interface IEventWrapper {
}

/**
 * Type of event handlers functions.
 */
export type OnEventHandler = (event: EventWrapper, memo: any) => any;

/**
 * Class defining the offset of an element.
 */
export interface ElementOffset {
  top: Number;
  left: Number;
}

/**
 * Type defining the types of content
 */
export type AddableContent = string | ElementWrapper | HTMLElement | null;

/**
 * Type defining the options used in an AJAX request.
 */
export interface AjaxRequestOptions {

  /**
   * HTTP method to be used ("post", "get", etc.). Default: "post".
   */
  method?: string; 

  /**
   * Request content type. Defaults to "application/x-www-form-urlencoded".
   */
  contentType?: string;

  /**
   * Additional key/value pairs (for the default content type). Optional.
   */
  data?: [key: string, value: string];

  /**
   * Additional key/value pairs to be added to the request headers. Optional.
   */
  headers?: [key: string, value: string];
  
  /**
   * Handler to invoke on success. Passed the ResponseWrapper object. Default: does nothing.
   */
  success?: (r: ResponseWrapper) => void;

  /**
   * Handler to invoke on failure (server responds with a non-2xx code).
   * Passed the response. Default will throw the exception.
   */
  failure?: (r: ResponseWrapper, message?: string) => void;

  /**
   * Handler to invoke when an exception occurs (often means the server is unavailable).
   *  Passed the exception. Default will generate an exception message and throw an `Error`.
   */
  exception?: (e: any) => void;
}

/**
 * Wraps a DOM element, providing some common behaviors.
 * Exposes the DOM element as property `element`.
 */
export interface ElementWrapper {

  readonly element: HTMLElement;
  
  /**
   * Hides the wrapped element, setting its display to 'none'.
   * 
   * @returns <code>this</code>.
   */
  hide(): ElementWrapper;
  
  /**
   * Displays teh wrapped element if hidden.
   * 
   * @returns <code>this</code>.
   */
  show(): ElementWrapper;
  
  /**
   * Gets or sets a CSS property.
   * 
   * @param {string} name name the name of the property.
   * @param {string} value the value of the property.
   * @returns <code>this</code>.
   */
  css(name: string, value: string | undefined): ElementWrapper | string;
  
  /**
   * Returns the offset of the object relative to the document. The returned object has
   * keys <code>top</code> and <code>left</code>'.
   */
  offset(): ElementOffset;
  
  /**
   * Removes the wrapped element from the DOM. It can later be re-attached.
   * 
   * @returns <code>this</code>.
   */
  remove(): ElementWrapper;
  
  /**
   * Reads or updates an attribute. With one argument, returns the current value
   * of the attribute. With two arguments, updates the attribute's value, and returns
   * the previous value. Setting an attribute to null is the same as removing it.
   *
   * Alternately, the first attribute can be an object in which case all the keys
   * and values of the object are applied as attributes, and this `ElementWrapper` is returned.
   *
   * @param {string | [key: string, value: string]} name the attribute to read or update, or an object of keys and values
   * @param {string | boolean | number} value (optional) the new value for the attribute  
   */
  attr(name: string | [key: string, value: string], value?: string | boolean | number | null): ElementWrapper | string | null;
  
  /**
   * Moves the cursor to the field.
   */
  focus(): ElementWrapper;
  
  /**
   * Returns true if the element has the indicated class name, false otherwise.
   * 
   * @param {string} name the class name
   * @returns {boolean} <code>true</code> of <code>false</code>
   */
  hasClass(name: string): boolean;

  /**
   * Removes the class name from the element.
   * 
   * @param {string} name the class name
   * @returns <code>this</code>.
   */
  removeClass(name: string): ElementWrapper;

  /**
   * Adds the class name to the element.
   * 
   * @param {string} name the class name
   * @returns <code>this</code>.
   */
  addClass(name: string) : ElementWrapper;
  
  /**
   * Updates this element with new content, replacing any old content. The new content may be HTML text, or a DOM
   * element, or an ElementWrapper, or null (to remove the body of the element).
   * 
   * @param {AddableContent} content the content to be added
   * @returns <code>this</code>.
   */
  update(content: AddableContent) : ElementWrapper;
  
  /**
   * Appends new content (Element, ElementWrapper, or HTML markup string) to the body of the element.
   * 
   * @param {AddableContent} content the content to be added
   * @returns <code>this</code>.
   */
  append(content: AddableContent): ElementWrapper;

  /**
   * Prepends new content (Element, ElementWrapper, or HTML markup string) to the body of the element.
   * 
   * @param {AddableContent} content the content to be added
   * @returns <code>this</code>.
   */
  prepend(content: AddableContent): ElementWrapper;
  
  /**
   * Inserts new content (Element, ElementWrapper, or HTML markup string) into the DOM immediately before
   * this ElementWrapper's element.
   * 
   * @param {AddableContent} content the content to be added
   * @returns <code>this</code>.
   */
  insertBefore(content: AddableContent): ElementWrapper;
  
  /**
   * Inserts new content (Element, ElementWrapper, or HTML markup string) into the DOM immediately before
   * this ElementWrapper's element.
   * 
   * @param {AddableContent} content the content to be added
   * @returns <code>this</code>.
   */
  insertAfter(content: AddableContent): ElementWrapper;

  /**
   * Finds all child elements matching the CSS selector, returning them
   * as an array of ElementWrappers.
   * @param {string} selector a CSS selector.
   * @returns {ElementWrapper[]} the matching child elements as an array of ElementWrapper instances
   */
  find(selector: string): ElementWrapper[];
  
  /**
   * Finds the first child element that matches the CSS selector, wrapped as an ElementWrapper.
   * Returns null if not found.  
   *
   * @param {string} selector a CSS selector
   */
  findFirst(selector: string): ElementWrapper | null;
  
  /**
   * Finds the first container element that matches the CSS selector, wrapped as an ElementWrapper.
   * Returns null if not found.  
   *
   * @param {string} selector a CSS selector
   * @returns {ElementWrapper | null} an element wrapper or null.   * 
   */
  findParent(selector: string): ElementWrapper | null ;
  
  /**
   * Returns this ElementWrapper if it matches the selector; otherwise, returns the first container element (as an ElementWrapper)
   * that matches the selector. Returns null if no container element matches.
   * 
   * @param {string} selector a CSS selector
   * @returns {ElementWrapper | null} an element wrapper or null.
   */
  closest(selector: string): ElementWrapper | null ;
  
  /**
   * Returns an ElementWrapper for this element's containing element.
   * Returns null if this element has no parent (either because this element is the document object, or
   * because this element is not yet attached to the DOM).
   * 
   * @returns {ElementWrapper | null} an element wrapper or null.
   */
  parent(): ElementWrapper | null;
  
  /**
   * Returns an array of all the immediate child elements of this element, as ElementWrappers.
   * 
   * @returns {ElementWrapper[]} the children as ElementWrapper[]
   */
  children(): ElementWrapper[];
  
  /**
   * Returns true if this element is visible, false otherwise. This does not check to see if all containers of the
   * element are visible.
   * 
   * @returns {boolean} <code>true</code> or <code>false</code>
   */
  visible(): boolean;
  
  /**
   * Returns true if this element is visible, and all parent elements are also visible, up to the document body.
   * 
   * @returns {boolean} <code>true</code> or <code>false</code>
   */
  deepVisible(): boolean;
  
  /**
   * Fires a named event, passing an optional _memo_ object to event handler functions. This must support
   * common native events (exact list TBD), as well as custom events (in Prototype, custom events must have
   * a prefix that ends with a colon).
   *
   * @param {string} eventName name of event to trigger on the wrapped Element
   * @param {any | null} memo optional value assocated with the event; available as WrappedeEvent.memo in event handler functions (must
   * be null for native events). The memo, when provided, should be an object; it is an error if it is a string or other
   * non-object type..
   * @returns {boolean} true if the event fully executed, or false if the event was canceled.
   */
  trigger(eventName: string, memo?: any): void;
  
  /**
   * With no parameters, returns the current value of the element (which must be a form control element, such as `<input>` or
   * `<textarea>`). With one parameter, updates the field's value, and returns the previous value. The underlying
   * foundation is responsible for mapping this correctly based on the type of control element.
   * TODO: Define behavior for multi-named elements, such as `<select>`.
   *
   * @param {string | null} newValue (optional) new value for field
   */
  value(newValue?: string | null): String | null;
  
  /**
   * Returns true if element is a checkbox and is checked.
   * 
   * @returns {boolean} <code>true</code> of <code>false</code>
   */
  checked(): boolean;

  /**
   * Stores or retrieves meta-data on the element. With one parameter, the current value for the name
   * is returned (or undefined). With two parameters, the meta-data is updated and the previous value returned.
   * For Prototype, the meta data is essentially empty (except, perhaps, for some internal keys used to store
   * event handling information).  For jQuery, the meta data may be initialized from data- attributes.
   *
   * @param {string} name name of meta-data value to store or retrieve
   * @param {string?} value (optional) new value for meta-data
   */
  meta(name: string, value?: string | null | boolean): string | null | boolean;

  /**
   * Adds an event handler for one or more events.
   * 
   * @param {string} events - one or more event names, separated by spaces
   * @param {string | null} match - optional: CSS expression used as a filter; only events that bubble
   *   up to the wrapped element from an originating element that matches the CSS expression
   *   will invoke the handler.
   * @param {(element: ElementWrapper) => void} handler - function invoked; the function is passed an `EventWrapper` object, and the
   *   context (`this`) is the `ElementWrapper` for the matched element.
   */
  on(events: string, match: string | null, handler: OnEventHandler): any;
  
  /**
   * Returns the text of the element and its children.
   * 
   * @returns {string} the element's text.
   */
  text(): string;

  /**
   * Returns a string describing this object.
   * 
   * @returns {string} a string. 
   */  
  toString(): string;
  
}

/** 
 * Generic view of an DOM event that is passed to a handler function.
 *
 *Properties:
 * nativeEvent - the native Event object, which may provide additional information.
 * memo - the object passed to `ElementWrapper.trigger()`.
 * type - the name of the event that was triggered.
 * char - the character value of the pressed key, if a printable character, as a string.
 * key -The key value of the pressed key. This is the same as the `char` property for printable keys,
 *  or a key name for others.
 */
export interface EventWrapper {

  /**
   * Wrapped event.
   */
  readonly nativeEvent: any;

  /**
   * Object passed to ElementWrapper.trigger().
   */
  readonly memo?: any;

  /**
   * Event type.
   */
  readonly type: string;

  /**
   * The character value of the pressed key, if a printable character, as a string.
   */
  readonly char?: string;

  /**
   * The key value of the pressed key. This is the same as the `char` property for printable keys,
   * or a key name for others.
   */
  readonly key?: string;

  /**
   * Interface between the dom's event model, and the one from the library (Prototype, jQuery, etc).
   *
   * Event handlers may return false to stop event propogation; this prevents an event from bubbling up, and
   * prevents any browser default behavior from triggering.  This is often easier than accepting the `EventWrapper`
   * object as the first parameter and invoking `stop()`.
   *
   * Returns a function of no parameters that removes any added handlers.   * 
   * 
   * @param {Type[]} object array of DOM elements (or the document object) for Prototype, jQuery object if jQuery.
   * @param {string[]} eventNames array of event names.
   * @param {string | null} match selector to match bubbled elements, or null.
   * @returns a function that removes the listeners.
   */
  //onevent: (object: T, eventNames: string[], match: string | null, handler: (e: EventWrapper<T>) => void) => () => void;

}

/**
 * Wrapper around AJAX request objects (for example, <code>jqXHR</code> for jQuery
 * or <code>Ajax.Request</code> for Prototype).
 */
export interface RequestWrapper {
	
  /**
   * Aborts the current AJAX request.
   */
  abort(): void;
}

/**
 * Wrapper around AJAX response objects (for example, <code>jqXHR</code> for jQuery
 * or <code>Ajax.Response</code> for Prototype).
 */
export interface ResponseWrapper {

  /**
   * Retrieves a response header by name.
   * 
   * @param {string} name the name of the header
   * @returns {string | null} the value of the header
   */
  header(name: String) : string | null

  /**
   * The response text, if present, as a JSON object.
   */
  readonly json: any | null;
		
}

/**
 * Defines the type of the exported object for t5/core/dom.js implementations.
 */
export interface DOM {

  /**
   * Function that wraps a DOM element as an ElementWrapper; additional functions are attached as
   * properties.
   *
   * @param {HTMLElement | string} element a DOM element, or a string id of a DOM element.
   * @returns the ElementWrapper, or null if no element with the id exists.
   */
  (element: HTMLElement | string): ElementWrapper | null;

  /**
   * Returns the URL of a component event based on its name and an optional element
   * or null if the event information is not found. When the element isn't passed
   * or it's null, the event data is taken from the <body> element.
   *
   * @param {string} eventName name of the component event.
   * @param {HTMLElement | null} element (object) HTML DOM element to be used as the beginning of the event data search. Optional.
   */
  getEventUrl: (eventName: string, element?: HTMLElement) => string | null;

  /**
   * Function that wraps a DOM element as an ElementWrapper; additional functions are attached as
   * properties.
   *
   * @param {HTMLElement | string} element a DOM element, or a string id of a DOM element.
   * @returns the ElementWrapper, or null if no element with the id exists.
   */
  wrap: (element: HTMLElement | string) => ElementWrapper | null;

  /**
   * Creates a new element, detached from the DOM.
   *
   * @param elementName {string} name of element to create, if ommitted, then "div"
   * @param attributes {object} attributes to apply to the created element (may be omitted)
   * @param body {AddableContent} content for the new element, may be omitted for no body
   */
  create: (elementName: string, attributes?: object, body?: AddableContent) => ElementWrapper;

  ajaxRequest: (url: string, options?: AjaxRequestOptions) => RequestWrapper;
  
  /**
   * Used to add an event handler to an element (possibly from elements below it in the hierarchy).
   * @param {string} selector CSS selector used to select elements to attach handler to; alternately,
   * a single DOM element, or an array of DOM elements. The document is considered an element
   * for these purposes.
   * @param {string} events one or more event names, separated by spaces
   * @param {string | null} match optional: CSS expression used as a filter; only events that bubble
   * up to a selected element from an originating element that matches the CSS expression
   * will invoke the handler.
   * @param handler function invoked; the function is passed an `EventWrapper` object, and the context (`this`)
   * is the `ElementWrapper` for the matched element.
   */
  on: (selector: string | HTMLElement | Document | HTMLElement[], events: string, match: string | null, handler: OnEventHandler) => () => any;

  /**
   * onDocument() is used to add an event handler to the document object; this is used
   * for global (or default) handlers.
   * @param {string} events one or more event names, separated by spaces
   * @param {string | null} match optional: CSS expression used as a filter; only events that bubble
   * up to a selected element from an originating element that matches the CSS expression
   * will invoke the handler.
   * @param handler function invoked; the function is passed an `EventWrapper` object, and the context (`this`)
   * is the `ElementWrapper` for the matched element.
   * @returns a function of no parameters that removes any added handlers.
   */
  onDocument: (events: string, match: string | null, handler: OnEventHandler) => () => any;

  /**
   * Sets up a scanner callback; this is used to perfom one-time setup of elements
   * that match a particular CSS selector. The callback is passed each element that
   * matches the selector. The callback is expected to modify the element so that it does not
   * match future selections caused by zone updates, typically by removing the CSS class or data- attribute
   * referenced by the selector.
   *
   * @param {string} selector a CSS selector.
   * @param {(e: ElementWrapper) => void)} callback the function to be called.
   */
  scanner: (selector: string, callback: (e: ElementWrapper) => void) => void;
  
  /**
   * Returns a wrapped version of the document.body element. Because all Tapestry JavaScript occurs
   * inside a block at the end of the document, inside the `<body`> element, it is assumed that
   * it is always safe to get the body.
   */
  body: ElementWrapper;
  
}