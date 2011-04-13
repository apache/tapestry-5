// Copyright 2007, 2008, 2009, 2010 The Apache Software Foundation
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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
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
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.ioc.util.UnknownValueException;
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

    private static final MethodSignature GET_SIGNATURE = new MethodSignature(Object.class, "get", new Class[]
    { Object.class }, null);

    private static final MethodSignature SET_SIGNATURE = new MethodSignature(void.class, "set", new Class[]
    { Object.class, Object.class }, null);

    private static final Method RANGE = getMethod(PropertyConduitDelegate.class, "range", int.class, int.class);

    private static final Method INVERT = getMethod(PropertyConduitDelegate.class, "invert", Object.class);

    private static final MethodDescription GET_ANNOTATION = getMethodDescription(AnnotationProvider.class,
            "getAnnotation", Class.class);

    private static final MethodDescription GET_PROPERTY_TYPE = getMethodDescription(PropertyConduit.class,
            "getPropertyType");

    private static final MethodDescription GET_PROPERTY_NAME = getMethodDescription(InternalPropertyConduit.class,
            "getPropertyName");

    private static final InstructionBuilderCallback RETURN_RESULT = new InstructionBuilderCallback()
    {
        public void doBuild(InstructionBuilder builder)
        {
            builder.returnResult();
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

    private static class ConstructorParameter
    {
        private final String fieldName;

        private final Class type;

        private final Object value;

        ConstructorParameter(String fieldName, Class type, Object value)
        {
            this.fieldName = fieldName;
            this.type = type;
            this.value = value;
        }

        public String getFieldName()
        {
            return fieldName;
        }

        public Class getType()
        {
            return type;
        }

        public Object getValue()
        {
            return value;
        }
    }

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

    private class GeneratedTerm
    {
        final Type type;

        final String termReference;

        /**
         * @param type
         *            type of variable
         * @param termReference
         *            name of variable, or a constant value
         */
        private GeneratedTerm(Type type, String termReference)
        {
            this.type = type;
            this.termReference = termReference;
        }
    }

    private class PlasticTerm
    {
        final Type type;

        final InstructionBuilderCallback callback;

        PlasticTerm(Type type, InstructionBuilderCallback callback)
        {
            this.type = type;
            this.callback = callback;
        }
    }

    private final PropertyAccess access;

    private final ClassFactory classFactory;

    private final PlasticProxyFactory proxyFactory;

    private final TypeCoercer typeCoercer;

    private final StringInterner interner;

    /**
     * Because of stuff like Hibernate, we sometimes start with a subclass in
     * some inaccessible class loader and need to
     * work up to a base class from a common class loader.
     */
    private final Map<Class, Class> classToEffectiveClass = CollectionFactory.newConcurrentMap();

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
     * expression.
     */
    class PropertyConduitBuilder implements PlasticClassTransformer
    {
        private final Class rootType;

        private final ClassFab classFab;

        private final String expression;

        private final Tree tree;

        private Class conduitPropertyType;

        private String conduitPropertyName;

        private AnnotationProvider annotationProvider = nullAnnotationProvider;

        // Used to create unique variable names.

        private int variableIndex = 0;

        private final List<ConstructorParameter> parameters = CollectionFactory.newList();

        private final BodyBuilder navBuilder = new BodyBuilder();

        private PlasticField delegateField;

        private PlasticClass plasticClass;

        private PlasticMethod getRootMethod, navMethod;

        PropertyConduitBuilder(Class rootType, String expression, Tree tree)
        {
            this.rootType = rootType;
            this.expression = expression;
            this.tree = tree;

            String name = ClassFabUtils.generateClassName("PropertyConduit");

            this.classFab = classFactory.newClass(name, PropertyConduitDelegate.class);
        }

        public void transform(PlasticClass plasticClass)
        {
            this.plasticClass = plasticClass;

            delegateField = plasticClass.introduceField(PropertyConduitDelegate.class, "delegate");

            // Create the various methods; also determine the conduit's property type, property name and identify
            // the annotation provider.

            createPlasticAccessors();

            addDelegateMethods();

            plasticClass.addToString(String.format("PropertyConduit[%s %s]", rootType.getName(), expression));
        }

        private void addDelegateMethods()
        {
            PropertyConduitDelegate delegate = new PropertyConduitDelegate(conduitPropertyType, conduitPropertyName,
                    annotationProvider, typeCoercer);

            delegateField.inject(delegate);

            plasticClass.introduceMethod(GET_ANNOTATION).delegateTo(delegateField);
            plasticClass.introduceMethod(GET_PROPERTY_TYPE).delegateTo(delegateField);
            plasticClass.introduceMethod(GET_PROPERTY_NAME).delegateTo(delegateField);
        }

        /**
         * Creates a method that does a conversion from Object to the expected root type, with
         * a null check.
         */
        private void createPlasticGetRoot()
        {
            getRootMethod = plasticClass.introducePrivateMethod(PlasticUtils.toTypeName(rootType), "getRoot",
                    SINGLE_OBJECT_ARGUMENT, null);

            getRootMethod.changeImplementation(new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadArgument(0).dupe(0);
                    builder.ifNull(new InstructionBuilderCallback()
                    {
                        public void doBuild(InstructionBuilder builder)
                        {
                            builder.throwException(NullPointerException.class,
                                    String.format("Root object of property expression '%s' is null.", expression));
                        }
                    }, null);

                    builder.checkcast(rootType).returnResult();
                }
            });
        }

        PropertyConduit createInstance()
        {
            createAccessors();

            Object[] parameters = createConstructor();

            Class conduitClass = classFab.createClass();

            try
            {
                return (PropertyConduit) conduitClass.getConstructors()[0].newInstance(parameters);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }

        private Object[] createConstructor()
        {
            List<Class> types = CollectionFactory.newList();

            // $1, $2, $3, $4, $5 ...

            types.add(Class.class);
            types.add(String.class);
            types.add(AnnotationProvider.class);
            types.add(String.class);
            types.add(TypeCoercer.class);

            List<Object> values = CollectionFactory.newList();

            values.add(conduitPropertyType);
            values.add(conduitPropertyName);
            values.add(annotationProvider);
            values.add(interner.format("PropertyConduit[%s %s]", rootType.getName(), expression));
            values.add(typeCoercer);

            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("super($1,$2,$3,$4,$5);");

            int index = 6;

            for (ConstructorParameter p : parameters)
            {
                types.add(p.getType());
                values.add(p.getValue());

                builder.addln("%s = $%d;", p.getFieldName(), index++);
            }

            builder.end();

            Class[] arrayOfTypes = types.toArray(new Class[0]);

            classFab.addConstructor(arrayOfTypes, null, builder.toString());

            return values.toArray();
        }

        private String addInjection(Class fieldType, Object fieldValue)
        {
            String fieldName = String.format("injected_%s_%d", toSimpleName(fieldType), parameters.size());

            classFab.addField(fieldName, Modifier.PRIVATE | Modifier.FINAL, fieldType);

            parameters.add(new ConstructorParameter(fieldName, fieldType, fieldValue));

            return fieldName;
        }

        private void createNoOp(ClassFab classFab, MethodSignature signature, String format, Object... values)
        {
            String message = String.format(format, values);

            String body = String.format("throw new RuntimeException(\"%s\");", message);

            classFab.addMethod(Modifier.PUBLIC, signature, body);
        }

        private boolean isLeaf(Tree node)
        {
            int type = node.getType();

            return type != DEREF && type != SAFEDEREF;
        }

        private void createGetRoot()
        {
            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s root = (%<s) $1;", ClassFabUtils.toJavaClassName(rootType));

            builder.addln(
                    "if (root == null) throw new NullPointerException(\"Root object of property expression '%s' is null.\");",
                    expression);

            builder.addln("return root;");

            builder.end();

            MethodSignature sig = new MethodSignature(rootType, "getRoot", new Class[]
            { Object.class }, null);

            classFab.addMethod(Modifier.PRIVATE, sig, builder.toString());
        }

        private void addRootVariable(BodyBuilder builder)
        {
            builder.addln("%s root = getRoot($1);", ClassFabUtils.toJavaClassName(rootType));
        }

        private void createPlasticAccessors()
        {
            createPlasticGetRoot();

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

                    // No need to box, the callbacks for each property ensure that primitives are boxed

                    builder.returnResult();
                }
            });

            createPlasticGetterAndSetter(activeClass, node);
        }

        private void createAccessors()
        {
            createGetRoot();

            navBuilder.begin();

            String previousReference = "$1";
            Type activeType = rootType;

            Tree node = tree;

            while (!isLeaf(node))
            {
                GeneratedTerm term = processDerefNode(navBuilder, activeType, node, previousReference, "$1");

                activeType = term.type;

                previousReference = term.termReference;

                // Second term is the continuation, possibly another chained
                // DEREF, etc.
                node = node.getChild(1);
            }

            navBuilder.addln("return %s;", previousReference);

            navBuilder.end();
            Class activeClass = GenericsUtils.asClass(activeType);

            MethodSignature sig = new MethodSignature(activeClass, "navigate", new Class[]
            { rootType }, null);

            classFab.addMethod(Modifier.PRIVATE, sig, navBuilder.toString());

            createGetterAndSetter(activeClass, sig, node);
        }

        private void createPlasticGetterAndSetter(Class activeType, Tree node)
        {
            switch (node.getType())
            {
                case IDENTIFIER:
                case INVOKE:

                    // So, a this point, we have the navigation method written
                    // and it covers all but the terminal
                    // de-reference. node is an IDENTIFIER or INVOKE. We're
                    // ready to use the navigation
                    // method to implement get() and set().

                    ExpressionTermInfo info = infoForMember(activeType, node);

                    createPlasticSetter(activeType, info);
                    createPlasticGetter(activeType, info);

                    conduitPropertyType = GenericsUtils.asClass(info.getType());
                    conduitPropertyName = info.getPropertyName();
                    annotationProvider = info;

                    return;

                case RANGEOP:

                    // As currently implemented, RANGEOP can only appear as the
                    // top level, which
                    // means we didn't need the navigate method after all.

                    // createPlasticRangeOpGetter(node, "root");
                    createPlasticNoOpSetter();

                    conduitPropertyType = IntegerRange.class;

                    return;

                case LIST:

                    // createPlasticListGetter(node, "root");
                    createPlasticNoOpSetter();

                    conduitPropertyType = List.class;

                    return;

                case NOT:
                    // createPlasticNotOpGetter(node);
                    createPlasticNoOpSetter();

                    conduitPropertyType = boolean.class;

                    return;

                default:
                    throw unexpectedNodeType(node, IDENTIFIER, INVOKE, RANGEOP, LIST, NOT);
            }
        }

        private void createGetterAndSetter(Class activeType, MethodSignature navigateMethod, Tree node)
        {
            switch (node.getType())
            {
                case IDENTIFIER:
                case INVOKE:

                    // So, a this point, we have the navigation method written
                    // and it covers all but the terminal
                    // de-reference. node is an IDENTIFIER or INVOKE. We're
                    // ready to use the navigation
                    // method to implement get() and set().

                    ExpressionTermInfo info = infoForMember(activeType, node);

                    createSetter(navigateMethod, info);
                    createGetter(navigateMethod, node, info);

                    conduitPropertyType = GenericsUtils.asClass(info.getType());
                    conduitPropertyName = info.getPropertyName();
                    annotationProvider = info;

                    return;

                case RANGEOP:

                    // As currently implemented, RANGEOP can only appear as the
                    // top level, which
                    // means we didn't need the navigate method after all.

                    createRangeOpGetter(node, "root");
                    createNoOpSetter();

                    conduitPropertyType = IntegerRange.class;

                    return;

                case LIST:

                    createListGetter(node, "root");
                    createNoOpSetter();

                    conduitPropertyType = List.class;

                    return;

                case NOT:
                    createNotOpGetter(node, "root");
                    createNoOpSetter();

                    conduitPropertyType = boolean.class;

                    return;

                default:
                    throw unexpectedNodeType(node, IDENTIFIER, INVOKE, RANGEOP, LIST, NOT);
            }
        }

        private void createRangeOpGetter(Tree node, String rootName)
        {
            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("return %s;", createMethodInvocation(builder, node, rootName, 0, RANGE));

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }

        private void createPlasticNotOpGetter()
        {

        }

        private void createNotOpGetter(Tree node, String rootName)
        {
            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("return ($w) %s;", createMethodInvocation(builder, node, rootName, 0, INVERT));

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }

        public void createListGetter(Tree node, String rootName)
        {
            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("return %s;", createListConstructor(builder, node, rootName));

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }

        private String createListConstructor(BodyBuilder builder, Tree node, String rootName)
        {
            String listName = nextVariableName(List.class);

            int count = node.getChildCount();

            builder.addln("java.util.List %s = new java.util.ArrayList(%d);", listName, count);

            for (int i = 0; i < count; i++)
            {
                GeneratedTerm generatedTerm = subexpression(builder, node.getChild(i), rootName);

                builder.addln("%s.add(($w) %s);", listName, generatedTerm.termReference);
            }

            return listName;
        }

        private String createNotOp(BodyBuilder builder, Tree node, String rootName)
        {
            String flagName = nextVariableName(Boolean.class);
            GeneratedTerm term = subexpression(builder, node.getChild(0), rootName);

            builder.addln("boolean %s = invert(($w) %s);", flagName, term.termReference);

            return flagName;
        }

        /**
         * Evalutates the node as a sub expression, storing the result into a
         * new variable, whose name is returned.
         * 
         * @param builder
         *            to receive generated code
         * @param node
         *            root of tree of nodes to be evaluated
         * @param rootName
         *            name of variable holding reference to root object of
         *            expression
         * @return GeneratedTerm identifying the name of the variable and its
         *         type
         */
        private GeneratedTerm subexpression(BodyBuilder builder, Tree node, String rootName)
        {
            String previousReference = rootName;
            Class activeType = rootType;

            while (node != null)
            {
                switch (node.getType())
                {
                    case TRUE:
                    case FALSE:

                        previousReference = node.getType() == TRUE ? "true" : "false";
                        activeType = boolean.class;

                        node = null;
                        break;

                    case INTEGER:

                        long integerValue = Long.parseLong(node.getText());

                        previousReference = String.format("%dL", integerValue);
                        activeType = long.class;

                        node = null;

                        break;

                    case DECIMAL:

                        double decimalValue = Double.parseDouble(node.getText());

                        previousReference = String.format(Locale.ENGLISH, "%fd", decimalValue);
                        activeType = double.class;

                        node = null;

                        break;

                    case STRING:

                        String stringValue = node.getText();
                        // Injecting is easier; don't have to fuss with escaping
                        // quotes or such.
                        previousReference = addInjection(String.class, stringValue);
                        activeType = String.class;

                        node = null;

                        break;

                    case DEREF:
                    case SAFEDEREF:

                        GeneratedTerm generated = processDerefNode(builder, activeType, node, previousReference,
                                rootName);

                        previousReference = generated.termReference;
                        activeType = GenericsUtils.asClass(generated.type);

                        node = node.getChild(1);

                        break;

                    case IDENTIFIER:
                    case INVOKE:

                        generated = addAccessForMember(builder, activeType, node, previousReference, rootName,
                                NullHandling.IGNORE);

                        previousReference = generated.termReference;
                        activeType = GenericsUtils.asClass(generated.type);
                        if (activeType.isPrimitive())
                            activeType = ClassFabUtils.getWrapperType(activeType);
                        node = null;

                        break;

                    case NOT:

                        previousReference = createNotOp(builder, node, rootName);
                        activeType = boolean.class;

                        node = null;

                        break;

                    case LIST:

                        previousReference = createListConstructor(builder, node, rootName);
                        activeType = List.class;

                        node = null;

                        break;

                    default:
                        throw unexpectedNodeType(node, TRUE, FALSE, INTEGER, DECIMAL, STRING, DEREF, SAFEDEREF,
                                IDENTIFIER, INVOKE, LIST);
                }
            }

            return new GeneratedTerm(activeType, previousReference);
        }

        private void createPlasticSetter(final Class activeType, final ExpressionTermInfo info)
        {
            final Method method = info.getWriteMethod();

            if (method == null && !info.isField())
            {
                createPlasticNoOpSetter();
                return;
            }

            plasticClass.introduceMethod(SET, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadThis().loadArgument(0).invokeVirtual(navMethod);

                    String typeName = PlasticUtils.toTypeName(GenericsUtils.asClass(info.getType()));

                    builder.loadArgument(0).unboxPrimitive(typeName);

                    if (info.isField())
                    {
                        builder.putField(PlasticUtils.toTypeName(activeType), info.getField().getName(), typeName);
                    }
                    else
                    {
                        invokeNoArgsMethod(builder, method);
                    }
                }
            });
        }

        private void createSetter(MethodSignature navigateMethod, ExpressionTermInfo info)
        {
            // A write method will only be identified if the info is a writable
            // property.
            // Other alternatives: a method as the final term, or a read-only
            // property.

            Method method = info.getWriteMethod();

            if (method == null && !info.isField())
            {
                createNoOpSetter();
                return;
            }

            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("%s target = navigate(root);", ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()));

            // I.e. due to ?. operator. The navigate method will already have
            // checked for nulls
            // if they are not allowed.

            builder.addln("if (target == null) return;");

            String propertyTypeName = ClassFabUtils.toJavaClassName(GenericsUtils.asClass(info.getType()));

            String reference = ClassFabUtils.castReference("$2", propertyTypeName);

            if (info.isField())
            {
                builder.add("target.%s = %s;", info.getPropertyName(), reference);
            }
            else
            {
                builder.addln("target.%s(%s);", method.getName(), reference);
            }

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());
        }

        private void createPlasticNoOpSetter()
        {
            createNoOpMethod(SET, "Expression '%s' for class %s is read-only.", expression, rootType.getName());
        }

        public void createNoOpMethod(MethodDescription method, String format, Object... arguments)
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

        private void createNoOpSetter()
        {
            createNoOp(classFab, SET_SIGNATURE, "Expression '%s' for class %s is read-only.", expression,
                    rootType.getName());
        }

        /**
         * Creates the get() method, using the navigate method.
         * 
         * @param activeType
         *            the type containing the property to read (may be the root type for simple
         *            property expressions)
         * @param info
         *            describes the property to read
         */
        private void createPlasticGetter(final Class activeType, final ExpressionTermInfo info)
        {
            final Method method = info.getReadMethod();

            if (method == null && !info.isField())
            {
                createNoOpMethod(GET, "Expression %s for class %s is write-only.", expression, rootType.getName());
                return;
            }

            plasticClass.introduceMethod(GET).changeImplementation(new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadThis().loadArgument(0).invokeVirtual(navMethod);

                    // I.e. due to ?. operator. The navigate method will already have
                    // checked for nulls if they are not allowed.

                    builder.dupe(0).ifNull(RETURN_RESULT, null);

                    Class termType = GenericsUtils.asClass(info.getType());
                    String termTypeName = PlasticUtils.toTypeName(termType);

                    if (info.isField())
                    {
                        builder.getField(PlasticUtils.toTypeName(activeType), info.getPropertyName(), termTypeName);
                    }
                    else
                    {
                        invokeNoArgsMethod(builder, method);
                    }

                    builder.boxPrimitive(termTypeName).returnResult();
                }
            });
        }

        private void invokeNoArgsMethod(InstructionBuilder builder, Method method)
        {
            builder.invoke(method.getDeclaringClass(), method.getReturnType(), method.getName());
        }

        private void createGetter(MethodSignature navigateMethod, Tree node, ExpressionTermInfo info)
        {
            Method method = info.getReadMethod();

            if (method == null && !info.isField())
            {
                createNoOp(classFab, GET_SIGNATURE, "Expression %s for class %s is write-only.", expression,
                        rootType.getName());
                return;
            }

            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("%s target = navigate(root);", ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()));

            // I.e. due to ?. operator. The navigate method will already have
            // checked for nulls
            // if they are not allowed.

            builder.addln("if (target == null) return null;");

            String reference = info.isField() ? info.getPropertyName() : createMethodInvocation(builder, node, "root",
                    method);

            builder.addln("return ($w) target.%s;", reference);

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }

        /**
         * Creates a method invocation call for the given node (an INVOKE node).
         * 
         * @param bodyBuilder
         *            may receive new code to define variables for some
         *            sub-expressions
         * @param node
         *            the INVOKE node; child #1 and up are parameter expressions
         *            to the method being invoked
         * @param rootName
         *            name of variable holding reference to root object of
         *            expression
         * @param method
         *            defines the name and parameter types of the method to
         *            invoke
         * @return method invocation string (the name of the method and any
         *         parameters, ready to be added to a method
         *         body)
         */
        private String createMethodInvocation(BodyBuilder bodyBuilder, Tree node, String rootName, Method method)
        {
            return createMethodInvocation(bodyBuilder, node, rootName, 1, method);
        }

        /**
         * Creates a method invocation call for the given node
         * 
         * @param bodyBuilder
         *            may receive new code to define variables for some
         *            sub-expressions
         * @param node
         *            the node containing child nodes for the parameters
         * @param rootName
         *            name of variable holding reference to root object of
         *            expression
         * @param childOffset
         *            the offset to the first parameter (for example, this is 1
         *            for an INVOKE node)
         * @param method
         *            defines the name and parameter types of the method to
         *            invoke
         * @return method invocation string (the name of the method and any
         *         parameters, ready to be added to a method
         *         body)
         */
        private String createMethodInvocation(BodyBuilder bodyBuilder, Tree node, String rootName, int childOffset,
                Method method)
        {
            Class[] parameterTypes = method.getParameterTypes();

            StringBuilder builder = new StringBuilder();

            builder.append(method.getName());
            builder.append("(");

            for (int i = 0; i < parameterTypes.length; i++)
            {
                // child(0) is the method name, child(1) is the first parameter,
                // etc.

                GeneratedTerm generatedTerm = subexpression(bodyBuilder, node.getChild(i + childOffset), rootName);
                String currentReference = generatedTerm.termReference;

                Class actualType = GenericsUtils.asClass(generatedTerm.type);

                Class parameterType = parameterTypes[i];

                boolean needsUnwrap = false;

                if (!parameterType.isAssignableFrom(actualType))
                {
                    String coerced = nextVariableName(parameterType);

                    String call = String.format("coerce(($w) %s, %s)", currentReference,
                            addInjection(Class.class, parameterType));

                    String parameterTypeName = ClassFabUtils.toJavaClassName(parameterType);

                    bodyBuilder.addln("%s %s = %s;", parameterTypeName, coerced,
                            ClassFabUtils.castReference(call, parameterTypeName));

                    currentReference = coerced;
                }
                else
                {
                    needsUnwrap = parameterType.isPrimitive() && !actualType.isPrimitive();
                }

                if (i > 0)
                    builder.append(", ");

                builder.append(currentReference);

                if (needsUnwrap)
                {
                    builder.append(".").append(ClassFabUtils.getUnwrapMethodName(parameterType)).append("()");
                }
            }

            return builder.append(")").toString();
        }

        /**
         * Extends the navigate method for a node, which will be a DEREF or
         * SAFEDERF.
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

        /**
         * Extends the navigate method for a node, which will be a DEREF or
         * SAFEDERF.
         */
        private GeneratedTerm processDerefNode(BodyBuilder builder, Type activeType, Tree node,
                String previousVariableName, String rootName)
        {
            // The first child is the term.

            Tree term = node.getChild(0);

            boolean allowNull = node.getType() == SAFEDEREF;

            // Returns the type of the method/property ... this is the wrapped
            // (i.e. java.lang.Integer) type if
            // the real type is primitive. It also reflects generics information
            // that may have been associated
            // with the underlying method.

            return addAccessForMember(builder, activeType, term, previousVariableName, rootName,
                    allowNull ? NullHandling.ALLOW : NullHandling.FORBID);
        }

        private String nextVariableName(Class type)
        {
            return String.format("var_%s_%d", toSimpleName(type), variableIndex++);
        }

        private String toSimpleName(Class type)
        {
            if (type.isArray())
            {
                Class<?> componentType = type.getComponentType();

                while (componentType.isArray())
                {
                    componentType = componentType.getComponentType();
                }

                return InternalUtils.lastTerm(componentType.getName()) + "_array";
            }
            return InternalUtils.lastTerm(type.getName());
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

            final Class wrappedType = ClassFabUtils.getWrapperType(termClass);

            String wrapperTypeName = ClassFabUtils.toJavaClassName(wrappedType);

            final String variableName = nextVariableName(wrappedType);

            InstructionBuilderCallback callback = new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    if (info.isField())
                    {
                        String typeName = PlasticUtils.toTypeName(termClass);

                        builder.getField(PlasticUtils.toTypeName(info.getField().getDeclaringClass()), info.getField()
                                .getName(), typeName);

                        builder.unboxPrimitive(typeName);
                    }
                    else
                    {
                        createPlasticMethodInvocation(term, method);
                    }

                    builder.dupe(0).ifNull(new InstructionBuilderCallback()
                    {
                        public void doBuild(InstructionBuilder builder)
                        {
                            switch (nullHandling)
                            {
                                case ALLOW:
                                    builder.returnResult();

                                case FORBID:

                                    builder.loadConstant(info.getDescription());
                                    builder.loadConstant(expression);
                                    builder.loadArgument(0);

                                    builder.invokeStatic(PropertyConduitSourceImpl.class, void.class, "nullTerm",
                                            String.class, String.class, Object.class);
                            }
                        }
                    }, null);
                }
            };

            return new PlasticTerm(termType, callback);
        }

        private void createPlasticMethodInvocation(Tree term, Method method)
        {
            // TODO Auto-generated method stub

        }

        private GeneratedTerm addAccessForMember(BodyBuilder builder, Type activeType, Tree term,
                String previousVariableName, String rootName, NullHandling nullHandling)
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

            Class termClass = GenericsUtils.asClass(termType);

            final Class wrappedType = ClassFabUtils.getWrapperType(termClass);

            String wrapperTypeName = ClassFabUtils.toJavaClassName(wrappedType);

            final String variableName = nextVariableName(wrappedType);

            String reference = info.isField() ? info.getPropertyName() : createMethodInvocation(builder, term,
                    rootName, method);

            builder.add("%s %s = ", wrapperTypeName, variableName);

            // Casts are needed for primitives, and for the case where
            // generics are involved.

            if (termClass.isPrimitive())
            {
                builder.add(" ($w) ");
            }
            else if (info.isCastRequired())
            {
                builder.add(" (%s) ", wrapperTypeName);
            }

            builder.addln("%s.%s;", previousVariableName, reference);

            switch (nullHandling)
            {
                case ALLOW:
                    builder.addln("if (%s == null) return null;", variableName);
                    break;

                case FORBID:
                    // Perform a null check on intermediate terms.
                    builder.addln("if (%s == null) %s.nullTerm(\"%s\", \"%s\", $1);", variableName,
                            PropertyConduitSourceImpl.class.getName(), info.getDescription(), expression);
                    break;

                default:
                    break;
            }

            return new GeneratedTerm(termType, variableName);
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
    }

    public PropertyConduitSourceImpl(PropertyAccess access, @ComponentLayer
    ClassFactory classFactory, @ComponentLayer
    PlasticProxyFactory proxyFactory, TypeCoercer typeCoercer, StringInterner interner)
    {
        this.access = access;
        this.classFactory = classFactory;
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
        Class effectiveClass = toEffectiveClass(rootClass);

        MultiKey key = new MultiKey(effectiveClass, expression);

        PropertyConduit result = cache.get(key);

        if (result == null)
        {
            result = build(effectiveClass, expression);
            cache.put(key, result);
        }

        return result;
    }

    private Class toEffectiveClass(Class rootClass)
    {
        Class result = classToEffectiveClass.get(rootClass);

        if (result == null)
        {
            result = classFactory.importClass(rootClass);

            classToEffectiveClass.put(rootClass, result);
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
        classToEffectiveClass.clear();
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
    public static void nullTerm(String term, String expression, Object root)
    {
        String message = String.format("Property '%s' (within property expression '%s', of %s) is null.", term,
                expression, root);

        throw new NullPointerException(message);
    }
}
