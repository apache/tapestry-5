// Copyright 2006, 2007, 2008, 2010, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.commons.services;

import org.apache.tapestry5.plastic.PlasticUtils;

/**
 * An immutable object that represents a mapping from one type to another. This is also the contribution type when
 * building the {@link org.apache.tapestry5.commons.services.TypeCoercer} service. Wraps a
 * {@link org.apache.tapestry5.commons.services.Coercion} object that performs the work with additional properties that
 * describe
 * the input and output types of the coercion, needed when searching for an appropriate coercion (or sequence of
 * coercions).
 *
 * @param <S>
 *         source (input) type
 * @param <T>
 *         target (output) type
 */
public final class CoercionTuple<S, T>
{
    private final Class<S> sourceType;

    private final Class<T> targetType;

    private final Coercion<S, T> coercion;
    
    private final Key key;

    /**
     * Wraps an arbitrary coercion with an implementation of toString() that identifies the source and target types.
     */
    private class CoercionWrapper<WS, WT> implements Coercion<WS, WT>
    {
        private final Coercion<WS, WT> coercion;

        public CoercionWrapper(Coercion<WS, WT> coercion)
        {
            this.coercion = coercion;
        }

        @Override
        public WT coerce(WS input)
        {
            return coercion.coerce(input);
        }

        @Override
        public String toString()
        {
            return String.format("%s --> %s", convert(sourceType), convert(targetType));
        }
    }

    private String convert(Class type)
    {
        if (Void.class.equals(type))
            return "null";

        String name = PlasticUtils.toTypeName(type);

        int dotx = name.lastIndexOf('.');

        // Strip off a package name of "java.lang"

        if (dotx > 0 && name.substring(0, dotx).equals("java.lang"))
            return name.substring(dotx + 1);

        return name;
    }

    /**
     * Standard constructor, which defaults wrap to true.
     */
    public CoercionTuple(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercion)
    {
        this(sourceType, targetType, coercion, true);
    }

    /**
     * Convenience constructor to help with generics.
     *
     * @since 5.2.0
     */
    public static <S, T> CoercionTuple<S, T> create(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercion)
    {
        return new CoercionTuple<S, T>(sourceType, targetType, coercion);
    }

    /**
     * Internal-use constructor.
     *
     * @param sourceType
     *         the source (or input) type of the coercion, may be Void.class to indicate a coercion from null
     * @param targetType
     *         the target (or output) type of the coercion
     * @param coercion
     *         the object that performs the coercion
     * @param wrap
     *         if true, the coercion is wrapped to provide a useful toString()
     */
    @SuppressWarnings("unchecked")
    public CoercionTuple(Class<S> sourceType, Class<T> targetType, Coercion<S, T> coercion, boolean wrap)
    {
        assert sourceType != null;
        assert targetType != null;
        assert coercion != null;

        this.sourceType = PlasticUtils.toWrapperType(sourceType);
        this.targetType = PlasticUtils.toWrapperType(targetType);
        this.coercion = wrap ? new CoercionWrapper<S, T>(coercion) : coercion;
        this.key = new Key();
    }

    @Override
    public String toString()
    {
        return coercion.toString();
    }

    public Coercion<S, T> getCoercion()
    {
        return coercion;
    }

    public Class<S> getSourceType()
    {
        return sourceType;
    }

    public Class<T> getTargetType()
    {
        return targetType;
    }
    
    public Key getKey() 
    {
        return key;
    }

    /**
     * Class that represents the key to be used to the mapped configuration of the
     * {@link TypeCoercer} service.
     */
    public final class Key 
    {
        
        protected Class<S> getSourceType()
        {
            return sourceType;
        }

        protected Class<T> getTargetType()
        {
            return targetType;
        }
        
        @Override
        public String toString() {
            return String.format("%s -> %s", sourceType.getName(), targetType.getName());
        }
    
        @Override
        public int hashCode() 
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
            result = prime * result + ((targetType == null) ? 0 : targetType.hashCode());
            return result;
        }
    
        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            Key other = (Key) obj;
            if (sourceType == null) 
            {
                if (other.getSourceType() != null)
                    return false;
            } else if (!sourceType.equals(other.getSourceType()))
                return false;
            if (targetType == null) 
            {
                if (other.getTargetType() != null)
                    return false;
            } else if (!targetType.equals(other.getTargetType()))
                return false;
            return true;
        }
        
    }

}
