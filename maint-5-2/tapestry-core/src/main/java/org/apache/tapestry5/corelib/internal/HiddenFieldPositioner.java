// Copyright 2009, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.MarkupWriterListener;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.services.HiddenFieldLocationRules;

/**
 * Used to position a hidden field (as part of a form-related component). Hidden fields are not allowed to go just
 * anywhere, there are rules, dictated by the (X)HTML schema, about where they are allowed. We use the
 * {@link org.apache.tapestry5.MarkupWriterListener} interface to monitor elements as they are started and ended to find
 * a
 * place to put content.
 */
public class HiddenFieldPositioner
{
    /**
     * The type of element to create.
     */
    private static final String ELEMENT = "input";

    private final MarkupWriter writer;

    private final HiddenFieldLocationRules rules;

    private final OneShotLock lock = new OneShotLock();

    private Element hiddenFieldElement;

    private final MarkupWriterListener listener = new MarkupWriterListener()
    {
        public void elementDidStart(Element element)
        {
            if (rules.placeHiddenFieldInside(element))
            {
                hiddenFieldElement = element.element(ELEMENT);
                writer.removeListener(this);
            }
        }

        public void elementDidEnd(Element element)
        {
            if (rules.placeHiddenFieldAfter(element))
            {
                hiddenFieldElement = element.getContainer().element(ELEMENT);
                writer.removeListener(this);
            }
        }
    };

    public HiddenFieldPositioner(MarkupWriter writer, HiddenFieldLocationRules rules)
    {
        this.writer = writer;
        this.rules = rules;

        this.writer.addListener(listener);
    }

    /**
     * Returns the hidden field element, which can have its attributes filled in.
     * 
     * @return the element
     * @throws IllegalStateException
     *             if the element was not positioned
     */
    public Element getElement()
    {
        lock.lock();

        // Remove the listener if it hasn't been removed already.

        writer.removeListener(listener);

        if (hiddenFieldElement == null)
            throw new IllegalStateException(
                    "The rendered content did not include any elements that allow for the positioning of the hidden form field's element.");

        return hiddenFieldElement;
    }

    /**
     * Discard this positioner (an alternative to invoking {@link #getElement()}).
     * If an {@link Element} has been created for the hidden field, that
     * element is removed.
     * 
     * @since 5.2.0
     */
    public void discard()
    {
        lock.lock();

        if (hiddenFieldElement != null)
            hiddenFieldElement.remove();

        writer.removeListener(listener);
    }

}
