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

package org.apache.tapestry5.kaptcha.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.kaptcha.components.KaptchaField;
import org.apache.tapestry5.kaptcha.components.KaptchaImage;
import org.apache.tapestry5.services.PropertyEditContext;

/**
 * Page containing <i>kaptcha</i> edit blocks contributed into the
 * {@link org.apache.tapestry5.services.BeanBlockSource} service configuration.
 *
 * @see org.apache.tapestry5.services.BeanBlockContribution
 * @see org.apache.tapestry5.corelib.components.BeanEditForm
 * @since 5.3
 */
public class KaptchaEditBlocks
{
    @Environmental
    @Property(write = false)
    private PropertyEditContext context;

    @Component
    private KaptchaImage kaptchaImage;

    @Component(parameters = {
            "label=prop:context.label",
            "clientId=prop:context.propertyId",
            "image=kaptchaImage"})
    private KaptchaField kaptchaField;
}
