// Copyright 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5;

import org.apache.tapestry5.dom.Element;


/**
 * An interface that allows objects to be alerted when after an element is started, and after an element is ended.
 */
public interface MarkupWriterListener
{
    /**
     * Invoked just after an element and its initial set of attributes has been written.
     *
     * @param element element just created and populated with attributes
     * @see org.apache.tapestry5.MarkupWriter#element(String, Object[])
     * @see org.apache.tapestry5.MarkupWriter#elementNS(String, String)
     */
    void elementDidStart(Element element);

    /**
     * Invoked just after an element has ended.
     *
     * @param element just ended
     * @see org.apache.tapestry5.MarkupWriter#end()
     */
    void elementDidEnd(Element element);
}
