// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.internal.antlr.PropertyExpressionLexer;
import org.apache.tapestry5.internal.antlr.PropertyExpressionParser;
import static org.apache.tapestry5.internal.antlr.PropertyExpressionParser.*;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.internal.util.MultiKey;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.NullAnnotationProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.GenericsUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.services.ComponentLayer;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.PropertyConduitSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class PropertyConduitSourceImpl implements PropertyConduitSource, InvalidationListener
{
    private static final MethodSignature GET_SIGNATURE = new MethodSignature(Object.class, "get",
                                                                             new Class[] {Object.class}, null);

    private static final MethodSignature SET_SIGNATURE = new MethodSignature(void.class, "set",
                                                                             new Class[] {Object.class, Object.class},
                                                                             null);

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
     * Describes all the gory details of one term (one property or method invocation) from within the expression.
     */
    private interface ExpressionTermInfo extends AnnotationProvider
    {

        /**
         * The method to invoke to read the property value, or null.
         */
        Method getReadMethod();

        /**
         * The method to invoke to write the property value, or null. Always null for method terms (which are inherently
         * read-only).
         */
        Method getWriteMethod();

        /**
         * The return type of the method, or the type of the property.
         */
        Class getType();

        /**
         * True if an explicit cast to the return type is needed (typically because of generics).
         */
        boolean isCastRequired();

        /**
         * Returns a user-presentable name identifying the property or method name.
         */
        String getDescription();
    }

    private class GeneratedTerm
    {
        private final Class type;

        private final String variableName;

        private GeneratedTerm(Class type, String variableName)
        {
            this.type = type;
            this.variableName = variableName;
        }

        /**
         * The name of the variable that contains the evaluation of the term.
         */
        String getVariableName()
        {
            return variableName;
        }

        /**
         * The type of the variable (used to determine what methods/properties may be dereferenced).
         */
        Class getType()
        {
            return type;
        }
    }

    private final PropertyAccess access;

    private final ClassFactory classFactory;

    private final TypeCoercer typeCoercer;

    /**
     * Because of stuff like Hibernate, we sometimes start with a subclass in some inaccessible class loader and need to
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
     * Encapsulates the process of building a PropertyConduit instance from an expression.
     */
    class PropertyConduitBuilder
    {
        private final Class rootType;

        private final ClassFab classFab;

        private final String expression;

        private final Tree tree;

        private Class conduitPropertyType;

        private AnnotationProvider annotationProvider = nullAnnotationProvider;

        // Used to create unique variable names.

        private int variableIndex = 0;

        private final List<ConstructorParameter> parameters = CollectionFactory.newList();

        private final BodyBuilder navBuilder = new BodyBuilder();

        PropertyConduitBuilder(Class rootType, String expression, Tree tree)
        {
            this.rootType = rootType;
            this.expression = expression;
            this.tree = tree;

            String name = ClassFabUtils.generateClassName("PropertyConduit");

            this.classFab = classFactory.newClass(name, BasePropertyConduit.class);
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

            // $1, $2, $3, $4 ...

            types.add(Class.class);
            types.add(AnnotationProvider.class);
            types.add(String.class);
            types.add(TypeCoercer.class);

            List<Object> values = CollectionFactory.newList();

            values.add(conduitPropertyType);
            values.add(annotationProvider);
            values.add(String.format("PropertyConduit[%s %s]", rootType.getName(), expression));
            values.add(typeCoercer);

            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("super($1,$2,$3,$4);");

            int index = 5;

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

        String addInjection(Class fieldType, Object fieldValue)
        {
            String fieldName =
                    String.format("injected_%s_%d",
                                  toSimpleName(fieldType),
                                  parameters.size());

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

            return type == IDENTIFIER || type == INVOKE || type == RANGEOP;
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

            MethodSignature sig = new MethodSignature(rootType, "getRoot", new Class[] {Object.class}, null);

            classFab.addMethod(Modifier.PRIVATE, sig, builder.toString());
        }

        private void addRootVariable(BodyBuilder builder)
        {
            builder.addln("%s root = getRoot($1);",
                          ClassFabUtils.toJavaClassName(rootType));
        }

        private void createAccessors()
        {
            createGetRoot();

            navBuilder.begin();

            String previousVariableName = "$1";
            Class activeType = rootType;

            Tree node = tree;

            while (!isLeaf(node))
            {
                GeneratedTerm term = processDerefNode(navBuilder, activeType, node, previousVariableName);

                activeType = term.getType();

                previousVariableName = term.getVariableName();

                // Second term is the continuation, possibly another chained DEREF, etc.
                node = node.getChild(1);
            }

            navBuilder.addln("return %s;", previousVariableName);

            navBuilder.end();

            MethodSignature sig = new MethodSignature(activeType, "navigate", new Class[] {rootType},
                                                      null);

            classFab.addMethod(Modifier.PRIVATE, sig, navBuilder.toString());


            createGetterAndSetter(activeType, sig, node);
        }

        private void createGetterAndSetter(Class activeType, MethodSignature navigateMethod, Tree node)
        {
            switch (node.getType())
            {
                case IDENTIFIER:
                case INVOKE:

                    // So, a this point, we have the navigation method written and it covers all but the terminal
                    // de-reference.  node is an IDENTIFIER or INVOKE. We're ready to use the navigation
                    // method to implement get() and set().

                    ExpressionTermInfo info = infoForPropertyOrMethod(activeType, node);

                    createSetter(navigateMethod, info);
                    createGetter(navigateMethod, node, info);

                    conduitPropertyType = info.getType();
                    annotationProvider = info;

                    return;

                case RANGEOP:

                    // As currently implemented, RANGEOP can only appear as the top level, which
                    // means we didn't need the navigate method after all.

                    createRangeOpGetter(navigateMethod, node);
                    createNoOpSetter();

                    conduitPropertyType = IntegerRange.class;

                    return;


                default:
                    throw unexpectedNodeType(node, IDENTIFIER, INVOKE, RANGEOP);
            }
        }

        private void createRangeOpGetter(MethodSignature navigateMethod, Tree node)
        {
            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            String fromVar = subexpression(builder, node.getChild(0)).getVariableName();
            String toVar = subexpression(builder, node.getChild(1)).getVariableName();

            builder.addln("return new %s(toInt(%s), toInt(%s));", IntegerRange.class.getName(), fromVar, toVar);

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }


        /**
         * Evalutates the node as a sub expression, storing the result into a new variable, whose name is returned.
         *
         * @param builder to receive generated code
         * @param node    root of tree of nodes to be evaluated
         * @return name of variable containing expression
         */
        private GeneratedTerm subexpression(BodyBuilder builder, Tree node)
        {
            String previousVariableName = "root";
            Class activeType = rootType;

            while (node != null)
            {
                switch (node.getType())
                {
                    case INTEGER:

                        Long integerValue = new Long(node.getText());

                        previousVariableName = addInjection(Long.class, integerValue);
                        activeType = Long.class;

                        node = null;

                        break;

                    case DECIMAL:

                        Double decimalValue = new Double(node.getText());
                        previousVariableName = addInjection(Double.class, decimalValue);
                        activeType = Double.class;

                        node = null;

                        break;

                    case STRING:

                        String stringValue = node.getText();
                        previousVariableName = addInjection(String.class, stringValue);
                        activeType = String.class;

                        node = null;

                        break;

                    case DEREF:
                    case SAFEDEREF:

                        GeneratedTerm generated = processDerefNode(builder, activeType, node, previousVariableName);

                        previousVariableName = generated.getVariableName();
                        activeType = generated.getType();

                        node = node.getChild(1);

                        break;

                    case IDENTIFIER:
                    case INVOKE:

                        generated = addAccessForPropertyOrMethod(builder, activeType, node, previousVariableName, true);

                        previousVariableName = generated.getVariableName();
                        activeType = generated.getType();

                        node = null;

                        break;

                    default:
                        throw unexpectedNodeType(node, INTEGER, DECIMAL, STRING, DEREF, SAFEDEREF, IDENTIFIER, INVOKE);
                }
            }

            return new GeneratedTerm(activeType, previousVariableName);
        }


        private void createSetter(MethodSignature navigateMethod,
                                  ExpressionTermInfo info)
        {
            // A write method will only be identified if the info is a writable property.
            // Other alternatives: a method as the final term, or a read-only property.

            Method method = info.getWriteMethod();

            if (method == null)
            {
                createNoOpSetter();
                return;
            }

            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("%s target = navigate(root);",
                          ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()));

            // I.e. due to ?. operator. The navigate method will already have checked for nulls
            // if they are not allowed.

            builder.addln("if (target == null) return;");

            String propertyTypeName = ClassFabUtils.toJavaClassName(info.getType());

            builder.addln("target.%s(%s);", method.getName(), ClassFabUtils.castReference("$2", propertyTypeName));

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());
        }

        private void createNoOpSetter()
        {
            createNoOp(classFab, SET_SIGNATURE, "Expression '%s' for class %s is read-only.", expression,
                       rootType.getName());
        }

        private void createGetter(MethodSignature navigateMethod,
                                  Tree node,
                                  ExpressionTermInfo info)
        {
            Method method = info.getReadMethod();

            if (method == null)
            {
                createNoOp(classFab, GET_SIGNATURE, "Expression %s for class %s is write-only.", expression,
                           rootType.getName());
                return;
            }

            BodyBuilder builder = new BodyBuilder().begin();

            addRootVariable(builder);

            builder.addln("%s target = navigate(root);", ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()));

            // I.e. due to ?. operator. The navigate method will already have checked for nulls
            // if they are not allowed.

            builder.addln("if (target == null) return null;");

            builder.addln("return ($w) target.%s;", createMethodInvocation(builder, node, method));

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }

        private String createMethodInvocation(BodyBuilder bodyBuilder, Tree methodInvocation, Method method)
        {
            Class[] parameterTypes = method.getParameterTypes();

            StringBuilder builder = new StringBuilder();

            builder.append(method.getName());
            builder.append("(");

            for (int i = 0; i < parameterTypes.length; i++)
            {
                // child(0) is the method name, child(1) is the first parameter, etc.

                GeneratedTerm generatedTerm = subexpression(bodyBuilder, methodInvocation.getChild(i + 1));
                String variableName = generatedTerm.getVariableName();

                Class actualType = generatedTerm.getType();

                Class parameterType = parameterTypes[i];

                if (!parameterType.isAssignableFrom(actualType))
                {
                    String coerced = nextVariableName(parameterType);

                    String call = String.format("coerce(($w) %s, %s)", variableName,
                                                addInjection(Class.class, parameterType));

                    String parameterTypeName = ClassFabUtils.toJavaClassName(parameterType);

                    bodyBuilder.addln("%s %s = %s;",
                                      parameterTypeName, coerced, ClassFabUtils.castReference(call, parameterTypeName));

                    variableName = coerced;
                }

                // TODO: Casting, coercing, unwrapping primitives.

                if (i > 0) builder.append(", ");

                builder.append(variableName);
            }

            return builder.append(")").toString();
        }


        /**
         * Extends the navigate method for a node, which will be a DEREF or SAFEDERF.
         */
        private GeneratedTerm processDerefNode(BodyBuilder builder, Class activeType, Tree node,
                                               String previousVariableName
        )
        {
            // The first child is the term.

            Tree term = node.getChild(0);

            boolean allowNull = node.getType() == SAFEDEREF;


            // Returns the type of the method/property ... this is the wrapped (i.e. java.lang.Integer) type if
            // the real type is primitive. It also reflects generics information that may have been associated
            // with the underlying method.


            return addAccessForPropertyOrMethod(builder, activeType, term, previousVariableName,
                                                allowNull);
        }

        private String nextVariableName(Class type)
        {
            return String.format("var_%s_%d",
                                 toSimpleName(type), variableIndex++);
        }


        private String toSimpleName(Class type)
        {
            // TODO: handle arrays types
            return InternalUtils.lastTerm(type.getName());
        }

        private GeneratedTerm addAccessForPropertyOrMethod(BodyBuilder builder, Class activeType, Tree term,
                                                           String previousVariableName,
                                                           boolean allowNull)
        {
            assertNodeType(term, IDENTIFIER, INVOKE);

            // Get info about this property or method.

            ExpressionTermInfo info = infoForPropertyOrMethod(activeType, term);

            Method method = info.getReadMethod();

            if (method == null)
                throw new PropertyExpressionException(
                        ServicesMessages.writeOnlyProperty(info.getDescription(), activeType, expression),
                        expression,
                        null);


            // If a primitive type, convert to wrapper type

            Class termType = info.getType();
            final Class wrappedType = ClassFabUtils.getWrapperType(termType);

            String wrapperTypeName = ClassFabUtils.toJavaClassName(wrappedType);

            final String variableName = nextVariableName(wrappedType);

            String invocation = createMethodInvocation(builder, term, method);

            builder.add("%s %s = ", wrapperTypeName, variableName);

            // Casts are needed for primitives, and for the case where
            // generics are involved.

            if (termType.isPrimitive())
            {
                builder.add(" ($w) ");
            }
            else if (info.isCastRequired())
            {
                builder.add(" (%s) ", wrapperTypeName);
            }

            builder.addln("%s.%s;", previousVariableName, invocation);

            if (allowNull)
            {
                builder.addln("if (%s == null) return null;", variableName);
            }
            else
            {
                // Perform a null check on intermediate terms.
                builder.addln("if (%s == null) %s.nullTerm(\"%s\", \"%s\", $1);",
                              variableName, PropertyConduitSourceImpl.class.getName(), info.getDescription(),
                              expression);
            }

            return new GeneratedTerm(wrappedType, variableName);
        }

        private void assertNodeType(Tree node, int... expected)
        {
            int type = node.getType();

            for (int e : expected)
            {
                if (type == e) return;
            }

            throw unexpectedNodeType(node, expected);
        }

        private RuntimeException unexpectedNodeType(Tree node, int... expected)
        {
            List<String> tokenNames = CollectionFactory.newList();

            for (int i = 0; i < expected.length; i++)
                tokenNames.add(PropertyExpressionParser.tokenNames[expected[i]]);

            String message =
                    String.format("Node %s (within expression '%s') was type %s, but was expected to be (one of) %s.",
                                  node.toStringTree(),
                                  expression,
                                  PropertyExpressionParser.tokenNames[node.getType()],
                                  InternalUtils.joinSorted(tokenNames));

            return new PropertyExpressionException(message, expression, null);
        }

        private ExpressionTermInfo infoForPropertyOrMethod(Class activeType, Tree node)
        {
            if (node.getType() == INVOKE)
                return infoForInvokeNode(activeType, node);

            return infoForPropertyNode(activeType, node);
        }

        private ExpressionTermInfo infoForPropertyNode(Class activeType, Tree node)
        {
            String propertyName = node.getText();

            ClassPropertyAdapter classAdapter = access.getAdapter(activeType);
            final PropertyAdapter adapter = classAdapter.getPropertyAdapter(propertyName);

            if (adapter == null) throw new PropertyExpressionException(
                    ServicesMessages.noSuchProperty(activeType, propertyName, expression,
                                                    classAdapter.getPropertyNames()), expression, null);

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

                public Class getType()
                {
                    return adapter.getType();
                }

                public boolean isCastRequired()
                {
                    return adapter.isCastRequired();
                }

                public String getDescription()
                {
                    return adapter.getName();
                }

                public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
                {
                    return adapter.getAnnotation(annotationClass);
                }
            };
        }

        private ExpressionTermInfo infoForInvokeNode(Class activeType, Tree node)
        {
            String methodName = node.getChild(0).getText();

            int parameterCount = node.getChildCount() - 1;

            try
            {
                final Method method = findMethod(activeType, methodName, parameterCount);

                if (method.getReturnType().equals(void.class))
                    throw new PropertyExpressionException(
                            ServicesMessages.methodIsVoid(methodName, activeType, expression), expression, null);

                final Class genericType = GenericsUtils.extractGenericReturnType(activeType, method);

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

                    public Class getType()
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
                };
            }
            catch (NoSuchMethodException ex)
            {
                throw new PropertyExpressionException(
                        ServicesMessages.methodNotFound(methodName, activeType, expression), expression, ex);
            }
        }

        private Method findMethod(Class activeType, String methodName, int parameterCount) throws NoSuchMethodException
        {
            for (Method method : activeType.getMethods())
            {

                if (method.getParameterTypes().length == parameterCount && method.getName().equalsIgnoreCase(
                        methodName))
                    return method;
            }

            throw new NoSuchMethodException(ServicesMessages.noSuchMethod(activeType, methodName));
        }
    }

    public PropertyConduitSourceImpl(PropertyAccess access, @ComponentLayer ClassFactory classFactory,
                                     TypeCoercer typeCoercer)
    {
        this.access = access;
        this.classFactory = classFactory;
        this.typeCoercer = typeCoercer;

        literalTrue = createLiteralConduit(Boolean.class, true);
        literalFalse = createLiteralConduit(Boolean.class, false);
        literalNull = createLiteralConduit(Void.class, null);
    }

    public PropertyConduit create(Class rootClass, String expression)
    {
        Defense.notNull(rootClass, "rootType");
        Defense.notBlank(expression, "expression");

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
     * Clears its caches when the component class loader is invalidated; this is because it will be common to generate
     * conduits rooted in a component class (which will no longer be valid and must be released to the garbage
     * collector).
     */
    public void objectWasInvalidated()
    {
        cache.clear();
        classToEffectiveClass.clear();
    }


    /**
     * Builds a subclass of {@link BasePropertyConduit} that implements the get() and set() methods and overrides the
     * constructor. In a worst-case race condition, we may build two (or more) conduits for the same
     * rootClass/expression, and it will get sorted out when the conduit is stored into the cache.
     *
     * @param rootClass  class of root object for expression evaluation
     * @param expression expression to be evaluated
     * @return the conduit
     */
    private PropertyConduit build(final Class rootClass, String expression)
    {
        Tree tree = parse(expression);

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

                // If the range is defined as integers (not properties, etc.) then
                // it is possible to calcualte the value here, once, and not build
                // a new class.

                if (fromNode.getType() != INTEGER || toNode.getType() != INTEGER) break;

                int from = Integer.parseInt(fromNode.getText());
                int to = Integer.parseInt(toNode.getText());

                IntegerRange ir = new IntegerRange(from, to);

                return createLiteralConduit(IntegerRange.class, ir);

            case THIS:

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

            default:
                break;
        }

        return new PropertyConduitBuilder(rootClass, expression, tree).createInstance();
    }

    private <T> PropertyConduit createLiteralConduit(Class<T> type, T value)
    {
        return new LiteralPropertyConduit(type, invariantAnnotationProvider,
                                          String.format("LiteralPropertyConduit[%s]", value), typeCoercer, value);
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
        catch (RecognitionException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * May be invoked from fabricated PropertyConduit instances.
     */
    public static void nullTerm(String term, String expression, Object root)
    {
        String message = String.format("Property '%s' (within property expression '%s', of %s) is null.",
                                       term, expression, root);

        throw new NullPointerException(message);
    }
}
