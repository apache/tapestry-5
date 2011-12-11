// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;

public class ActivationRequestParameterDemo
{

    @Property
    @Persist(PersistenceConstants.FLASH)
    private Integer selectedClickCount;

    @Property
    private boolean clickCountSet;

    @Property
    @ActivationRequestParameter
    private Integer clickCount;

    @Property
    @ActivationRequestParameter("status-message")
    private String message;

    void onActivate()
    {
        clickCountSet = clickCount != null;
    }

    void onActionFromIncrement()
    {
        clickCount = clickCount == null ? 1 : clickCount + 1;
    }

    void onActionFromSetMessage()
    {
        message = "Link clicked!";
    }

    void onActionFromSetSpecialMessage()
    {
        message = "!#$&'()*+,/:;=?@[]";
    }

    void onActionFromReset()
    {
        clickCount = null;
        message = null;
    }

    public SelectModel getClickCountModel()
    {
        OptionModel one = new OptionModelImpl("one", 1);
        OptionModel two = new OptionModelImpl("two", 2);
        OptionModel three = new OptionModelImpl("three", 3);

        return new SelectModelImpl(one, two, three);
    }
}
