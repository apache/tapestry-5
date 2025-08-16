// Copyright 2009, 2010, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.translator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Set;

import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.ajax.RequireJsModeHelper;
import org.apache.tapestry5.ioc.services.ThreadLocale;

public class NumericTranslatorSupportImpl implements NumericTranslatorSupport
{
    private final TypeCoercer typeCoercer;

    private final ThreadLocale threadLocale;

    private final RequireJsModeHelper requireJsModeHelper;

    private final Set<Class> integerTypes = CollectionFactory.newSet();

    public NumericTranslatorSupportImpl(TypeCoercer typeCoercer, ThreadLocale threadLocale,
                                        RequireJsModeHelper requireJsModeHelper)
    {
        this.typeCoercer = typeCoercer;
        this.threadLocale = threadLocale;
        this.requireJsModeHelper = requireJsModeHelper;

        Class[] integerTypes =
                {Byte.class, Short.class, Integer.class, Long.class, BigInteger.class};

        for (Class c : integerTypes)
        {
            this.integerTypes.add(c);
        }

    }

    public <T extends Number> void setupTranslation(Class<T> type, Element element, String message)
    {
        String translation = isIntegerType(type) ? "integer" : "numeric";

        requireJsModeHelper.importModule("t5/core/validation");

        element.attributes("data-validation", "true",
                "data-translation", translation,
                "data-translation-message", message);
    }

    private boolean isIntegerType(Class type)
    {
        return integerTypes.contains(type);
    }

    public <T extends Number> T parseClient(Class<T> type, String clientValue) throws ParseException
    {
        NumericFormatter formatter = getParseFormatter(type);

        Number number = formatter.parse(clientValue.trim());

        return typeCoercer.coerce(number, type);
    }

    private NumericFormatter getParseFormatter(Class type)
    {
        Locale locale = threadLocale.getLocale();
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);

        if (type.equals(BigInteger.class))
            return new BigIntegerNumericFormatter(symbols);

        if (type.equals(BigDecimal.class))
            return new BigDecimalNumericFormatter(symbols);

        // We don't cache NumberFormat instances because they are not thread safe.
        // Perhaps we should turn this service into a perthread so that we can cache
        // (for the duration of a request)?

        // We don't cache the rest of these, because they are built on DecimalFormat which is
        // not thread safe.

        if (isIntegerType(type))
        {
            NumberFormat format = NumberFormat.getIntegerInstance(locale);
            return new NumericFormatterImpl(format);
        }

        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(locale);

        if (type.equals(BigDecimal.class))
            df.setParseBigDecimal(true);

        return new NumericFormatterImpl(df);
    }

    private NumericFormatter getOutputFormatter(Class type)
    {
        Locale locale = threadLocale.getLocale();

        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);

        if (type.equals(BigInteger.class))
            return new BigIntegerNumericFormatter(symbols);

        if (type.equals(BigDecimal.class))
            return new BigDecimalNumericFormatter(symbols);

        // We don't cache the rest of these, because they are built on DecimalFormat which is
        // not thread safe.

        if (!isIntegerType(type))
        {
            NumberFormat format = NumberFormat.getNumberInstance(locale);

            return new NumericFormatterImpl(format);
        }

        DecimalFormat df = new DecimalFormat(toString(symbols.getZeroDigit()), symbols);

        return new NumericFormatterImpl(df);
    }

    public <T extends Number> String toClient(Class<T> type, T value)
    {
        return getOutputFormatter(type).toClient(value);
    }

    public <T extends Number> String getMessageKey(Class<T> type)
    {
        return isIntegerType(type) ? "integer-format-exception" : "number-format-exception";
    }

    private static String toString(char ch)
    {
        return String.valueOf(ch);
    }
}
