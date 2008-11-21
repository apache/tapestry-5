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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.AnnotationProvider;

/**
 * Defines a context for editing a bean via {@link org.apache.tapestry5.corelib.components.BeanEditor}.
 * This value is made available at render time via the {@link org.apache.tapestry5.annotations.Environmental} annotation.
 */
public interface BeanEditContext extends AnnotationProvider
{
    /**
     * @return The class of the bean under edit.
     */
    Class<?> getBeanClass();
}
