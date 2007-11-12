// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Asset;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.internal.DefaultValidationDecorator;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.PageRenderCommand;
import org.apache.tapestry.services.ValidationMessagesSource;

/**
 * Pushes a {@link DefaultValidationDecorator} instance into the Environment's
 * {@link ValidationDecorator} service stack.
 */
public class DefaultValidationDelegateCommand implements PageRenderCommand
{
    private final ThreadLocale _threadLocale;

    private final ValidationMessagesSource _messagesSource;

    private final Asset _iconAsset;

    public DefaultValidationDelegateCommand(ThreadLocale threadLocale,
                                            ValidationMessagesSource messagesSource, Asset iconAsset)
    {
        _threadLocale = threadLocale;
        _messagesSource = messagesSource;
        _iconAsset = iconAsset;
    }

    public void cleanup(Environment environment)
    {
        environment.pop(ValidationDecorator.class);
    }

    public void setup(Environment environment)
    {
        Messages messages = _messagesSource.getValidationMessages(_threadLocale.getLocale());

        ValidationDecorator decorator = new DefaultValidationDecorator(environment, messages,
                                                                       _iconAsset);

        environment.push(ValidationDecorator.class, decorator);
    }

}
