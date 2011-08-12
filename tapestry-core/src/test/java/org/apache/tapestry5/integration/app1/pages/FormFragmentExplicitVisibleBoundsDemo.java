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
package org.apache.tapestry5.integration.app1.pages;

/**
 * Demos the use of explicit bounds for checking visibility of a form fragment for form submission processing.
 * By default, a FormFragment searches to make sure the containing form is visible via "isDeepVisible".  If
 * no intermediate parent elements are invisible, the fragment is considered visible.  However, there are times when
 * that behavior is not desired; some element other than form should be used as the stopping point for determining
 * visibility.  This page demonstrates that use case.
 */
public class FormFragmentExplicitVisibleBoundsDemo {
}
