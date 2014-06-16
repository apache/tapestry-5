// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.tapestry5.ioc.util.CaseInsensitiveMap;
import org.apache.tapestry5.services.ComponentOverride;
import org.slf4j.Logger;

public class ComponentOverrideImpl implements ComponentOverride
{

    final Logger logger;
    final private Map<Class, Class> replacements;
    final private Map<String, Class> nameToClass;

    @SuppressWarnings("rawtypes")
    public ComponentOverrideImpl(Map<Class, Class> contributions, Logger logger)
    {

        this.logger = logger;
        this.replacements = Collections.unmodifiableMap(contributions);
        Map<String, Class> nameToClass = new HashMap<String, Class>();

        int maxLength = 0;

        for (Class<?> clasz : contributions.keySet())
        {

            final String name = clasz.getName();
            if (name.length() > maxLength) {
                maxLength = name.length();
            }
            nameToClass.put(name, contributions.get(clasz));

        }

        this.nameToClass = Collections.unmodifiableMap(nameToClass);
        
        if (replacements.size() > 0 && logger.isInfoEnabled())
        {
            
            StringBuilder builder = new StringBuilder(1000);
            final String format = "%" + maxLength + "s: %s\n";
            builder.append("Component replacements (including components, pages and mixins):\n");
            List<String> names = new ArrayList<String>(nameToClass.keySet());
            Collections.sort(names);
            
            for (String name : names) {
                builder.append(String.format(format, name, nameToClass.get(name).getName()));
            }
            
            logger.info(builder.toString());
            
        }

    }

    @Override
    public Map<Class, Class> getReplacements()
    {
        return replacements;
    }

    @Override
    public Class getReplacement(String className)
    {
        return nameToClass.get(className);
    }

}
