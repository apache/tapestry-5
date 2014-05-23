// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.internal.beanvalidator;

import java.util.Locale;

import javax.validation.MessageInterpolator;

import org.apache.tapestry5.ioc.services.ThreadLocale;
/**
 * The default message interpolation algorithm uses {@link Locale#getDefault()}. This behavior is not appropriate for Tapestry applications, 
 * thus we need a {@link Locale} aware message interpolator.
 */
public class MessageInterpolatorImpl implements MessageInterpolator 
{
	private final MessageInterpolator delegate;
	private final ThreadLocale threadLocale;
	
	public MessageInterpolatorImpl(MessageInterpolator delegate, ThreadLocale threadLocale) 
	{
		this.delegate = delegate;
		this.threadLocale = threadLocale;
	}

	/**
	 * @see javax.validation.MessageInterpolator#interpolate(java.lang.String, javax.validation.MessageInterpolator.Context)
	 */
	@Override
	public String interpolate(String messageTemplate, Context context) 
	{
		return interpolate(messageTemplate, context, threadLocale.getLocale());
	}

	/**
	 * @see javax.validation.MessageInterpolator#interpolate(java.lang.String, javax.validation.MessageInterpolator.Context, java.util.Locale)
	 */
	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) 
	{
		return this.delegate.interpolate(messageTemplate, context, locale);
	}

}
