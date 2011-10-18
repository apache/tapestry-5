// Copyright 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.def;

import org.apache.tapestry5.ioc.Markable;
import org.apache.tapestry5.ioc.annotations.Contribute;

/**
 * Extended version of {@link org.apache.tapestry5.ioc.def.ContributionDef} introduced to determine any
 * module method annotated with {@link Contribute} as a contributor method. As of version 5.2 a contribution
 * identifies the service contributed either by the service id or by a combination of {@link Contribute} annotation and
 * a set of marker annotations. This means that {@link #getServiceId()} may to return <code>null</code> if
 * {@link #getServiceInterface()} returns a non <code>null</code> value.
 *
 * @since 5.2.0
 */
public interface ContributionDef2 extends ContributionDef, Markable
{

}
