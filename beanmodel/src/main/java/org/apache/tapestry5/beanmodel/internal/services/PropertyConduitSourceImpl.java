// Copyright 2007-2013 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal.services;

import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.DECIMAL;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.DEREF;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.FALSE;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.IDENTIFIER;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.INTEGER;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.INVOKE;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.LIST;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.MAP;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.NOT;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.NULL;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.RANGEOP;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.SAFEDEREF;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.STRING;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.THIS;
import static org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser.TRUE;

import org.apache.tapestry5.ioc.annotations.ComponentLayer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;
import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.PropertyConduit2;
import org.apache.tapestry5.beanmodel.internal.InternalPropertyConduit;
import org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionLexer;
import org.apache.tapestry5.beanmodel.internal.antlr.PropertyExpressionParser;
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.internal.NullAnnotationProvider;
import org.apache.tapestry5.commons.internal.services.StringInterner;
import org.apache.tapestry5.commons.internal.util.GenericsUtils;
import org.apache.tapestry5.commons.internal.util.InternalCommonsUtils;
import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.commons.util.IntegerRange;
import org.apache.tapestry5.commons.util.MultiKey;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.plastic.Condition;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;

public class PropertyConduitSourceImpl implements PropertyConduitSource
{
    static class ConduitMethods
    {
        private static final MethodDescription GET = getMethodDescription(PropertyConduit.class, "get", Object.class);

        private static final MethodDescription SET = getMethodDescription(PropertyConduit.class, "set", Object.class,
                Object.class);

        private static final MethodDescription GET_PROPERTY_TYPE = getMethodDescription(PropertyConduit.class,
                "getPropertyType");

        private static final MethodDescription GET_PROPERTY_GENERIC_TYPE = getMethodDescription(PropertyConduit2.class,
                "getPropertyGenericType");
        
        private static final MethodDescription GET_PROPERTY_NAME = getMethodDescription(InternalPropertyConduit.class,
                "getPropertyName");
        
        private static final MethodDescription GET_ANNOTATION = getMethodDescription(AnnotationProvider.class,
                "getAnnotation", Class.class);

    }

    static class DelegateMethods
    {
        static final Method INVERT = getMethod(PropertyConduitDelegate.class, "invert", Object.class);

        static final Method RANGE = getMethod(PropertyConduitDelegate.class, "range", int.class, int.class);

        static final Method COERCE = getMethod(PropertyConduitDelegate.class, "coerce", Object.class, Class.class);
    }

    static class ArrayListMethods
    {
        static final Method ADD = getMethod(ArrayList.class, "add", Object.class);
    }

    static class HashMapMethods
    {
        static final Method PUT = getMethod(HashMap.class, "put", Object.class, Object.class);
    }

    private static InstructionBuilderCallback RETURN_NULL = new InstructionBuilderCallback()
    {
        public void doBuild(InstructionBuilder builder)
        {
            builder.loadNull().returnResult();
        }
    };

    private static final String[] SINGLE_OBJECT_ARGUMENT = new String[]
            {Object.class.getName()};

    @SuppressWarnings("unchecked")
    private static Method getMethod(Class containingClass, String name, Class... parameterTypes)
    {
        try
        {
            return containingClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private static MethodDescription getMethodDescription(Class containingClass, String name, Class... parameterTypes)
    {
        return new MethodDescription(getMethod(containingClass, name, parameterTypes));
    }

    private final AnnotationProvider nullAnnotationProvider = new NullAnnotationProvider();

    /**
     * How are null values in intermdiate terms to be handled?
     */
    private enum NullHandling
    {
        /**
         * Add code to check for null and throw exception if null.
         */
        FORBID,

        /**
         * Add code to check for null and short-circuit (i.e., the "?."
         * safe-dereference operator)
         */
        ALLOW
    }

    /**
     * One term in an expression. Expressions start with some root type and each term advances
     * to a new type.
     */
    private class Term
    {
        /**
         * The generic type of the term.
         */
        final Type type;

        final Class genericType;

        /**
         * Describes the term, for use in error messages.
         */
        final String description;

        final AnnotationProvider annotationProvider;

        /**
         * Callback that will implement the term.
         */
        final InstructionBuilderCallback callback;

        Term(Type type, Class genericType, String description, AnnotationProvider annotationProvider,
             InstructionBuilderCallback callback)
        {
            this.type = type;
            this.genericType = genericType;
            this.description = description;
            this.annotationProvider = annotationProvider;
            this.callback = callback;
        }

        Term(Type type, String description, AnnotationProvider annotationProvider, InstructionBuilderCallback callback)
        {
            this(type, GenericsUtils.asClass(type), description, annotationProvider, callback);
        }

        Term(Type type, String description, InstructionBuilderCallback callback)
        {
            this(type, description, null, callback);
        }

        /**
         * Returns a clone of this Term with a new callback.
         */
        Term withCallback(InstructionBuilderCallback newCallback)
        {
            return new Term(type, genericType, description, annotationProvider, newCallback);
        }
    }

    private final PropertyAccess access;

    private final PlasticProxyFactory proxyFactory;

    private final TypeCoercer typeCoercer;

    private final StringInterner interner;

    /**
     * Keyed on combination of root class and expression.
     */
    private final Map<MultiKey, PropertyConduit> cache = CollectionFactory.newConcurrentMap();

    private final Invariant invariantAnnotation = new Invariant()
    {
        public Class<? extends Annotation> annotationType()
        {
            return Invariant.class;
        }
    };

    private final AnnotationProvider invariantAnnotationProvider = new AnnotationProvider()
    {
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
        {
            if (annotationClass == Invariant.class)
                return annotationClass.cast(invariantAnnotation);

            return null;
        }
    };

    private final PropertyConduit literalTrue;

    private final PropertyConduit literalFalse;

    private final PropertyConduit literalNull;

    private final PropertyConduitDelegate sharedDelegate;

    /**
     * Encapsulates the process of building a PropertyConduit instance from an
     * expression, as an {@link PlasticClassTransformer}.
     */
    class PropertyConduitBuilder implements PlasticClassTransformer
    {
        private final Class rootType;

        private final String expression;

        private final Tree tree;

        private Class conduitPropertyType;

        private Type conduitPropertyGenericType;

        private String conduitPropertyName;

        private AnnotationProvider annotationProvider = nullAnnotationProvider;

        private PlasticField delegateField;

        private PlasticClass plasticClass;

        private PlasticMethod getRootMethod, navMethod;

        PropertyConduitBuilder(Class rootType, String expression, Tree tree)
        {
            this.rootType = rootType;
            this.expression = expression;
            this.tree = tree;
        }

        public void transform(PlasticClass plasticClass)
        {
            this.plasticClass = plasticClass;

            // Create the various methods; also determine the conduit's property type, property name and identify
            // the annotation provider.

            implementNavMethodAndAccessors();

            implementOtherMethods();

            plasticClass.addToString(String.format("PropertyConduit[%s %s]", rootType.getName(), expression));
        }

        private void implementOtherMethods()
        {
            PlasticField annotationProviderField = plasticClass.introduceField(AnnotationProvider.class,
                    "annotationProvider").inject(annotationProvider);

            plasticClass.introduceMethod(ConduitMethods.GET_ANNOTATION).delegateTo(annotationProviderField);

            plasticClass.introduceMethod(ConduitMethods.GET_PROPERTY_NAME, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadConstant(conduitPropertyName).returnResult();
                }
            });

            final PlasticField propertyTypeField = plasticClass.introduceField(Class.class, "propertyType").inject(
                    conduitPropertyType);

            plasticClass.introduceMethod(ConduitMethods.GET_PROPERTY_TYPE, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadThis().getField(propertyTypeField).returnResult();
                }
            });

            final PlasticField propertyGenericTypeField = plasticClass.introduceField(Type.class, "propertyGenericType").inject(
                    conduitPropertyGenericType);

            plasticClass.introduceMethod(ConduitMethods.GET_PROPERTY_GENERIC_TYPE, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadThis().getField(propertyGenericTypeField).returnResult();
                }
            });
        }

        /**
         * Creates a method that does a conversion from Object to the expected root type, with
         * a null check.
         */
        private void implementGetRoot()
        {
            getRootMethod = plasticClass.introducePrivateMethod(PlasticUtils.toTypeName(rootType), "getRoot",
                    SINGLE_OBJECT_ARGUMENT, null);

            getRootMethod.changeImplementation(new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadArgument(0).dupe().when(Condition.NULL, new InstructionBuilderCallback()
                    {
                        public void doBuild(InstructionBuilder builder)
                        {
                            builder.throwException(NullPointerException.class,
                                    String.format("Root object of property expression '%s' is null.", expression));
                        }
                    });

                    builder.checkcast(rootType).returnResult();
                }
            });
        }

        private boolean isLeaf(Tree node)
        {
            int type = node.getType();

            return type != DEREF && type != SAFEDEREF;
        }

        private void implementNavMethodAndAccessors()
        {
            implementGetRoot();

            // First, create the navigate method.

            final List<InstructionBuilderCallback> callbacks = CollectionFactory.newList();

            Type activeType = rootType;

            Tree node = tree;

            while (!isLeaf(node))
            {
                Term term = analyzeDerefNode(activeType, node);

                callbacks.add(term.callback);

                activeType = term.type;

                // Second term is the continuation, possibly another chained
                // DEREF, etc.
                node = node.getChild(1);
            }

            Class activeClass = GenericsUtils.asClass(activeType);

            if (callbacks.isEmpty())
            {
                navMethod = getRootMethod;
            } else
            {
                navMethod = plasticClass.introducePrivateMethod(PlasticUtils.toTypeName(activeClass), "navigate",
                        SINGLE_OBJECT_ARGUMENT, null);

                navMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().loadArgument(0).invokeVirtual(getRootMethod);

                        for (InstructionBuilderCallback callback : callbacks)
                        {
                            callback.doBuild(builder);
                        }

                        builder.returnResult();
                    }
                });
            }

            implementAccessors(activeType, node);
        }

        private void implementAccessors(Type activeType, Tree node)
        {
            switch (node.getType())
            {
                case IDENTIFIER:

                    implementPropertyAccessors(activeType, node);

                    return;

                case INVOKE:

                    // So, at this point, we have the navigation method written
                    // and it covers all but the terminal
                    // de-reference. node is an IDENTIFIER or INVOKE. We're
                    // ready to use the navigation
                    // method to implement get() and set().

                    implementMethodAccessors(activeType, node);

                    return;

                case RANGEOP:

                    // As currently implemented, RANGEOP can only appear as the
                    // top level, which
                    // means we didn't need the navigate method after all.

                    implementRangeOpGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = IntegerRange.class;
                    conduitPropertyGenericType = IntegerRange.class;

                    return;

                case LIST:

                    implementListGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = List.class;
                    conduitPropertyGenericType = List.class;
                    
                    return;

                case MAP:
                    implementMapGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = Map.class;
                    conduitPropertyGenericType = Map.class;

                    return;


                case NOT:
                    implementNotOpGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = boolean.class;
                    conduitPropertyGenericType = boolean.class;

                    return;

                default:
                    throw unexpectedNodeType(node, IDENTIFIER, INVOKE, RANGEOP, LIST, NOT);
            }
        }

        public void implementMethodAccessors(final Type activeType, final Tree invokeNode)
        {
            final Term term = buildInvokeTerm(activeType, invokeNode);

            implementNoOpSetter();

            conduitPropertyName = term.description;
            conduitPropertyType = term.genericType;
            conduitPropertyGenericType = term.genericType;
            annotationProvider = term.annotationProvider;

            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    term.callback.doBuild(builder);

                    boxIfPrimitive(builder, conduitPropertyType);

                    builder.returnResult();
                }
            });

            implementNoOpSetter();
        }

        public void implementPropertyAccessors(Type activeType, Tree identifierNode)
        {
            String propertyName = identifierNode.getText();

            PropertyAdapter adapter = findPropertyAdapter(activeType, propertyName);

            conduitPropertyName = propertyName;
            conduitPropertyType = adapter.getType();
            conduitPropertyGenericType = getGenericType(adapter);
            annotationProvider = adapter;

            implementGetter(adapter);
            implementSetter(adapter);
        }

        private Type getGenericType(PropertyAdapter adapter)
        {
            Type genericType = null;
            if (adapter.getField() != null)
            {
                genericType = adapter.getField().getGenericType();
            }
            else if (adapter.getReadMethod() != null)
            {
                genericType = adapter.getReadMethod().getGenericReturnType(); 
            }
            else if (adapter.getWriteMethod() != null)
            {
                genericType = adapter.getWriteMethod().getGenericParameterTypes()[0];
            }
            else
            {
                throw new RuntimeException("Could not find accessor for property " + adapter.getName());
            }
            
            return genericType == null ? adapter.getType() : genericType;
        }

        private void implementSetter(PropertyAdapter adapter)
        {
            if (adapter.getWriteMethod() != null)
            {
                implementSetter(adapter.getWriteMethod());
                return;
            }

            if (adapter.getField() != null && adapter.isUpdate())
            {
                implementSetter(adapter.getField());
                return;
            }

            implementNoOpMethod(ConduitMethods.SET, "Expression '%s' for class %s is read-only.", expression,
                    rootType.getName());
        }

        private boolean isStatic(Member member)
        {
            return Modifier.isStatic(member.getModifiers());
        }

        private void implementSetter(final Field field)
        {
            if (isStatic(field))
            {
                plasticClass.introduceMethod(ConduitMethods.SET, new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadArgument(1).castOrUnbox(PlasticUtils.toTypeName(field.getType()));

                        builder.putStaticField(field.getDeclaringClass().getName(), field.getName(), field.getType());

                        builder.returnResult();
                    }
                });

                return;
            }

            plasticClass.introduceMethod(ConduitMethods.SET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    builder.loadArgument(1).castOrUnbox(PlasticUtils.toTypeName(field.getType()));

                    builder.putField(field.getDeclaringClass().getName(), field.getName(), field.getType());

                    builder.returnResult();
                }
            });
        }

        private void implementSetter(final Method writeMethod)
        {
            plasticClass.introduceMethod(ConduitMethods.SET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    Class propertyType = writeMethod.getParameterTypes()[0];
                    String propertyTypeName = PlasticUtils.toTypeName(propertyType);

                    builder.loadArgument(1).castOrUnbox(propertyTypeName);

                    builder.invoke(writeMethod);

                    builder.returnResult();
                }
            });
        }

        private void implementGetter(PropertyAdapter adapter)
        {
            if (adapter.getReadMethod() != null)
            {
                implementGetter(adapter.getReadMethod());
                return;
            }

            if (adapter.getField() != null)
            {
                implementGetter(adapter.getField());
                return;
            }

            implementNoOpMethod(ConduitMethods.GET, "Expression '%s' for class %s is write-only.", expression,
                    rootType.getName());
        }

        private void implementGetter(final Field field)
        {
            if (isStatic(field))
            {
                plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.getStaticField(field.getDeclaringClass().getName(), field.getName(), field.getType());

                        // Cast not necessary here since the return type of get() is Object

                        boxIfPrimitive(builder, field.getType());

                        builder.returnResult();
                    }
                });

                return;
            }

            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    builder.getField(field.getDeclaringClass().getName(), field.getName(), field.getType());

                    // Cast not necessary here since the return type of get() is Object

                    boxIfPrimitive(builder, field.getType());

                    builder.returnResult();
                }
            });
        }

        private void implementGetter(final Method readMethod)
        {
            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    invokeMethod(builder, readMethod, null, 0);

                    boxIfPrimitive(builder, conduitPropertyType);

                    builder.returnResult();
                }
            });
        }

        private void implementRangeOpGetter(final Tree rangeNode)
        {
            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    // Put the delegate on top of the stack

                    builder.loadThis().getField(getDelegateField());

                    invokeMethod(builder, DelegateMethods.RANGE, rangeNode, 0);

                    builder.returnResult();
                }
            });
        }

        /**
         * @param node
         *         subexpression to invert
         */
        private void implementNotOpGetter(final Tree node)
        {
            // Implement get() as navigate, then do a method invocation based on node
            // then, then pass (wrapped) result to delegate.invert()

            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    Type expressionType = implementNotExpression(builder, node);

                    // Yes, we know this will always be the case, for now.

                    boxIfPrimitive(builder, expressionType);

                    builder.returnResult();
                }
            });
        }

        /**
         * The first part of any implementation of get() or set(): invoke the navigation method
         * and if the result is null, return immediately.
         */
        private void invokeNavigateMethod(InstructionBuilder builder)
        {
            builder.loadThis().loadArgument(0).invokeVirtual(navMethod);

            builder.dupe().when(Condition.NULL, RETURN_NULL);
        }

        /**
         * Uses the builder to add instructions for a subexpression.
         *
         * @param builder
         *         used to add instructions
         * @param activeType
         *         type of value on top of the stack when this code will execute, or null if no value on stack
         * @param node
         *         defines the expression
         * @return the expression type
         */
        private Type implementSubexpression(InstructionBuilder builder, Type activeType, Tree node)
        {
            Term term;

            while (true)
            {
                switch (node.getType())
                {
                    case IDENTIFIER:
                    case INVOKE:

                        if (activeType == null)
                        {
                            invokeGetRootMethod(builder);

                            activeType = rootType;
                        }

                        term = buildTerm(activeType, node);

                        term.callback.doBuild(builder);

                        return term.type;

                    case INTEGER:

                        builder.loadConstant(new Long(node.getText()));

                        return long.class;

                    case DECIMAL:

                        builder.loadConstant(new Double(node.getText()));

                        return double.class;

                    case STRING:

                        builder.loadConstant(node.getText());

                        return String.class;

                    case DEREF:
                    case SAFEDEREF:

                        if (activeType == null)
                        {
                            invokeGetRootMethod(builder);

                            activeType = rootType;
                        }

                        term = analyzeDerefNode(activeType, node);

                        term.callback.doBuild(builder);

                        activeType = GenericsUtils.asClass(term.type);

                        node = node.getChild(1);

                        break;

                    case TRUE:
                    case FALSE:

                        builder.loadConstant(node.getType() == TRUE ? 1 : 0);

                        return boolean.class;

                    case LIST:

                        return implementListConstructor(builder, node);

                    case MAP:
                        return implementMapConstructor(builder, node);

                    case NOT:

                        return implementNotExpression(builder, node);

                    case THIS:

                        invokeGetRootMethod(builder);

                        return rootType;

                    case NULL:

                        builder.loadNull();

                        return Void.class;

                    default:
                        throw unexpectedNodeType(node, TRUE, FALSE, INTEGER, DECIMAL, STRING, DEREF, SAFEDEREF,
                                IDENTIFIER, INVOKE, LIST, NOT, THIS, NULL);
                }
            }
        }

        public void invokeGetRootMethod(InstructionBuilder builder)
        {
            builder.loadThis().loadArgument(0).invokeVirtual(getRootMethod);
        }

        private void implementListGetter(final Tree listNode)
        {
            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    implementListConstructor(builder, listNode);

                    builder.returnResult();
                }
            });
        }

        private Type implementListConstructor(InstructionBuilder builder, Tree listNode)
        {
            // First, create an empty instance of ArrayList

            int count = listNode.getChildCount();

            builder.newInstance(ArrayList.class);
            builder.dupe().loadConstant(count).invokeConstructor(ArrayList.class, int.class);

            for (int i = 0; i < count; i++)
            {
                builder.dupe(); // the ArrayList

                Type expressionType = implementSubexpression(builder, null, listNode.getChild(i));

                boxIfPrimitive(builder, GenericsUtils.asClass(expressionType));

                // Add the value to the array, then pop off the returned boolean
                builder.invoke(ArrayListMethods.ADD).pop();
            }

            return ArrayList.class;
        }

        private void implementMapGetter(final Tree mapNode)
        {
            plasticClass.introduceMethod(ConduitMethods.GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    implementMapConstructor(builder, mapNode);

                    builder.returnResult();
                }
            });
        }

        private Type implementMapConstructor(InstructionBuilder builder, Tree mapNode)
        {
            int count = mapNode.getChildCount();
            builder.newInstance(HashMap.class);
            builder.dupe().loadConstant(count).invokeConstructor(HashMap.class, int.class);

            for (int i = 0; i < count; i += 2)
            {
                builder.dupe();

                //build the key:
                Type keyType = implementSubexpression(builder, null, mapNode.getChild(i));
                boxIfPrimitive(builder, GenericsUtils.asClass(keyType));

                //and the value:
                Type valueType = implementSubexpression(builder, null, mapNode.getChild(i + 1));
                boxIfPrimitive(builder, GenericsUtils.asClass(valueType));

                //put the value into the array, then pop off the returned object.
                builder.invoke(HashMapMethods.PUT).pop();

            }

            return HashMap.class;
        }


        private void implementNoOpSetter()
        {
            implementNoOpMethod(ConduitMethods.SET, "Expression '%s' for class %s is read-only.", expression,
                    rootType.getName());
        }

        public void implementNoOpMethod(MethodDescription method, String format, Object... arguments)
        {
            final String message = String.format(format, arguments);

            plasticClass.introduceMethod(method).changeImplementation(new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.throwException(RuntimeException.class, message);
                }
            });
        }

        /**
         * Invokes a method that may take parameters. The children of the invokeNode are subexpressions
         * to be evaluated, and potentially coerced, so that they may be passed to the method.
         *
         * @param builder
         *         constructs code
         * @param method
         *         method to invoke
         * @param node
         *         INVOKE or RANGEOP node
         * @param childOffset
         *         offset within the node to the first child expression (1 in an INVOKE node because the
         *         first child is the method name, 0 in a RANGEOP node)
         */
        private void invokeMethod(InstructionBuilder builder, Method method, Tree node, int childOffset)
        {
            // We start with the target object for the method on top of the stack.
            // Next, we have to push each method parameter, which may include boxing/deboxing
            // and coercion. Once the code is in good shape, there's a lot of room to optimize
            // the bytecode (a bit too much boxing/deboxing occurs, as well as some unnecessary
            // trips through TypeCoercer). We might also want to have a local variable to store
            // the root object (result of getRoot()).

            Class[] parameterTypes = method.getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++)
            {
                Type expressionType = implementSubexpression(builder, null, node.getChild(i + childOffset));

                // The value left on the stack is not primitive, and expressionType represents
                // its real type.

                Class parameterType = parameterTypes[i];

                if (!parameterType.isAssignableFrom(GenericsUtils.asClass(expressionType)))
                {
                    boxIfPrimitive(builder, expressionType);

                    builder.loadThis().getField(getDelegateField());
                    builder.swap().loadTypeConstant(PlasticUtils.toWrapperType(parameterType));
                    builder.invoke(DelegateMethods.COERCE);

                    if (parameterType.isPrimitive())
                    {
                        builder.castOrUnbox(parameterType.getName());
                    } else
                    {
                        builder.checkcast(parameterType);
                    }
                }

                // And that should leave an object of the correct type on the stack,
                // ready for the method invocation.
            }

            // Now the target object and all parameters are in place.

            builder.invoke(method.getDeclaringClass(), method.getReturnType(), method.getName(),
                    method.getParameterTypes());
        }

        /**
         * Analyzes a DEREF or SAFEDEREF node, proving back a term that identifies its type and provides a callback to
         * peform the dereference.
         *
         * @return a term indicating the type of the expression to this point, and a {@link InstructionBuilderCallback}
         *         to advance the evaluation of the expression form the previous value to the current
         */
        private Term analyzeDerefNode(Type activeType, Tree node)
        {
            // The first child is the term.

            Tree term = node.getChild(0);

            boolean allowNull = node.getType() == SAFEDEREF;

            return buildTerm(activeType, term, allowNull ? NullHandling.ALLOW : NullHandling.FORBID);
        }

        private Term buildTerm(Type activeType, Tree term, final NullHandling nullHandling)
        {
            assertNodeType(term, IDENTIFIER, INVOKE);

            final Term simpleTerm = buildTerm(activeType, term);

            if (simpleTerm.genericType.isPrimitive())
                return simpleTerm;

            return simpleTerm.withCallback(new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    simpleTerm.callback.doBuild(builder);

                    builder.dupe().when(Condition.NULL, new InstructionBuilderCallback()
                    {
                        public void doBuild(InstructionBuilder builder)
                        {
                            switch (nullHandling)
                            {
                                // It is necessary to load a null onto the stack (even if there's already one
                                // there) because of the verifier. It sees the return when the stack contains an
                                // intermediate value (along the navigation chain) and thinks the method is
                                // returning a value of the wrong type.

                                case ALLOW:
                                    builder.loadNull().returnResult();

                                case FORBID:

                                    builder.loadConstant(simpleTerm.description);
                                    builder.loadConstant(expression);
                                    builder.loadArgument(0);

                                    builder.invokeStatic(PropertyConduitSourceImpl.class, NullPointerException.class,
                                            "nullTerm", String.class, String.class, Object.class);
                                    builder.throwException();

                                    break;

                            }
                        }
                    });
                }
            });
        }

        private void assertNodeType(Tree node, int... expected)
        {
            int type = node.getType();

            for (int e : expected)
            {
                if (type == e)
                    return;
            }

            throw unexpectedNodeType(node, expected);
        }

        private RuntimeException unexpectedNodeType(Tree node, int... expected)
        {
            List<String> tokenNames = CollectionFactory.newList();

            for (int i = 0; i < expected.length; i++)
                tokenNames.add(PropertyExpressionParser.tokenNames[expected[i]]);

            String message = String.format("Node %s was type %s, but was expected to be (one of) %s.",
                    node.toStringTree(), PropertyExpressionParser.tokenNames[node.getType()],
                    InternalCommonsUtils.joinSorted(tokenNames));

            return new RuntimeException(message);
        }

        private Term buildTerm(Type activeType, Tree termNode)
        {
            switch (termNode.getType())
            {
                case INVOKE:

                    return buildInvokeTerm(activeType, termNode);

                case IDENTIFIER:

                    return buildPropertyAccessTerm(activeType, termNode);

                default:
                    throw unexpectedNodeType(termNode, INVOKE, IDENTIFIER);
            }
        }

        private Term buildPropertyAccessTerm(Type activeType, Tree termNode)
        {
            String propertyName = termNode.getText();

            PropertyAdapter adapter = findPropertyAdapter(activeType, propertyName);

            // Prefer the accessor over the field

            if (adapter.getReadMethod() != null)
            {
                return buildGetterMethodAccessTerm(activeType, propertyName,
                        adapter.getReadMethod());
            }

            if (adapter.getField() != null)
            {
                return buildPublicFieldAccessTerm(activeType, propertyName,
                        adapter.getField());
            }

            throw new RuntimeException(String.format(
                    "Property '%s' of class %s is not readable (it has no read accessor method).", adapter.getName(),
                    adapter.getBeanType().getName()));
        }

        public PropertyAdapter findPropertyAdapter(Type activeType, String propertyName)
        {
            Class activeClass = GenericsUtils.asClass(activeType);

            ClassPropertyAdapter classAdapter = access.getAdapter(activeClass);
            PropertyAdapter adapter = classAdapter.getPropertyAdapter(propertyName);

            if (adapter == null)
            {
                final List<String> names = classAdapter.getPropertyNames();
                final String className = activeClass.getName();
                throw new UnknownValueException(String.format(
                        "Class %s does not contain a property (or public field) named '%s'.", className, propertyName),
                        new AvailableValues("Properties (and public fields)", names));
            }
            return adapter;
        }

        private Term buildGetterMethodAccessTerm(final Type activeType, String propertyName, final Method readMethod)
        {
            Type returnType = GenericsUtils.extractActualType(activeType, readMethod);

            return new Term(returnType, propertyName, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeMethod(builder, readMethod, null, 0);

                    Type genericType = GenericsUtils.extractActualType(activeType, readMethod);

                    castToGenericType(builder, readMethod.getReturnType(), genericType);
                }
            });
        }

        private Term buildPublicFieldAccessTerm(Type activeType, String propertyName, final Field field)
        {
            final Type fieldType = GenericsUtils.extractActualType(activeType, field);

            return new Term(fieldType, propertyName, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    Class rawFieldType = field.getType();

                    String rawTypeName = PlasticUtils.toTypeName(rawFieldType);
                    String containingClassName = field.getDeclaringClass().getName();
                    String fieldName = field.getName();

                    if (isStatic(field))
                    {
                        // We've gone to the trouble of loading the root object, or navigated to some other object,
                        // but we don't need or want the instance, since it's a static field we're accessing.
                        // Ideally, we would optimize this, and only generate and invoke the getRoot() and nav() methods as needed, but
                        // access to public fields is relatively rare, and the cost is just the unused bytecode.

                        builder.pop();

                        builder.getStaticField(containingClassName, fieldName, rawTypeName);

                    } else
                    {
                        builder.getField(containingClassName, fieldName, rawTypeName);
                    }

                    castToGenericType(builder, rawFieldType, fieldType);
                }

            });
        }

        /**
         * Casts the results of a field read or method invocation based on generic information.
         *
         * @param builder
         *         used to add instructions
         * @param rawType
         *         the simple type (often Object) of the field (or method return type)
         * @param genericType
         *         the generic Type, from which parameterizations can be determined
         */
        private void castToGenericType(InstructionBuilder builder, Class rawType, final Type genericType)
        {
            if (!genericType.equals(rawType))
            {
                Class castType = GenericsUtils.asClass(genericType);
                builder.checkcast(castType);
            }
        }

        private Term buildInvokeTerm(final Type activeType, final Tree invokeNode)
        {
            String methodName = invokeNode.getChild(0).getText();

            int parameterCount = invokeNode.getChildCount() - 1;

            Class activeClass = GenericsUtils.asClass(activeType);

            final Method method = findMethod(activeClass, methodName, parameterCount);

            if (method.getReturnType().equals(void.class))
                throw new RuntimeException(String.format("Method %s.%s() returns void.", activeClass.getName(),
                        methodName));

            Type returnType = GenericsUtils.extractActualType(activeType, method);

            return new Term(returnType, toUniqueId(method), InternalCommonsUtils.toAnnotationProvider(method), new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeMethod(builder, method, invokeNode, 1);

                    Type genericType = GenericsUtils.extractActualType(activeType, method);

                    castToGenericType(builder, method.getReturnType(), genericType);
                }
            }
            );
        }

        private Method findMethod(Class activeType, String methodName, int parameterCount)
        {
            Class searchType = activeType;

            while (true)
            {

                for (Method method : searchType.getMethods())
                {
                    if (method.getParameterTypes().length == parameterCount
                            && method.getName().equalsIgnoreCase(methodName))
                        return method;
                }

                // TAP5-330
                if (searchType != Object.class)
                {
                    searchType = Object.class;
                } else
                {
                    throw new RuntimeException(String.format("Class %s does not contain a public method named '%s()'.",
                            activeType.getName(), methodName));
                }
            }
        }

        public void boxIfPrimitive(InstructionBuilder builder, Type termType)
        {
            boxIfPrimitive(builder, GenericsUtils.asClass(termType));
        }

        public void boxIfPrimitive(InstructionBuilder builder, Class termType)
        {
            if (termType.isPrimitive())
                builder.boxPrimitive(termType.getName());
        }

        public Class implementNotExpression(InstructionBuilder builder, final Tree notNode)
        {
            Type expressionType = implementSubexpression(builder, null, notNode.getChild(0));

            boxIfPrimitive(builder, expressionType);

            // Now invoke the delegate invert() method

            builder.loadThis().getField(getDelegateField());

            builder.swap().invoke(DelegateMethods.INVERT);

            return boolean.class;
        }

        /**
         * Defer creation of the delegate field unless actually needed.
         */
        private PlasticField getDelegateField()
        {
            if (delegateField == null)
                delegateField = plasticClass.introduceField(PropertyConduitDelegate.class, "delegate").inject(
                        sharedDelegate);

            return delegateField;
        }
    }

    public PropertyConduitSourceImpl(PropertyAccess access, @ComponentLayer
    PlasticProxyFactory proxyFactory, TypeCoercer typeCoercer, StringInterner interner)
    {
        this.access = access;
        this.proxyFactory = proxyFactory;
        this.typeCoercer = typeCoercer;
        this.interner = interner;

        literalTrue = createLiteralConduit(Boolean.class, true);
        literalFalse = createLiteralConduit(Boolean.class, false);
        literalNull = createLiteralConduit(Void.class, null);

        sharedDelegate = new PropertyConduitDelegate(typeCoercer);
    }

    @PostInjection
    public void listenForInvalidations(@ComponentClasses InvalidationEventHub hub)
    {
        hub.clearOnInvalidation(cache);
    }


    public PropertyConduit create(Class rootClass, String expression)
    {
        assert rootClass != null;
        assert InternalCommonsUtils.isNonBlank(expression);

        MultiKey key = new MultiKey(rootClass, expression);

        PropertyConduit result = cache.get(key);

        if (result == null)
        {
            result = build(rootClass, expression);
            cache.put(key, result);
        }

        return result;
    }

    /**
     * Builds a subclass of {@link PropertyConduitDelegate} that implements the
     * get() and set() methods and overrides the
     * constructor. In a worst-case race condition, we may build two (or more)
     * conduits for the same
     * rootClass/expression, and it will get sorted out when the conduit is
     * stored into the cache.
     *
     * @param rootClass
     *         class of root object for expression evaluation
     * @param expression
     *         expression to be evaluated
     * @return the conduit
     */
    private PropertyConduit build(final Class rootClass, String expression)
    {
        Tree tree = parse(expression);

        try
        {
            switch (tree.getType())
            {
                case TRUE:

                    return literalTrue;

                case FALSE:

                    return literalFalse;

                case NULL:

                    return literalNull;

                case INTEGER:

                    // Leading '+' may screw this up.
                    // TODO: Singleton instance for "0", maybe "1"?

                    return createLiteralConduit(Long.class, new Long(tree.getText()));

                case DECIMAL:

                    // Leading '+' may screw this up.
                    // TODO: Singleton instance for "0.0"?

                    return createLiteralConduit(Double.class, new Double(tree.getText()));

                case STRING:

                    return createLiteralConduit(String.class, tree.getText());

                case RANGEOP:

                    Tree fromNode = tree.getChild(0);
                    Tree toNode = tree.getChild(1);

                    // If the range is defined as integers (not properties, etc.)
                    // then it is possible to calculate the value here, once, and not
                    // build a new class.

                    if (fromNode.getType() != INTEGER || toNode.getType() != INTEGER)
                        break;

                    int from = Integer.parseInt(fromNode.getText());
                    int to = Integer.parseInt(toNode.getText());

                    IntegerRange ir = new IntegerRange(from, to);

                    return createLiteralConduit(IntegerRange.class, ir);

                case THIS:

                    return createLiteralThisPropertyConduit(rootClass);

                default:
                    break;
            }

            return proxyFactory.createProxy(InternalPropertyConduit.class,
                    new PropertyConduitBuilder(rootClass, expression, tree)).newInstance();
        } catch (Exception ex)
        {
            throw new PropertyExpressionException(String.format("Exception generating conduit for expression '%s': %s",
                    expression, ExceptionUtils.toMessage(ex)), expression, ex);
        }
    }

    private PropertyConduit createLiteralThisPropertyConduit(final Class rootClass)
    {
        return new PropertyConduit()
        {
            public Object get(Object instance)
            {
                return instance;
            }

            public void set(Object instance, Object value)
            {
                throw new RuntimeException("Literal values are not updateable.");
            }

            public Class getPropertyType()
            {
                return rootClass;
            }
            
            public Type getPropertyGenericType()
            {
                return rootClass;
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return invariantAnnotationProvider.getAnnotation(annotationClass);
            }
        };
    }

    private <T> PropertyConduit createLiteralConduit(Class<T> type, T value)
    {
        return new LiteralPropertyConduit(typeCoercer, type, invariantAnnotationProvider, interner.format(
                "LiteralPropertyConduit[%s]", value), value);
    }

    private Tree parse(String expression)
    {
        InputStream is = new ByteArrayInputStream(expression.getBytes());

        ANTLRInputStream ais;

        try
        {
            ais = new ANTLRInputStream(is);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        PropertyExpressionLexer lexer = new PropertyExpressionLexer(ais);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        PropertyExpressionParser parser = new PropertyExpressionParser(tokens);

        try
        {
            return (Tree) parser.start().getTree();
        } catch (Exception ex)
        {
            throw new RuntimeException(String.format("Error parsing property expression '%s': %s.", expression,
                    ex.getMessage()), ex);
        }
    }

    /**
     * May be invoked from fabricated PropertyConduit instances.
     */
    @SuppressWarnings("unused")
    public static NullPointerException nullTerm(String term, String expression, Object root)
    {
        String message = String.format("Property '%s' (within property expression '%s', of %s) is null.", term,
                expression, root);

        return new NullPointerException(message);
    }

    private static String toUniqueId(Method method)
    {
        StringBuilder builder = new StringBuilder(method.getName()).append('(');
        String sep = "";

        for (Class parameterType : method.getParameterTypes())
        {
            builder.append(sep);
            builder.append(PlasticUtils.toTypeName(parameterType));

            sep = ",";
        }

        return builder.append(')').toString();
    }
    
}
