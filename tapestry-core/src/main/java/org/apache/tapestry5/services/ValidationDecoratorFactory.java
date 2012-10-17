// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationDecorator;

/**
 * Creates an instance of {@link org.apache.tapestry5.ValidationDecorator} for a
 * {@link org.apache.tapestry5.MarkupWriter}.    This service is overridden in applications
 * that do not wish to use the {@linkplain org.apache.tapestry5.BaseValidationDecorator default no-op validation decorator}.
 *
 * @since 5.3
 * @deprecated Deprecated in 5.4 with no replacement, as {@link ValidationDecorator} is being phased out.
 */
public interface ValidationDecoratorFactory
{
    /**
     * Creates a new decorator for the indicated writer.
     */
    ValidationDecorator newInstance(MarkupWriter writer);
}
