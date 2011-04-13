// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.DECIMAL;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.DEREF;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.FALSE;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.IDENTIFIER;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.INTEGER;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.INVOKE;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.LIST;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.NOT;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.NULL;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.RANGEOP;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.SAFEDEREF;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.STRING;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.THIS;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.TRUE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.internal.InternalPropertyConduit;
import org.apache.tapestry5.internal.antlr.PropertyExpressionLexer;
import org.apache.tapestry5.internal.antlr.PropertyExpressionParser;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.internal.util.MultiKey;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.NullAnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.GenericsUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.plastic.Condition;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.services.ComponentLayer;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.PropertyConduitSource;

public class PropertyConduitSourceImpl implements PropertyConduitSource, InvalidationListener
{
    private static final MethodDescription GET = getMethodDescription(PropertyConduit.class, "get", Object.class);

    private static final MethodDescription SET = getMethodDescription(PropertyConduit.class, "set", Object.class,
            Object.class);

    private static final MethodDescription GET_ANNOTATION = getMethodDescription(AnnotationProvider.class,
            "getAnnotation", Class.class);

    private static final MethodDescription GET_PROPERTY_TYPE = getMethodDescription(PropertyConduit.class,
            "getPropertyType");

    private static final MethodDescription GET_PROPERTY_NAME = getMethodDescription(InternalPropertyConduit.class,
            "getPropertyName");

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

    private static InstructionBuilderCallback RETURN_NULL = new InstructionBuilderCallback()
    {
        public void doBuild(InstructionBuilder builder)
        {
            builder.loadNull().returnResult();
        }
    };

    private static final String[] SINGLE_OBJECT_ARGUMENT = new String[]
    { Object.class.getName() };

    @SuppressWarnings("unchecked")
    private static Method getMethod(Class containingClass, String name, Class... parameterTypes)
    {
        try
        {
            return containingClass.getMethod(name, parameterTypes);
        }
        catch (NoSuchMethodException ex)
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
     * Describes all the gory details of one term (one property or method
     * invocation) from within the expression.
     */
    private interface ExpressionTermInfo extends AnnotationProvider
    {

        /**
         * The method to invoke to read the property value, or null.
         */
        Method getReadMethod();

        /**
         * The method to invoke to write the property value, or null. Always
         * null for method terms (which are inherently
         * read-only).
         */
        Method getWriteMethod();

        /**
         * The return type of the method, or the type of the property.
         */
        Type getType();

        /**
         * True if an explicit cast to the return type is needed (typically
         * because of generics).
         */
        boolean isCastRequired();

        /**
         * Returns a user-presentable name identifying the property or method
         * name.
         */
        String getDescription();

        /**
         * Returns the name of the property, if exists. This is also the name of the public field.
         */
        String getPropertyName();

        /**
         * Returns true if the term is actually a public field.
         */
        boolean isField();

        /**
         * Returns the Field if the term is a public field.
         */
        Field getField();

    }

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
        ALLOW,

        /**
         * Add no null check at all.
         */
        IGNORE
    }

    /**
     * One term in an expression. Expressions start with some root type and each term advances
     * to a new type.
     */
    private class PlasticTerm
    {
        /**
         * The generic type of the term.
         */
        final Type type;

        /** Callback that will implement the term. */
        final InstructionBuilderCallback callback;

        PlasticTerm(Type type, InstructionBuilderCallback callback)
        {
            this.type = type;
            this.callback = callback;
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

            delegateField = plasticClass.introduceField(PropertyConduitDelegate.class, "delegate");

            // Create the various methods; also determine the conduit's property type, property name and identify
            // the annotation provider.

            implementNavMethodAndAccessors();

            implementDelegateMethods();

            plasticClass.addToString(String.format("PropertyConduit[%s %s]", rootType.getName(), expression));
        }

        private void implementDelegateMethods()
        {
            PropertyConduitDelegate delegate = new PropertyConduitDelegate(conduitPropertyType, conduitPropertyName,
                    annotationProvider, typeCoercer);

            delegateField.inject(delegate);

            // TODO: These can easily be injected into the proxy, and not require delegate access.

            plasticClass.introduceMethod(GET_ANNOTATION).delegateTo(delegateField);
            plasticClass.introduceMethod(GET_PROPERTY_TYPE).delegateTo(delegateField);
            plasticClass.introduceMethod(GET_PROPERTY_NAME).delegateTo(delegateField);
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
                PlasticTerm term = analyzeDerefNode(activeType, node);

                callbacks.add(term.callback);

                activeType = term.type;

                // Second term is the continuation, possibly another chained
                // DEREF, etc.
                node = node.getChild(1);
            }

            // TODO: Optimization -- navigate method not needed (i.e., same as getRoot) for simple
            // expressions.

            Class activeClass = GenericsUtils.asClass(activeType);

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

            implementAccessors(activeClass, node);
        }

        private void implementAccessors(Class activeType, Tree node)
        {
            switch (node.getType())
            {
                case IDENTIFIER:
                case INVOKE:

                    // So, at this point, we have the navigation method written
                    // and it covers all but the terminal
                    // de-reference. node is an IDENTIFIER or INVOKE. We're
                    // ready to use the navigation
                    // method to implement get() and set().

                    ExpressionTermInfo info = infoForMember(activeType, node);

                    implementSetter(activeType, info);
                    implementGetter(activeType, info, node);

                    conduitPropertyType = GenericsUtils.asClass(info.getType());
                    conduitPropertyName = info.getPropertyName();
                    annotationProvider = info;

                    return;

                case RANGEOP:

                    // As currently implemented, RANGEOP can only appear as the
                    // top level, which
                    // means we didn't need the navigate method after all.

                    implementRangeOpGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = IntegerRange.class;

                    return;

                case LIST:

                    implementListGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = List.class;

                    return;

                case NOT:
                    implementNotOpGetter(node);
                    implementNoOpSetter();

                    conduitPropertyType = boolean.class;

                    return;

                default:
                    throw unexpectedNodeType(node, IDENTIFIER, INVOKE, RANGEOP, LIST, NOT);
            }
        }

        private void implementRangeOpGetter(final Tree rangeNode)
        {
            plasticClass.introduceMethod(GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    // Put the delegate on top of the stack

                    builder.loadThis().getField(delegateField);

                    invokeMethod(builder, DelegateMethods.RANGE, rangeNode, 0);

                    builder.returnResult();
                }
            });
        }

        /**
         * @param node
         *            subexpression to invert
         */
        private void implementNotOpGetter(final Tree node)
        {
            // Implement get() as navigate, then do a method invocation based on node
            // then, then pass (wrapped) result to delegate.invert()

            plasticClass.introduceMethod(GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    Class expressionType = implementNotExpression(builder, node);

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
         *            used to add instructions
         * @param activeType
         *            type of value on top of the stack when this code will execute, or null if no value on stack
         * @param node
         *            defines the expression
         * @return the expression type
         */
        private Class implementSubexpression(InstructionBuilder builder, Class activeType, Tree node)
        {
            while (node != null)
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

                        ExpressionTermInfo info = infoForMember(activeType, node);

                        return evaluateTerm(builder, activeType, node, info);

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

                        PlasticTerm term = analyzeDerefNode(activeType, node);

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

                    case NOT:

                        return implementNotExpression(builder, node);

                    default:
                        throw unexpectedNodeType(node, TRUE, FALSE, INTEGER, DECIMAL, STRING, DEREF, SAFEDEREF,
                                IDENTIFIER, INVOKE, LIST, NOT);
                }
            }

            return activeType;
        }

        public void invokeGetRootMethod(InstructionBuilder builder)
        {
            builder.loadThis().loadArgument(0).invokeVirtual(getRootMethod);
        }

        private void implementListGetter(final Tree listNode)
        {
            plasticClass.introduceMethod(GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    implementListConstructor(builder, listNode);

                    builder.returnResult();
                }
            });
        }

        private Class implementListConstructor(InstructionBuilder builder, Tree listNode)
        {
            // First, create an empty instance of ArrayList

            int count = listNode.getChildCount();

            builder.newInstance(ArrayList.class);
            builder.dupe().loadConstant(count).invokeConstructor(ArrayList.class, int.class);

            for (int i = 0; i < count; i++)
            {
                builder.dupe(); // the ArrayList

                Class expressionType = implementSubexpression(builder, null, listNode.getChild(i));

                boxIfPrimitive(builder, expressionType);

                // Add the value to the array, then pop off the returned boolean
                builder.invoke(ArrayListMethods.ADD).pop();
            }

            return ArrayList.class;
        }

        private void implementSetter(final Class activeType, final ExpressionTermInfo info)
        {
            final Method method = info.getWriteMethod();

            if (method == null && !info.isField())
            {
                implementNoOpSetter();
                return;
            }

            plasticClass.introduceMethod(SET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    String typeName = PlasticUtils.toTypeName(GenericsUtils.asClass(info.getType()));

                    builder.loadArgument(1).castOrUnbox(typeName);

                    if (info.isField())
                    {
                        builder.putField(PlasticUtils.toTypeName(activeType), info.getField().getName(), typeName);
                    }
                    else
                    {
                        // Invoke the setter method
                        // TODO: unbox to primitive?
                        builder.invoke(method);
                    }

                    builder.returnResult();
                }
            });
        }

        private void implementNoOpSetter()
        {
            implementNoOpMethod(SET, "Expression '%s' for class %s is read-only.", expression, rootType.getName());
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
         * Implements the get() method, using the navigate method.
         * 
         * @param activeType
         *            the type containing the property to read (may be the root type for simple
         *            property expressions)
         * @param info
         *            describes the property to read
         */
        private void implementGetter(final Class activeType, final ExpressionTermInfo info, final Tree node)
        {
            if (info.getReadMethod() == null && !info.isField())
            {
                implementNoOpMethod(GET, "Expression %s for class %s is write-only.", expression, rootType.getName());
                return;
            }

            plasticClass.introduceMethod(GET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    invokeNavigateMethod(builder);

                    Class termType = evaluateTerm(builder, activeType, node, info);

                    boxIfPrimitive(builder, termType);

                    builder.returnResult();
                }
            });
        }

        /**
         * Extends the builder with the code to evaluate a term (which may
         * 
         * @param builder
         * @param activeType
         *            current type
         * @param termNode
         *            the parse Tree node for the term (IDENTIFIER or INVOKE)
         * @param info
         *            about the expression term
         * @return the new active type
         */
        public Class evaluateTerm(InstructionBuilder builder, Class activeType, Tree termNode, ExpressionTermInfo info)
        {
            Class termType = GenericsUtils.asClass(info.getType());
            String termTypeName = PlasticUtils.toTypeName(termType);

            if (info.isField())
            {
                builder.getField(PlasticUtils.toTypeName(activeType), info.getPropertyName(), termTypeName);
            }
            else
            {
                invokeMethod(builder, info.getReadMethod(), termNode, 1);
            }

            return termType;
        }

        /**
         * Invokes a method that may take parameters. The children of the invokeNode are subexpressions
         * to be evaluated, and potentially coerced, so that they may be passed to the method.
         * 
         * @param builder
         *            constructs code
         * @param method
         *            method to invoke
         * @param node
         *            INVOKE or RANGEOP node
         * @param childOffset
         *            offset within the node to the first child expression (1 in an INVOKE node because the
         *            first child is the method name, 0 in a RANGEOP node)
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
                Class expressionType = implementSubexpression(builder, null, node.getChild(i + childOffset));

                // The value left on the stack is not primitive, and expressionType represents
                // its real type.

                Class parameterType = parameterTypes[i];

                if (!parameterType.isAssignableFrom(expressionType))
                {
                    if (expressionType.isPrimitive())
                    {
                        builder.boxPrimitive(expressionType.getName());
                    }

                    builder.loadThis().getField(delegateField);
                    builder.swap().loadTypeConstant(PlasticUtils.toWrapperType(parameterType));
                    builder.invoke(DelegateMethods.COERCE);

                    if (parameterType.isPrimitive())
                    {
                        builder.castOrUnbox(parameterType.getName());
                    }
                    else
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
        private PlasticTerm analyzeDerefNode(Type activeType, Tree node)
        {
            // The first child is the term.

            Tree term = node.getChild(0);

            boolean allowNull = node.getType() == SAFEDEREF;

            return buildTerm(activeType, term, allowNull ? NullHandling.ALLOW : NullHandling.FORBID);
        }

        private PlasticTerm buildTerm(Type activeType, final Tree term, final NullHandling nullHandling)
        {
            assertNodeType(term, IDENTIFIER, INVOKE);

            // Get info about this property or method.

            final ExpressionTermInfo info = infoForMember(activeType, term);

            final Method method = info.getReadMethod();
            final Class activeClass = GenericsUtils.asClass(activeType);

            if (method == null && !info.isField())
                throw new RuntimeException(String.format(
                        "Property '%s' of class %s is not readable (it has no read accessor method).",
                        info.getDescription(), activeClass.getName()));

            Type termType = info.getType();

            final Class termClass = GenericsUtils.asClass(termType);

            InstructionBuilderCallback callback = new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    if (info.isField())
                    {
                        String typeName = PlasticUtils.toTypeName(termClass);

                        builder.getField(info.getField().getDeclaringClass().getName(), info.getField().getName(),
                                PlasticUtils.toTypeName(info.getField().getType()));

                        builder.unboxPrimitive(typeName);
                    }
                    else
                    {
                        invokeMethod(builder, method, term, 1);
                    }

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

                                    builder.loadConstant(info.getDescription());
                                    builder.loadConstant(expression);
                                    builder.loadArgument(0);

                                    builder.invokeStatic(PropertyConduitSourceImpl.class, NullPointerException.class,
                                            "nullTerm", String.class, String.class, Object.class);
                                    builder.throwException();

                                    break;

                            }
                        }
                    });

                    if (info.isCastRequired())
                    {
                        builder.checkcast(termClass);
                    }
                }
            };

            return new PlasticTerm(termType, callback);
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
                    InternalUtils.joinSorted(tokenNames));

            return new RuntimeException(message);
        }

        private ExpressionTermInfo infoForMember(Type activeType, Tree node)
        {
            if (node.getType() == INVOKE)
                return infoForInvokeNode(activeType, node);

            return infoForPropertyOrPublicField(activeType, node);
        }

        private ExpressionTermInfo infoForPropertyOrPublicField(Type activeType, Tree node)
        {
            String propertyName = node.getText();

            final Class activeClass = GenericsUtils.asClass(activeType);
            ClassPropertyAdapter classAdapter = access.getAdapter(activeClass);
            final PropertyAdapter adapter = classAdapter.getPropertyAdapter(propertyName);

            if (adapter == null)
            {
                final List<String> names = classAdapter.getPropertyNames();
                final String className = activeClass.getName();
                throw new UnknownValueException(String.format(
                        "Class %s does not contain a property (or public field) named '%s'.", className, propertyName),
                        new AvailableValues("Properties (and public fields)", names));
            }

            final Type type;
            final boolean isCastRequired;
            if (adapter.getField() != null)
            {
                type = GenericsUtils.extractActualType(activeType, adapter.getField());
                isCastRequired = !type.equals(adapter.getField().getType());
            }
            else if (adapter.getReadMethod() != null)
            {
                type = GenericsUtils.extractActualType(activeType, adapter.getReadMethod());
                isCastRequired = !type.equals(adapter.getReadMethod().getReturnType());
            }
            else
            {
                type = adapter.getType();
                isCastRequired = adapter.isCastRequired();
            }

            return new ExpressionTermInfo()
            {
                public Method getReadMethod()
                {
                    return adapter.getReadMethod();
                }

                public Method getWriteMethod()
                {
                    return adapter.getWriteMethod();
                }

                public Type getType()
                {
                    return type;
                }

                public boolean isCastRequired()
                {
                    return isCastRequired;
                }

                public String getDescription()
                {
                    return adapter.getName();
                }

                public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
                {
                    return adapter.getAnnotation(annotationClass);
                }

                public String getPropertyName()
                {
                    return adapter.getName();
                }

                public boolean isField()
                {
                    return adapter.isField();
                }

                public Field getField()
                {
                    return adapter.getField();
                }
            };
        }

        private ExpressionTermInfo infoForInvokeNode(Type activeType, Tree node)
        {
            String methodName = node.getChild(0).getText();

            int parameterCount = node.getChildCount() - 1;

            final Class activeClass = GenericsUtils.asClass(activeType);
            try
            {
                final Method method = findMethod(activeClass, methodName, parameterCount);

                if (method.getReturnType().equals(void.class))
                    throw new RuntimeException(String.format("Method %s.%s() returns void.", activeClass.getName(),
                            methodName));

                final Type genericType = GenericsUtils.extractActualType(activeType, method);

                return new ExpressionTermInfo()
                {
                    public Method getReadMethod()
                    {
                        return method;
                    }

                    public Method getWriteMethod()
                    {
                        return null;
                    }

                    public Type getType()
                    {
                        return genericType;
                    }

                    public boolean isCastRequired()
                    {
                        return genericType != method.getReturnType();
                    }

                    public String getDescription()
                    {
                        return new MethodSignature(method).getUniqueId();
                    }

                    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
                    {
                        return method.getAnnotation(annotationClass);
                    }

                    public String getPropertyName()
                    {
                        return null;
                    }

                    public boolean isField()
                    {
                        return false;
                    }

                    public Field getField()
                    {
                        return null;
                    }
                };
            }
            catch (NoSuchMethodException ex)
            {
                throw new RuntimeException(String.format("No public method '%s()' in class %s.", methodName,
                        activeClass.getName()));
            }
        }

        private Method findMethod(Class activeType, String methodName, int parameterCount) throws NoSuchMethodException
        {
            for (Method method : activeType.getMethods())
            {

                if (method.getParameterTypes().length == parameterCount
                        && method.getName().equalsIgnoreCase(methodName))
                    return method;
            }

            // TAP5-330
            if (activeType != Object.class)
                return findMethod(Object.class, methodName, parameterCount);

            throw new NoSuchMethodException(ServicesMessages.noSuchMethod(activeType, methodName));
        }

        public void boxIfPrimitive(InstructionBuilder builder, Class termType)
        {
            if (termType.isPrimitive())
                builder.boxPrimitive(termType.getName());
        }

        public Class implementNotExpression(InstructionBuilder builder, final Tree notNode)
        {
            Class expressionType = implementSubexpression(builder, null, notNode.getChild(0));

            boxIfPrimitive(builder, expressionType);

            // Now invoke the delegate invert() method

            builder.loadThis().getField(delegateField);

            builder.swap().invoke(DelegateMethods.INVERT);

            return boolean.class;
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
    }

    public PropertyConduit create(Class rootClass, String expression)
    {
        assert rootClass != null;
        assert InternalUtils.isNonBlank(expression);

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
     * Clears its caches when the component class loader is invalidated; this is
     * because it will be common to generate
     * conduits rooted in a component class (which will no longer be valid and
     * must be released to the garbage
     * collector).
     */
    public void objectWasInvalidated()
    {
        cache.clear();
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
     *            class of root object for expression evaluation
     * @param expression
     *            expression to be evaluated
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
        }
        catch (Exception ex)
        {
            throw new PropertyExpressionException(String.format("Exception generating conduit for expression '%s': %s",
                    expression, InternalUtils.toMessage(ex)), expression, ex);
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
                throw new RuntimeException(ServicesMessages.literalConduitNotUpdateable());
            }

            public Class getPropertyType()
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
        return new LiteralPropertyConduit(type, invariantAnnotationProvider, interner.format(
                "LiteralPropertyConduit[%s]", value), typeCoercer, value);
    }

    private Tree parse(String expression)
    {
        InputStream is = new ByteArrayInputStream(expression.getBytes());

        ANTLRInputStream ais;

        try
        {
            ais = new ANTLRInputStream(is);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        PropertyExpressionLexer lexer = new PropertyExpressionLexer(ais);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        PropertyExpressionParser parser = new PropertyExpressionParser(tokens);

        try
        {
            return (Tree) parser.start().getTree();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format("Error parsing property expression '%s': %s.", expression,
                    ex.getMessage()), ex);
        }
    }

    /**
     * May be invoked from fabricated PropertyConduit instances.
     */
    public static NullPointerException nullTerm(String term, String expression, Object root)
    {
        String message = String.format("Property '%s' (within property expression '%s', of %s) is null.", term,
                expression, root);

        return new NullPointerException(message);
    }

}
