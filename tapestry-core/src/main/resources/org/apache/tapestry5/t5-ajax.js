/* Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Defines Tapestry Ajax support, which includes sending requests and receiving
 * replies, but also includes default handlers for errors and failures, and
 * processing of Tapestry's partial page render response (a common response for
 * many types of Ajax requests).             `
 */
T5.define("ajax", function() {

    var _ = T5._;
    var $ = T5.$;
    var spi = T5.spi;

    var exceptionContainer, iframe, iframeDocument;

    function noop() {
    }

    function writeToErrorIFrame(content) {
        // Clear current content.
        iframeDocument.open();
        // Write in the new content.
        iframeDocument.write(content);
        iframeDocument.close();

    }

    function resizeExceptionDialog() {
        // Very Prototype specific!
        var dims = document.viewport.getDimensions();

        iframe.width = dims.width - 100;
        iframe.height = dims.height - (100 + 20);
    }

    /**
     * When there's a server-side failure, Tapestry sends back the exception report page as HTML.
     * This function creates and displays a dialog that presents that content to the user using
     * a created iframe element.
     * @param exceptionContext HTML markup for the exception report
     */
    function showExceptionDialog(exceptionContent) {

        if (!exceptionContainer) {
            var markup = [ "<div class='t-exception-container'>",
                "<iframe class='t-exception-frame' width='100%'></iframe>",
                "<div class='t-exception-controls'>",
                "<span class='t-exception-close'>Close</span>",
                "</div>",
                "</div>"].join("");

            exceptionContainer = T5.dom.find(T5.dom.appendMarkup(document.body, markup), 'div.t-exception-container');

            iframe = T5.dom.find(exceptionContainer, "iframe");

            // See http://xkr.us/articles/dom/iframe-document/

            iframeDocument = (iframe.contentWindow || iframe.contentDocument);
            if (iframeDocument.document) {
                iframeDocument = iframeDocument.document;
            }

            var closeButton = T5.dom.find(exceptionContainer, ".t-exception-close");

            T5.dom.observe(closeButton, "click", function(event) {
                event.stop();
                writeToErrorIFrame("");
                T5.dom.hide(exceptionContainer);
            });

            // Call it now to set initial width/height.

            resizeExceptionDialog();

            // Very Prototype specific:

            // See http://groups.google.com/group/prototype-scriptaculous/browse_thread/thread/1b0ce3e94020121f/cdbab773fd8e7a4b
            // debounced to handle the rate at which IE sends the resizes (every pixel!)

            Event.observe(window, "resize", _.debounce(resizeExceptionDialog, 20));
        }


        writeToErrorIFrame(exceptionContent);

        exceptionContainer.show();
    }

    function defaultFailure(transport) {
    }

    function defaultException(exception) {
    }

    /**
     * Performs an AJAX request. The options object is used to identify
     * additional parameters to be encoded into the request, and to identify the
     * handlers for success and failure.
     * <p>
     * Option keys:
     * <dl>
     * <dt>parameters
     * <dd>object with string keys and string values, defines additional query
     * parameters
     * <dt>failure
     * <dd>A function invoked if the Ajax request fails; the function is passed
     * the transport
     * <dt>exception
     * <dd>A function invoked if there's an exception processing the Ajax
     * request, the function is passed the exception
     * <dt>success
     * <dd>A function invoked when the Ajax response is returned successfully.
     * The function is passed the transport object.
     * <dt>method
     * <dd>The type of request, 'get' or 'post'. 'post' is the default.
     * </dl>
     *
     * @param url
     *            the URL for the request
     * @param options
     *            an optional object that provides additional options.
     * @return not defined
     *
     */
    function request(url, options) {

        throw "not yet implemented";
    }

    return {
        defaultFailure : defaultFailure,
        defaultException : defaultException,
        defaultSuccess : noop,
        showExceptionDialog: showExceptionDialog,
        request : request
    };
});