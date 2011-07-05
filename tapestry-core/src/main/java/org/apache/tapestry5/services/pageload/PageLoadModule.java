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

package org.apache.tapestry5.services.pageload;

import org.apache.tapestry5.internal.pageload.DefaultComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.internal.pageload.DefaultComponentResourceLocator;
import org.apache.tapestry5.internal.services.ComponentTemplateSource;
import org.apache.tapestry5.internal.services.ComponentTemplateSourceImpl;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.services.Core;

/**
 * @since 5.3
 */
@Marker(Core.class)
public class PageLoadModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ComponentRequestSelectorAnalyzer.class, DefaultComponentRequestSelectorAnalyzer.class);
        binder.bind(ComponentResourceLocator.class, DefaultComponentResourceLocator.class);
        binder.bind(ComponentTemplateSource.class, ComponentTemplateSourceImpl.class);
    }
}
