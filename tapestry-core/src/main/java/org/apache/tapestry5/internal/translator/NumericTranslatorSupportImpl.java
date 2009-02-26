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

package org.apache.tapestry5.internal.translator;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.Request;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NumericTranslatorSupportImpl implements NumericTranslatorSupport
{
    private final TypeCoercer typeCoercer;

    private final ThreadLocale threadLocale;

    private final Request request;

    private final RenderSupport renderSupport;

    private final ClientBehaviorSupport clientBehaviorSupport;

    private final Map<Locale, DecimalFormatSymbols> symbolsCache = CollectionFactory.newConcurrentMap();

    private final Set<Class> integerTypes = CollectionFactory.newSet();

    private static final String DECIMAL_FORMAT_SYMBOLS_PROVIDED = "tapestry.decimal-format-symbols-provided";

    public NumericTranslatorSupportImpl(TypeCoercer typeCoercer, ThreadLocale threadLocale, Request request,
                                        RenderSupport renderSupport, ClientBehaviorSupport clientBehaviorSupport)
    {
        this.typeCoercer = typeCoercer;
        this.threadLocale = threadLocale;
        this.request = request;
        this.renderSupport = renderSupport;
        this.clientBehaviorSupport = clientBehaviorSupport;

        Class[] integerTypes = {
                Byte.class, Short.class, Integer.class, Long.class, BigInteger.class
        };

        for (Class c : integerTypes)
            this.integerTypes.add(c);

    }

    public <T> void addValidation(Class<T> type, Field field, String message)
    {
        if (request.getAttribute(DECIMAL_FORMAT_SYMBOLS_PROVIDED) == null)
        {
            renderSupport.addScript("Tapestry.decimalFormatSymbols = %s;", createJSONDecimalFormatSymbols());

            request.setAttribute(DECIMAL_FORMAT_SYMBOLS_PROVIDED, true);
        }

        clientBehaviorSupport.addValidation(field, "numericformat", message, isIntegerType(type));
    }

    private JSONObject createJSONDecimalFormatSymbols()
    {
        Locale locale = threadLocale.getLocale();

        DecimalFormatSymbols symbols = getSymbols(locale);

        JSONObject result = new JSONObject();

        result.put("groupingSeparator", toString(symbols.getGroupingSeparator()));
        result.put("minusSign", toString(symbols.getMinusSign()));
        result.put("decimalSeparator", toString(symbols.getDecimalSeparator()));

        return result;
    }

    private DecimalFormatSymbols getSymbols(Locale locale)
    {
        DecimalFormatSymbols symbols = symbolsCache.get(locale);

        if (symbols == null)
        {
            symbols = new DecimalFormatSymbols(locale);
            symbolsCache.put(locale, symbols);
        }

        return symbols;
    }

    private boolean isIntegerType(Class type)
    {
        return integerTypes.contains(type);
    }

    public <T> T parseClient(Class<T> type, String clientValue) throws ParseException
    {
        NumberFormat formatter = getParseFormatter(type);

        Number number = formatter.parse(clientValue.trim());

        return typeCoercer.coerce(number, type);
    }

    private NumberFormat getParseFormatter(Class type)
    {
        Locale locale = threadLocale.getLocale();

        boolean isInteger = isIntegerType(type);

        // We don't cache NumberFormat instances because they are not thread safe.
        // Perhaps we should turn this service into a perthread so that we can cache
        // (for the duration of a request)?

        return isInteger ? NumberFormat.getIntegerInstance(locale)
                         : NumberFormat.getNumberInstance(locale);

    }

    private NumberFormat getOutputFormatter(Class type)
    {
        Locale locale = threadLocale.getLocale();

        boolean isInteger = isIntegerType(type);

        if (!isInteger) return NumberFormat.getNumberInstance(locale);

        DecimalFormatSymbols symbols = getSymbols(locale);

        return new DecimalFormat(toString(symbols.getZeroDigit()), symbols);
    }

    public <T> String toClient(Class<T> type, T value)
    {
        // TODO: The default includes the comma (thousands grouping) seperators.
        // I don't think most people want that!

        return getOutputFormatter(type).format(value);
    }

    public <T> String getMessageKey(Class<T> type)
    {
        return isIntegerType(type)
               ? "integer-format-exception"
               : "number-format-exception";
    }

    private static String toString(char ch)
    {
        return String.valueOf(ch);
    }
}
