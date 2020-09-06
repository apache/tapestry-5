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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.AnnotationUseContext;
import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation used to specifically identify the {@link org.apache.tapestry5.http.services.Dispatcher} used to dispatch
 * asset requests (so that {@link org.apache.tapestry5.services.assets.AssetRequestHandler}s can be contributed).
 */
@Target(
        {ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RUNTIME)
@Documented
@UseWith(AnnotationUseContext.SERVICE)
public @interface AssetRequestDispatcher
{
}
