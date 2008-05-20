// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.util.AbstractMessages;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Implementation of {@link org.apache.tapestry5.ioc.Messages} based around a {@link java.util.ResourceBundle}.
 */
public class MessagesImpl extends AbstractMessages
{
    private final Map<String, String> properties = CollectionFactory.newCaseInsensitiveMap();

    /**
     * Finds the messages for a given Messages utility class. Strings the trailing "Messages" and replaces it with
     * "Strings" to form the base path. Loads the bundle using the default locale, and the class' class loader.
     *
     * @param forClass
     * @return Messages for the class
     */
    public static Messages forClass(Class forClass)
    {
        String className = forClass.getName();
        String stringsClassName = className.replaceAll("Messages$", "Strings");

        Locale locale = Locale.getDefault();

        ResourceBundle bundle = ResourceBundle.getBundle(stringsClassName, locale, forClass.getClassLoader());

        return new MessagesImpl(locale, bundle);
    }

    public MessagesImpl(Locale locale, ResourceBundle bundle)
    {
        super(locale);

        // Our best (threadsafe) chance to determine all the available keys.
        Enumeration<String> e = bundle.getKeys();
        while (e.hasMoreElements())
        {
            String key = e.nextElement();
            String value = bundle.getString(key);

            properties.put(key, value);
        }
    }

    @Override
    protected String valueForKey(String key)
    {
        return properties.get(key);
    }

}
