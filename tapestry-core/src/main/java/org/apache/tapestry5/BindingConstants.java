//  Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5;

/**
 * Constants for the built-in binding prefixes.  These are often used with the {@link
 * org.apache.tapestry5.annotations.Parameter#defaultPrefix()} annotation attribute.
 */
public class BindingConstants
{
    /**
     * Binding expression prefix used for literal strings.
     */
    public static final String LITERAL = "literal";
    /**
     * Binding expression prefix used to bind to a property of the component. When {@link
     * org.apache.tapestry5.annotations.Parameter#defaultPrefix()} is not specified, the default is PROP.
     */
    public static final String PROP = "prop";

    /**
     * A way of selecting a named {@link org.apache.tapestry5.NullFieldStrategy} contributed to {@link
     * org.apache.tapestry5.services.NullFieldStrategySource}.
     */
    public static final String NULLFIELDSTRATEGY = "nullfieldstrategy";

    /**
     * A reference to a component within the container's template, by local component id.
     */
    public static final String COMPONENT = "component";

    /**
     * A reference to a localized message from the component's message catalog (including message keys inherited from
     * the application global message catalog).
     */
    public static final String MESSAGE = "message";

    /**
     * References (and configures) one ore more named {@link org.apache.tapestry5.Validator}s contributed to the {@link
     * org.apache.tapestry5.services.FieldValidatorSource} service.
     *
     * @see org.apache.tapestry5.services.FieldValidatorSource
     */
    public static final String VALIDATE = "validate";

    /**
     * References a named {@link org.apache.tapestry5.Translator} contributed to the {@link
     * org.apache.tapestry5.services.TranslatorSource} service. The binding is of type {@link
     * org.apache.tapestry5.FieldTranslator}.
     */
    public static final String TRANSLATE = "translate";

    /**
     * References a named block within the template.
     */
    public static final String BLOCK = "block";

    /**
     * References a localized asset.  The asset will be relative to the component's class file, unless a prefix
     * (typically, "context:") is used on the expression.    Typically, this is used for classpath assets relative to
     * the component class, and {@link #CONTEXT} is used for context assets.
     *
     * @see org.apache.tapestry5.Asset
     * @see org.apache.tapestry5.services.AssetSource
     */
    public static final String ASSET = "asset";

    /**
     * Allows for temporary storage of information during the render only (may not currently be used during form
     * submission processing).  This is often used to store the current object iterated over by a {@link
     * org.apache.tapestry5.corelib.components.Loop} component.
     */
    public static final String VAR = "var";

    /**
     * Binding factory for context assets specifically. The expression is the path from the root of the web
     * application.
     *
     * @since 5.1.0.0
     */
    public static final String CONTEXT = "context";
}
