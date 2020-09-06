// Copyright 2009-2013 The Apache Software Foundation
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
package org.apache.tapestry5.beanvalidator.modules;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.beanvalidator.*;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.internal.beanvalidator.*;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.services.PropertyShadowBuilder;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.FieldValidatorDefaultSource;
import org.apache.tapestry5.services.javascript.DataConstants;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.*;
import javax.validation.groups.Default;
import java.util.Map;

/**
 * Module for JSR-303 services.
 *
 * @since 5.2.0.0
 */
public class BeanValidatorModule
{

    private static final String MODULE_NAME = "t5/beanvalidator/beanvalidator-validation";

    public static void bind(final ServiceBinder binder)
    {
        binder.bind(FieldValidatorDefaultSource.class, BeanFieldValidatorDefaultSource.class).withSimpleId();
        binder.bind(BeanValidatorGroupSource.class, BeanValidationGroupSourceImpl.class);
        binder.bind(BeanValidatorSource.class, BeanValidatorSourceImpl.class);
        binder.bind(ClientConstraintDescriptorSource.class, ClientConstraintDescriptorImpl.class);
    }

    public static void contributeServiceOverride(
            MappedConfiguration<Class<?>, Object> configuration,
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
            final Configuration<Class<?>> configuration)
    {
        configuration.add(Default.class);
    }

    public static void contributeBeanValidatorSource(
            final OrderedConfiguration<BeanValidatorConfigurer> configuration, final ThreadLocale threadLocale)
    {
        configuration.add("LocaleAwareMessageInterpolator", new BeanValidatorConfigurer()
        {
            @Override
            public void configure(javax.validation.Configuration<?> configuration)
            {
                MessageInterpolator defaultInterpolator = configuration.getDefaultMessageInterpolator();

                configuration.messageInterpolator(new MessageInterpolatorImpl(defaultInterpolator, threadLocale));
            }
        });
    }

    public static void contributeClientConstraintDescriptorSource(final JavaScriptSupport javaScriptSupport,
                                                                  final Configuration<ClientConstraintDescriptor> configuration)
    {
        configuration.add(new BaseCCD(Max.class, "value")
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require("t5/core/validation");
                writer.attributes(
                        "data-validate", true,
                        "data-validate-max", attributes.get("value"),
                        "data-max-message", message);

            }
        });

        configuration.add(new BaseCCD(Min.class, "value")
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require("t5/core/validation");
                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-validate-min", attributes.get("value"),
                        "data-min-message", message);

            }
        });

        configuration.add(new BaseCCD(NotNull.class)
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require("t5/core/validation");
                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-optionality", "required",
                        "data-required-message", message);
            }
        });

        configuration.add(new BaseCCD(Null.class)
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require(MODULE_NAME);
                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-optionality", "prohibited",
                        "data-prohibited-message", message);
            }
        });

        configuration.add(new BaseCCD(Pattern.class, "regexp")
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require(MODULE_NAME);
                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-validate-regexp", attributes.get("regexp"),
                        "data-regexp-message", message);
            }
        });

        configuration.add(new BaseCCD(Size.class, "min", "max")
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require(MODULE_NAME);
                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-range-message", message);

                int min = (Integer) attributes.get("min");

                if (min != 0)
                {
                    writer.attributes("data-range-min", min);
                }

                int max = (Integer) attributes.get("max");

                if (max != Integer.MAX_VALUE)
                {
                    writer.attributes("data-range-max", max);
                }
            }
        });

        configuration.add(new BaseCCD(AssertTrue.class)
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require("t5/core/validation");

                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-expected-status", "checked",
                        "data-checked-message", message);
            }
        });

        configuration.add(new BaseCCD(AssertFalse.class)
        {
            @Override
            public void applyClientValidation(MarkupWriter writer, String message, Map<String, Object> attributes)
            {
                javaScriptSupport.require("t5/core/validation");

                writer.attributes(
                        DataConstants.VALIDATION_ATTRIBUTE, true,
                        "data-expected-status", "unchecked",
                        "data-checked-message", message);
            }
        });
    }
}
