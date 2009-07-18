// Copyright 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.annotations;

/**
 * Constants for documenting the context wherein the tapestry-provided annotations may be used, in conjunction with
 * {@link org.apache.tapestry5.ioc.annotations.UseWith}.
 *
 * @since 5.2.0.0
 */
public enum AnnotationUseContext
{
    /**
     * Annotation may be used on/in component classes
     */
    COMPONENT,

    /**
     * Annotation may be used on/in mixins
     */
    MIXIN,

    /**
     * Annotation may be used on modules
     */
    MODULE,

    /**
     * Annotation may be used on/in page classes
     */
    PAGE,

    /**
     * Annotation may be used on/in services
     */
    SERVICE,

    /**
     * Annotation may be used for service decorators
     */
    SERVICE_DECORATOR,

    /**
     * Annotation may be used on/in arbitrary java beans.
     */
    BEAN
}
