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

package org.apache.tapestry5.kaptcha.services;

import com.google.code.kaptcha.Producer;

/**
 * Extension of the {@link Producer} interface, adding property for getting the Kaptcha image
 * width and height (used when rendering the &lt;img&gt; element).
 *
 * @since 5.3
 */
public interface KaptchaProducer extends Producer
{
    int getWidth();

    int getHeight();
}
