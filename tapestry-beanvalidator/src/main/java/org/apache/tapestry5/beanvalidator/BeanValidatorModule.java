// Copyright 2009, 2010, 2011 The Apache Software Foundation
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
package org.apache.tapestry5.beanvalidator;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.beanvalidator.*;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.*;
import javax.validation.groups.Default;

/**
 * Module for JSR-303 services.
 *
 * @since 5.2.0.0
 */
public class BeanValidatorModule
{
    public static void bind(final ServiceBinder binder)
    {
        binder.bind(FieldValidatorDefaultSource.class, BeanFieldValidatorDefaultSource.class).withSimpleId();
        binder.bind(BeanValidatorGroupSource.class, BeanValidationGroupSourceImpl.class);
        binder.bind(BeanValidatorSource.class, BeanValidatorSourceImpl.class);
        binder.bind(ClientConstraintDescriptorSource.class, ClientConstraintDescriptorImpl.class);
    }

    public static void contributeServiceOverride(
            MappedConfiguration<Class, Object> configuration,
            @Local FieldValidatorDefaultSource source)
    {
        configuration.add(FieldValidatorDefaultSource.class, source);
    }

    public static Validator buildBeanValidator(ValidatorFactory validatorFactory, PropertyShadowBuilder propertyShadowBuilder)
    {
        return propertyShadowBuilder.build(validatorFactory, "validator", Validator.class);
    }


    public static ValidatorFactory buildValidatorFactory(BeanValidatorSource beanValidatorSource, PropertyShadowBuilder propertyShadowBuilder)
    {
        return propertyShadowBuilder.build(beanValidatorSource, "validatorFactory", ValidatorFactory.class);
    }

    public static void contributeBeanValidatorGroupSource(
            final Configuration<Class> configuration)
    {
        configuration.add(Default.class);
    }

    public static void contributeBeanValidatorSource(
            final OrderedConfiguration<BeanValidatorConfigurer> configuration, final ThreadLocale threadLocale)
    {
        configuration.add("LocaleAwareMessageInterpolator", new BeanValidatorConfigurer()
        {
            public void configure(javax.validation.Configuration<?> configuration)
            {
                MessageInterpolator defaultInterpolator = configuration.getDefaultMessageInterpolator();

                configuration.messageInterpolator(new MessageInterpolatorImpl(defaultInterpolator, threadLocale));
            }
        });
    }

    public static void contributeClientConstraintDescriptorSource(
            final Configuration<ClientConstraintDescriptor> configuration)
    {
        configuration.add(new ClientConstraintDescriptor(Max.class, "maxnumber", "value"));
        configuration.add(new ClientConstraintDescriptor(Min.class, "minnumber", "value"));
        configuration.add(new ClientConstraintDescriptor(NotNull.class, "notnull"));
        configuration.add(new ClientConstraintDescriptor(Null.class, "isnull"));
        configuration.add(new ClientConstraintDescriptor(Pattern.class, "pattern", "regexp"));
        configuration.add(new ClientConstraintDescriptor(Size.class, "size", "min", "max"));
    }

    public void contributeMarkupRenderer(
            OrderedConfiguration<MarkupRendererFilter> configuration,

            final AssetSource assetSource,

            final ThreadLocale threadLocale,

            final Environment environment)
    {
        MarkupRendererFilter injectBeanValidatorScript = new MarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, MarkupRenderer renderer)
            {

                JavaScriptSupport javaScriptSupport = environment.peek(JavaScriptSupport.class);

                Asset validators = assetSource.getAsset(null, "org/apache/tapestry5/beanvalidator/tapestry-beanvalidator.js",
                        threadLocale.getLocale());

                javaScriptSupport.importJavaScriptLibrary(validators);

                renderer.renderMarkup(writer);
            }
        };


        configuration.add("BeanValidatorScript", injectBeanValidatorScript, "after:*");
    }

}
