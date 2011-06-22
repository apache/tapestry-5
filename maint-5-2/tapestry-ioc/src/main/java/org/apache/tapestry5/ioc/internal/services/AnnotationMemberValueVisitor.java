// Copyright 2010 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.internal.services;

import java.lang.reflect.Method;
import java.util.List;

import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.MemberValueVisitor;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

public class AnnotationMemberValueVisitor implements MemberValueVisitor
{
    private final ConstPool constPool;

    private final CtClassSource classSource;

    private final Object value;

    public AnnotationMemberValueVisitor(final ConstPool constPool, final CtClassSource classSource, final Object value)
    {
        this.constPool = constPool;
        this.classSource = classSource;
        this.value = value;
    }

    public void visitAnnotationMemberValue(final AnnotationMemberValue mb)
    {
        Class annotationType = getClass(value);
        
        final Method[] methods = annotationType.getDeclaredMethods();

        for (final Method method : methods)
        {
            try
            {
                final Object result = method.invoke(value);

                final MemberValue memberValue = Annotation.createMemberValue(
                        this.constPool,
                        this.classSource.toCtClass(result.getClass()));

                memberValue.accept(new AnnotationMemberValueVisitor(this.constPool, this.classSource, result));
                
                mb.getValue().addMemberValue(method.getName(), memberValue);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        
    }

    public void visitArrayMemberValue(final ArrayMemberValue mb)
    {
        final Object[] array = (Object[]) this.value;

        final List<MemberValue> members = CollectionFactory.newList();
        
        for (final Object object : array)
        {
            try
            {
                final MemberValue memberValue = Annotation.createMemberValue(
                        this.constPool,
                        this.classSource.toCtClass(getClass(object)));

                memberValue.accept(new AnnotationMemberValueVisitor(this.constPool, this.classSource, object));

                members.add(memberValue);
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        mb.setValue(members.toArray(new MemberValue[] {}));
    }
    
    private Class getClass(Object object)
    {
        if(object instanceof java.lang.annotation.Annotation)
        {
            return ((java.lang.annotation.Annotation)object).annotationType();
        }
        
        return object.getClass();
    }

    public void visitBooleanMemberValue(final BooleanMemberValue mb)
    {
        mb.setValue((Boolean) this.value);
    }

    public void visitByteMemberValue(final ByteMemberValue mb)
    {
        mb.setValue((Byte) this.value);
    }

    public void visitCharMemberValue(final CharMemberValue mb)
    {
        mb.setValue((Character) this.value);
    }

    public void visitDoubleMemberValue(final DoubleMemberValue mb)
    {
        mb.setValue((Double) this.value);
    }

    public void visitEnumMemberValue(final EnumMemberValue mb)
    {
        final Enum enumeration = (Enum) this.value;
        
        final Class type = enumeration.getDeclaringClass();
        mb.setType(type.getName());
        mb.setValue(enumeration.name());
    }

    public void visitFloatMemberValue(final FloatMemberValue mb)
    {
        mb.setValue((Float) this.value);
    }

    public void visitIntegerMemberValue(final IntegerMemberValue mb)
    {
        mb.setValue((Integer) this.value);
    }

    public void visitLongMemberValue(final LongMemberValue mb)
    {
        mb.setValue((Long) this.value);
    }

    public void visitShortMemberValue(final ShortMemberValue mb)
    {
        mb.setValue((Short) this.value);
    }

    public void visitStringMemberValue(final StringMemberValue mb)
    {
        mb.setValue((String) this.value);
    }

    public void visitClassMemberValue(final ClassMemberValue mb)
    {
        mb.setValue(((Class) this.value).getName());
    }

}
