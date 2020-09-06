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
package org.apache.tapestry5.commons.internal;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.beaneditor.DataTypeConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.commons.internal.services.AnnotationDataTypeAnalyzer;
import org.apache.tapestry5.commons.internal.services.DefaultDataTypeAnalyzer;
import org.apache.tapestry5.commons.services.DataTypeAnalyzer;
import org.apache.tapestry5.commons.services.PropertyAdapter;

/**
 * Class that provides Tapestry's basic default data type analyzers.
 */
public class BasicDataTypeAnalyzers
{
    
    public static void contributeDataTypeAnalyzer(
            OrderedConfiguration<DataTypeAnalyzer> configuration,
            DataTypeAnalyzer defaultDataTypeAnalyzer) {
        configuration.add("Annotation", new AnnotationDataTypeAnalyzer());
        if (defaultDataTypeAnalyzer == null)
        {
            defaultDataTypeAnalyzer = createDefaultDataTypeAnalyzer();
        }
        configuration.add("Default", defaultDataTypeAnalyzer, "after:*");
    }

    public static DataTypeAnalyzer createDefaultDataTypeAnalyzer() 
    {
        DefaultDataTypeAnalyzerMappedConfiguration mappedConfiguration = new DefaultDataTypeAnalyzerMappedConfiguration();
        provideDefaultDataTypeAnalyzers(mappedConfiguration);
        return new CombinedDataTypeAnalyzer(new AnnotationDataTypeAnalyzer(), new DefaultDataTypeAnalyzer(mappedConfiguration.getMap()));
    }
    
    /**
     * Maps property types to data type names:
     * <ul>
     * <li>String --&gt; text
     * <li>Number --&gt; number
     * <li>Enum --&gt; enum
     * <li>Boolean --&gt; boolean
     * <li>Date --&gt; date
     * </ul>
     */
    public static void provideDefaultDataTypeAnalyzers(MappedConfiguration<Class, String> configuration)
    {
        // This is a special case contributed to avoid exceptions when a
        // property type can't be
        // matched. DefaultDataTypeAnalyzer converts the empty string to null.

        configuration.add(Object.class, "");

        configuration.add(String.class, DataTypeConstants.TEXT);
        configuration.add(Number.class, DataTypeConstants.NUMBER);
        configuration.add(Enum.class, DataTypeConstants.ENUM);
        configuration.add(Boolean.class, DataTypeConstants.BOOLEAN);
        configuration.add(Date.class, DataTypeConstants.DATE);
        configuration.add(Calendar.class, DataTypeConstants.CALENDAR);
    }

    final private static class DefaultDataTypeAnalyzerMappedConfiguration implements MappedConfiguration<Class, String> 
    {
        
        final Map<Class, String> map = new HashMap<Class, String>();

        @Override
        public void add(Class key, String value) {
            map.put(key, value);
        }

        @Override
        public void override(Class key, String value) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void addInstance(Class key, Class<? extends String> clazz) {
            throw new RuntimeException("Not implemented");            
        }

        @Override
        public void overrideInstance(Class key, Class<? extends String> clazz) {
            throw new RuntimeException("Not implemented");
        }

        public Map<Class, String> getMap() {
            return map;
        }
        
    }
    
    final private static class CombinedDataTypeAnalyzer implements DataTypeAnalyzer 
    {

        final private DataTypeAnalyzer[] analyzers;

        public CombinedDataTypeAnalyzer(DataTypeAnalyzer... analyzers) 
        {
        	this.analyzers = analyzers;
        }

        @Override
        public String identifyDataType(PropertyAdapter adapter) 
        {
        	String type = null;
        	for (DataTypeAnalyzer analyzer : analyzers) 
        	{
				type = analyzer.identifyDataType(adapter);
				if (type != null)
				{
					break;
				}
			}
        	return type;
        }

    }


}
