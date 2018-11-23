Scratch pad for changes destined for the 5.4 release notes page.

# Non-Breaking Changes:

A new DeprecationWarning service exists to write runtime warnings about deprecated component parameters.

There have been sweeping changes to the client-side support in JavaScript, including the long-awaited abstraction layer.
Tapestry is moving to the use of asynchronously-loaded modules, using the RequireJS library. Virtually all of Tapestry's
existing JavaScript libraries are being recoded as JavaScript modules; in 5.5, the remaining JavaScript libraries will
be removed.

The abstraction layer, the `t5/core/dom` module, is a set of wrappers that encapsulate most of the differences between
Prototype and jQuery. By coding to the abstraction layer, it will be possible to swap
out Prototype support for jQuery.  Prototype will no longer be bundled with Tapestry starting in Tapestry 5.5. Ultimately
it will be possible to efficiently support other foundation frameworks, such as MooTools or ExtJS, by providing your
own implementation of the abstraction layer.

Prior releases of Tapestry would require several separate constructs to initialize client-side behavior:

* A unique id, generated on the server, on a client-side element
* A JavaScript library that extended T5.initializers with an initialization function
* Creating a _spec_ to describe behavior, including URLs and the unique client element id
* Many similar client-side event handlers on specific elements (created by the initialization function)

Although this was well-structured, it had numerous pain points for developers, as well as some client-side performance issues.
For example, it means that components that occur many times on the page will include many similar event handler
functions.

Tapestry 5.4 represents a shift to a more modern, lighter approach. Behavior of components is encoded into
`data-` attributes on the element, and logic shifts to a module that provides top-level event handlers on the
document object. This is an overall win: it reduces the number of event handlers, solves some timing issues related
to running initialization functions, deals with DOM updates better, and is overall more performant.

Tapestry is moving away from "magic class names" to a more uniform approach based on the use of HTML 5 compliant `data-`
attributes.

## Service proxies annotations

The service proxies created by Tapestry-IoC for services defined on interfaces, not just a concrete class, now have the
service implementation class annotations. The same now happens to annotations in service methods too.

## SubModule Deprecated

The SubModule annotation has been deprecated; the new ImportModule annotation does exactly the same thing,
but is more clearly named.

## Asset Improvements

Prior versions of Tapestry created cacheable URLs for Assets that incorporated the application version number. The
Assets were served with a far-future expires header: the client browser would not even need to check to see
if the asset had changed.

Unfortunately, when any asset changed in a new deployment of the application, the version number of the entire
application needed to change, resulting in *all* assets being downloaded 
(because the application version number in their URLs changed).

In this release, individual assets are given a URL containing a checksum based on the asset's content. When the underlying
file is changed, the asset will be served with the new URL, but unchanged assets will not be affected. This means
that when redeploying your application, you'll see far less asset traffic, as most client web browsers will already
have most assets (whose contents have not changed) in their local cache.

In prior releases of Tapestry, the response to an asset URL could be compressed (with GZip) if the client supported
it, and the file was itself compressable. Compressable assets include CSS files and JavaScript, but not image
format files (those have built in compression). The fact that two different versions of the file were available
with the same URL could confuse some Content Delivery Networks. In Tapestry 5.4, compressed and uncompressed
asset URLs are distinct.

Tapestry 5.4 now re-writes CSS files, expanding any `url()` references in them into fully qualify URLs; this
is to allow for the checksum embedded into each URL, which breaks relative references.

Tapestry 5.4 introduces a new module, tapestry-webresources, which provides support for compiling
CoffeeScript into JavaScript, Less into CSS, and for minimizing CSS and JavaScript.
All processing takes place at runtime.

Tapestry now properly handles the case for a base class referencing assets that is subclassed into a
different library (or subclasses from a library to the application). As long as the new location, under
`META-INF/assets`, is used, then Tapestry will locate the base class asset inside the subfolder
corresponding the the base class' library. In prior releases, the resolution was against the
subclass' library, which would fail.

It is now possible to control, for each JavaScript Stack, how that stack treats its JavaScript libraries.
The default is to aggregate the libraries and minimize them, but there are now options to aggregate
them without minimizing, or to leave them as individual files (neither aggregating, nor minimizing).

# Classpath asset protection (introduced in 5.4.4)
A new service, `ClasspathAssetProtectionRule`, which receives contributions of `ClasspathAssetProtectionRule`
instances, was created to you can easily add rules to block requests to classpath assets according to your 
security needs. 

Three rules are added out-of-the-box and may be overriden:
* `ClassFile`: blocks access to assets with `.class` endings (case insensitive).
* `PropertiesFile`: blocks access to assets with `.properties` endings (case insensitive).
* `XMLFile`: blocks access to assets with `.xml` endings (case insensitive).

## FormGroup Mixin

This new mixin for Field components adds the outer `<div class="form-group">` and `<label>` elements for a Field
to layout correctly inside a Twitter Bootstrap form.

## Glyphicon Component

This new component renders a `<span>` tag for a Bootstrap 3 Glyphicon.

## DevTool Component

A component that can be added to your application-specific Layout to provide some useful support when in development
mode (but disabled in production mode):
- Identifies the current page's class name and logical name
- Re-render the current page
- Re-render the current page with component rendering comments
- Reset the current page's persistent state
- Kill (invalidate) any HttpSession
- Open the T5 Dashboard in a new tab
- Force the reload of component classes

## T5Dashboard Page

The T5 Dashboard is a new page the consolidates Tapestry 5.3's PageCatalog and ServiceStatus pages.
The page is itself extensible, allowing libraries or applications to add their own tabs.

## tapestry-test deprecated

The tapestry-test module contains base classes used when writing TestNG, Selenium, and EasyMock tests.
It has been deprecated; users are directed to the Spock Framework (as a replacement for TestNG and EasyMock),
and to Geb (which is a vastly improved wrapper around Selenium, and works nicely with Spock).

The useful RandomDataSource class has been extracted into a new module, tapestry-test-data.

The code for launching an instance of Jetty or Tomcat has been extracted to a new module, tapestry-runner.

## Select Component

The Select component has a new "secure" parameter which can be "always", "never" or "auto".
When "always", the submitted value must be listed somewhere in the SelectModel. When "never",
this check is not performed. When "auto" (the default), the check is performed only if the
SelectModel exists (is non-null) at the time of the submission.

## @RequestParameter annotation improvements

Now this annotation supports arrays: `void onEvent(@RequestParameter("values") Integer[] values)`. Same conversion
rules used to single values apply to arrays too.

## Easy rendering of parts of a template to a string

The new `PartialTemplateRenderer` service provides easy rendering to a string of blocks, component instances or
any other object which implements `RenderCommand` or coerced to it.

## Raw HTML in Alerts

The `Alert` class and the `Alerts` component now support rendering raw HTML instead of just plain text.

## Page Preloading

It is now possible to pre-load pages at system startup time by making contributions
to the new PagePreloader service. Pages can be pre-loaded only in development, only in production,
never, or always.

# Breaking Changes:

## Java 1.6 required

As of version 5.4, Tapestry requires Java 1.6 at least.

## clientId required for Ajax field decoration

Applications that perform server-side validation of form control data (such as TextField) *as part of
an Ajax Zone update* should bind the Zone's simpleIds parameter to true. This disables the injection of a per-request
unique id into allocated client-side ids and client-side control names.

Non-Ajax requests are not affected.  Client-side validation is not affected. Only the rare case where validation
only occurs on the server is affected; Tapestry has lost the ability to coordinate the Tapestry-generated id
for the field (this is related to big improvements in rendering described below).

## Charset for Assets

Tapestry now assumes that all text/* assets use the utf-8 character set. 
This is generally the case for JavaScript files (the primary text asset). 

## Logging Dependencies

Previous versions of Tapestry included dependencies on both SLF4J (Simple Logging Facade for Java)
and Log4J (the most common logging library), as well as the bridge between the two. Users who did not wish to
use Log4J had to muck about with dependencies in their Maven or Gradle build file.

Tapestry 5.4 only has dependencies on SL4FJ and not on any particular logging implementation; SLF4J will 
write only a warning message that it needs to be configured. Application developers must provide additional
dependencies (hint: use Logback!).

## tapestry-yuicompressor replaced by tapestry-webresources

The tapestry-yuicompressor module has been removed and superseded by tapestry-webresources.

With tapestry-webresources in place, you can write your client-side code in CoffeeScript (with the ".coffee" file
extension) and Tapestry will take care of converting it, at runtime, to JavaScript.

This support is separate and optional, as it is adds several large dependencies.

By default, tapestry-webresources enables:
- compilation of CoffeeScript to JavaScript (using Java's Rhino JavaScript engine)
- compilation of Less to CSS
- minification of CSS using YUICompressor's CSS minimizer
- minification of JavaScript using the Google Closure compiler (in simple optimizations mode)

## RenderSupport Removed

The RenderSupport interface, which was deprecated in Tapestry 5.2, has been removed entirely.

## FormFragment Component

The FormFragment component's visibleBound parameter is no longer supported; it was used to make a decision about how
far up the DOM above the FormFragment's client-side element should be searched when determining visibility. This may
resurface in the future as a CSS expression, but is currently not supported.

## ExceptionReport Page

The default exception report page has been improved dramatically:
- Formatting improvements, care of Bootstrap
- New links at the top of the page, to reload the failed page, or back to the root
- In development mode, additional links to reload pages after clearing the component cache
- A list of all threads, including their status

## Page Suffix for Page Names

It is now possible to use "page" as a suffix on a page name. The "page" suffix is stripped off of the logical page
name; thus for AccountsPage.java, the logical page name will be "Accounts", and this name (in lower case)
will be used inside generated URLs.  However, "AccountsPage" will also be recognized in URLs or in code. The
component template should always be named after the Java class itself, here "AccountsPage.tml".

This name stripping, combined with stripping out package names as prefixes and suffixes, can be somewhat hard to
reason about. Tapestry has always logged a listing of all these aliases at startup; in rare cases, you may
see conflicts or undesirable page names, and you should rename your classes to suit.

## ExceptionReporter Service

A new service, `ExceptionReporter`, will now create a text file on the file system for each runtime request processing exception.

## Symbol tapestry.asset-path-prefix

The definition of the symbol 'tapestry.asset-path-prefix' has changed; it no longer includes the leading and trailing
slashes. The default in 5.3 was "/assets/", in 5.4 it is simply "asset".

A second symbol, "tapestry.compressed-asset-path-prefix" has been added; this is used when assets are to be compressed.
It defaults to "${tapestry.asset-path-prefix}.gz". You should generally see "/asset" URLs for image files, and
"/asset.gz" URLs for CSS and JavaScript.

## Libraries de-emphasized

JavaScript Libraries (including stacks) are being replaced with modules. Note that libraries are now loaded with
RequireJS, which may mean that global values exported by the libraries are not visible; you should explicitly attach
properties to the global JavaScript window object, rather than assume that the context (the value of `this`) is the window.

## T5 and Tapestry namespaces all but eliminated

Only a limited number of properties exported in the `T5` and `Tapestry` namespaces (on the client) still exist; enough
to continue to support the `T5.initializers` approach to page initialization that was used in Tapestry 5.3 and earlier.
These will be eliminated entirely in Tapestry 5.5.

## New method on ResourceTransformer

The interface org.apache.tapestry5.services.assets.ResourceTransformer has had a new method added:
getTransformedContentType(). This makes it possible to determine which file extensions map to which content types
(for example, a ResourceTransformer for CoffeeScript files, with extension "coffee", would map to "text/javascript").

## Rendering Changes

There have been some subtle changes to how rendering of page content (in both traditional and Ajax requests) occurs;
such rendering now occurs deferred to a later stage of request processing, rather than immediately after
a component event handler method returns a value.

## Environment.cloak() and decloak() deprecated

Related to the rendering changes, the `cloak()` and `decloak()` methods of the Environment service are deprecated
and should not be invoked: they now throw an UnsupportedOperationException.

## Zone component change

Older versions of Tapestry included client-side support for an element with the CSS class "t-zone-update" as the actual
element to be updated when new content is provided for the zone in a partial page render response. This feature has been
removed with no replacement.

## Scriptaculous Deprecated

Tapestry code no longer makes use of Scriptaculous. Instead, Tapestry will fire events on elements, and user code may
decide to animate them using whatever library is desired. The event names are defined in the `t5/core/events` module.

## Floating Console

On the client side, the "floating console" is now only used in cases where a native console is not available. The console
should not be used to present information to ordinary users (Bootstrap provides Alerts for that purpose), but is only
intended for use in development.

The floating console has also been extended with an improved UI and the ability to filter the content shown.

In development and testing it can be desirable to have the floating console always visible; in that case, add the attribute
`data-floating-console="enabled"` to the `<body>` element.  However, the floating console can sometimes obscure page
content and interfere with tests; `data-floating-console="invisible"` will enable the console, but keep it hidden; the
messages written to the console will be visible in the page source.

## Form.clientValidation parameter

Prior releases of Tapestry mapped "true" and "false" values for Form.clientValidation to BLUR and NONE. This mapping
was introduced in Tapestry 5.2, and has now been removed.

Support for validating fields on blur (i.e., when tabbing out of a field) has been removed. Validation now occurs when
the form is submitted, or not at all. The ClientValidation.BLUR enum value has been deprecated and is now treated
identically to SUBMIT.

## Page loading mask

Tapestry now adds a "page loading mask" to the page; this mask will dim the browser window and add a spinning
"wait cursor" until all JavaScript on the page has been loaded and initialized. The mask prevents all interaction
with the page.

CSS animations are used to fade in the mask after a short period of time; this makes the mask less obtrusive.

## Wait-for-page logic removed

Tapestry 5.3 contained client-side code that attempted to prevent Ajax requests until after the page had loaded;
this was based on the function `Tapestry.waitForPage()`.  Server components no longer make use of this function, and the function
itself now does nothing. 

However, once initial page initialization has occurred, the attribute `data-page-initialized` on the root HTML element
is set to "true". In many cases, automated tests should be updated to wait for this attribute to be set after loading
a new page.

## Bootstrap 3

Tapestry now includes a default copy of Bootstrap 3.3.5, in addition to its own default set of CSS rules.
The Tapestry CSS from prior releases has been largely eliminated; instead
components now refer to standard Bootstrap CSS classes.

Tapestry now automatically imports the "core" stack for all pages (in previously releases, the "core" stack
was only imported if the page made use of JavaScript). Because of this, the Bootstrap CSS will always be available.

ValidationDecorator and ValidationDecoratorFactory are deprecated in 5.4 and will be removed in 5.5. The default
implementation of ValidationDecorator now does nothing. All the logic related to presentation of errors has moved
to the client, and expects and leverages the Bootstrap CSS.

Fields that require validation messages to be displayed fire events, and the default handlers show and update
help blocks that appear (by default) beneath the fields. To get the full effect, you should enclose your fields inside
.form-group elements, as described in the Bootstrap documentation:
http://getbootstrap.com/css/#forms

You may also use the new FormGroup mixin for most fields.

Bootstrap's JavaScript has been broken into multiple "shimmed" JavaScript modules; e.g., "bootstrap/modal" to
enable the JavaScript to support Bootstrap's modal dialogs. These modules do not export any value, but they
do establish dependencies: to Bootstrap's "transition" module to enable animations, and ultimately to "jquery".

Bootstrap is added to the core JavaScriptStack. Applications can override the tapestry.bootstrap-root symbol, or
the StackElement entries contributed to the core JavaScript stack to use an application-customized version of Bootstrap.

Certain built-in Tapestry pages (the T5Dashboard, the default ExceptionReport page) are set up to ignore any changes
to the core stack CSS, and use the default copy of Bootstrap (otherwise, the CSS changes can corrupt the layout of
these pages).

## Form element components

TextField, PasswordField, TextArea, and Select now render the CSS class attribute `form-control`; you may add additional
CSS class names with the `class` informal parameter.  Generally, you will want to add an enclosing element with
`col-md-x` CSS class control the size of the element (otherwise it will stretch to 100% of the available width).

## LabelComponent

The Label component now renders the CSS class attribute as "control-label"; you may add additional CSS class names
with the `class` informal parameter.

## Error Component

When Tapestry must present a validation exception, it will often dynamically create the `p.help-block`
needed to display the message; it does a reasonable job of positioning the new element just after the field, or
just after the `.input-group` containing the field. The Error component can be used to explicitly place this element.

Also, note that part of the styling of the element is predicated on containment inside a `.form-group` (which may have
the `has-error` selector added or removed).

## Regexp Validator

This validator now always writes the `pattern` and `title` attributes into the element, even when the Form
is configured to disable client side validation. On modern browsers, the pattern will be enforced, and the title
will be displayed in a browser-supplied popup window.

## BeanEditor / BeanEditForm

The property edit blocks contributed to the BeanBlockSource service should expect to be nested inside a
div.form-group, which is provided around the editor for each property.

## ClientBehaviorSupport

This service, primarily used by built-in components in Tapestry 5.3, is no longer useful in 5.4. The service
still exists, but the methods do nothing, and the service and interface will be removed in 5.5.

## JavaScriptSupport Extended

New methods have been added to allow JavaScript modules to be "required" into the page; it is possible to invoke
the exported function of a module with JSON-compatible parameters; a module may also export multiple names functions that
can be invoked.

## Palette Component

The selected property is now type `Collection`, not specifically type `List`. It is no longer allowed to be null. You may
need to provide a "prepare" event handler to initialize the property before it is read by the Palette component.

## Autocomplete Mixin

The Autocomplete mixin has been rewritten to use Twitter typeahead.js (0.10.5); this implies it will also force jQuery onto the page,
to support the typeahead.js library. In addition, typeahead.js does not support multiple
tokens, so this behavior (available in prior releases) has been removed.

## RenderNotification Mixin

The timing of this mixin has changed, it now has the @MixinAfter annotation, so it triggers its events *after*
the component to which it is attached has executed its @BeginRender phase, and *before* the component executes
its @AfterRender phase.

## LocalizationSetter service

The method setNonPeristentLocaleFromLocaleName() was renamed to setNonPersistentLocaleFromLocaleName() to correct
the typo (the missing 's').

## OperationTracker Extended

The OperationTracker interface has had a new method added, for performing an IO Operation (that may throw IOException).

## FormSupport Extended

The FormSupport interface has a new method, storeCancel(), which stores a component action that is only executed
if the containing form is canceled.

## Session Locking

Tapestry now uses a lock on access to the HttpSession; a shared read lock is acquired when reading session attribute
names; an exclusive write lock is acquired when reading or writing session attributes. Locks, once acquired, are kept
until the end of the request. A new configuration symbol can be used to turn this feature off, reverting to Tapestry
5.3 behavior.

## AssetPathConstructor

The interface for AssetPathConstructor was changed incompatibly, to allow for individual asset checksums.

## Link and LinkSecurity

The enum type LinkSecurity was an internal class in 5.3 despite being used in methods of the public Link interface.
It has been moved to package org.apache.tapestry5; this represents a minor incompatible change to the Link interface
(which is rarely, if ever, implemented outside of the framework).

## StreamableResource Extended

StreamableResource has been modified, adding a new `getChecksum()` method; this interface is rarely, if ever,
used or implemented by application code.

## MutableComponentModel Extended

MutableComponentModel has been modified, adding a new `doHandleActivationEventContext()' method to help check
activation context exactness; this interface is rarely, if ever, used or implemented by application code.

## IoC Module classes moved to new packages

Traditionally, Tapestry IoC Module classes have lived in the same package as the service interfaces they define, and
at the same time reference implementation classes in a separate (usually internal) package. This is not a desirable
approach, as proper practices is to avoid cases where separate packages import classes from each other.

To resolve this, new `modules` packages have been created, and module classes have been moved there. Since module
classes are typically not used directly by end-user code, this is not expected to be a disruptive change.

## tapestry-test and TapestryTestConstants

Several constants defined in org.apache.tapestry5.test.TapestryTestConstants have moved to a new class,
org.apache.tapestry5.test.TapestryRunnerConstants in the new tapestry-test-constants module. tapestry-core
has a dependency on module tapestry-test-constants that is only needed when making use of the PageTester
facility. When using PageTester, you will need to make tapestry-test-constants a testRuntime dependency.

## AbstractValidator base class

The constructor for AbstractValidator has changed to include an instance of JavaScriptSupport.

## CSS Changes

Where Tapestry-specific CSS still exists (in support of the Palette component and the Tree component), the "t-" prefix
has been removed. This may affect applications that overrode the Tapestry CSS rules to adapt Tapestry to the application
look and feel.

The Grid component no longer emits CSS class names, instead it renders data attributes into Grid headers, rows, and cells,
such as `data-grid-row="first"`, `data-grid-property="title"`, etc. These attributes may still be referenced using CSS rules
where desired.

## ValueEncoder Changes

The automatic ValueEncoder from String to any Number type, or to Boolean have
changed slightly. An empty input string is encoded to a null rather than being passed through
the type coercer. This reflects the desire that a submitted value (in a URL or a form) that is blank
is the same as no value: null.

## Client Persistent Fields

Tapestry now determines if a client-persistent field is mutable and contains changes, using the same
checks as for session-persistent objects (the OptimizedSessionPersistedObject interface and so forth).
Previously, Tapestry would load a mutable object into a field, but a change to the internal state
of that mutable field would not always be preserved in the serialized object data attached to links.

## ContentType

The ContentType objects, used to represent a content types such as "text/html" in a structured manner,
has been revised significantly in 5.4; it is now an immutable data type. In addition, a few scattered
interfaces that used a String content type have been changed to use the ContentType class instead.

## FormInjector Removed

The FormInjector component was removed; it was intended for use only inside the AjaxFormLoop component
(which was rewritten in 5.4 and no longer uses FormInjector). FormInjector was not widely used elsewhere, if 
it was used at all.