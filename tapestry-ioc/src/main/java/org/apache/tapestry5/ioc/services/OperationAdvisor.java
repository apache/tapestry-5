// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.plastic.MethodAdvice;

/**
 * Used from a {@linkplain org.apache.tapestry5.ioc.annotations.Advise service advice method} to identify methods with the
 * {@link org.apache.tapestry5.ioc.annotations.Operation} annotation, and add advice for those methods. This advice should typically
 * be provided first, or nearly first, among all advice, to maximize the benefit of tracking operations.
 *
 * @since 5.4
 */
public interface OperationAdvisor
{
    /**
     * Adds {@linkplain #createAdvice advice} to methods with the {@link org.apache.tapestry5.ioc.annotations.Operation} annotation.
     */
    void addOperationAdvice(MethodAdviceReceiver receiver);

    /**
     * Creates advice for a method.
     *
     * @param description the text (or format) used to display describe the operation for the method
     * @return method advice
     * @see org.apache.tapestry5.ioc.annotations.Operation#value()
     */
    MethodAdvice createAdvice(String description);
}
