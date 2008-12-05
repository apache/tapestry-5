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

    /**
     * Describes all the gory details of one term (one property or method invocation) from within the expression.
     */
    private interface ExpressionTermInfo extends AnnotationProvider
    {

        /**
         * The name of the method to invoke to read the property value, or null.
         */
        String getReadMethodName();

        /**
         * The name of the method to invoke to write the property value, or null. Always null for method terms (which
         * are inherently read-only).
         */
        String getWriteMethodName();

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

    private final PropertyAccess access;

    private final ClassFactory classFactory;

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

    private final PropertyConduit literalTrue = newLiteralConduit(Boolean.class, true);

    private final PropertyConduit literalFalse = newLiteralConduit(Boolean.class, false);

    private final PropertyConduit literalNull = newLiteralConduit(Void.class, null);


    /**
     * Encapsulates the process of building a PropertyConduit instance from an expression.
     */
    class PropertyConduitBuilder
    {
        private final Class rootClass;

        private final ClassFab classFab;

        private final String expression;

        private final Tree tree;

        private Class conduitPropertyType;

        private AnnotationProvider annotationProvider;

        PropertyConduitBuilder(Class rootClass, String expression, Tree tree)
        {
            this.rootClass = rootClass;
            this.expression = expression;
            this.tree = tree;

            String name = ClassFabUtils.generateClassName("PropertyConduit");

            this.classFab = classFactory.newClass(name, BasePropertyConduit.class);
        }

        PropertyConduit createInstance()
        {
            createAccessors();

            classFab.addConstructor(new Class[] {Class.class, AnnotationProvider.class, String.class}, null,
                                    "super($$);");

            String description = String.format("PropertyConduit[%s %s]", rootClass.getName(), expression);

            Class conduitClass = classFab.createClass();

            try
            {
                return (PropertyConduit) conduitClass.getConstructors()[0].newInstance(conduitPropertyType,
                                                                                       annotationProvider,
                                                                                       description);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }


        private void createNoOp(ClassFab classFab, MethodSignature signature, String format, Object... values)
        {
            String message = String.format(format, values);

            String body = String.format("throw new RuntimeException(\"%s\");", message);

            classFab.addMethod(Modifier.PUBLIC, signature, body);
        }

        private boolean isLeaf(Tree node)
        {
            return node.getType() == IDENTIFIER || node.getType() == INVOKE;
        }

        private void createAccessors()
        {
            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s root = (%<s) $1;", ClassFabUtils.toJavaClassName(rootClass));

            builder.addln(
                    "if (root == null) throw new NullPointerException(\"Root object of property expression '%s' is null.\");",
                    expression);

            String previousVariableName = "root";
            Class activeType = rootClass;

            int step = 0;

            Tree node = tree;

            while (!isLeaf(node))
            {
                assertNodeType(node, DEREF, SAFEDEREF);

                step++;

                String variableName = "step" + step;

                activeType = addDereference(builder, activeType, node, previousVariableName, variableName);

                previousVariableName = variableName;

                // Second term is the continuation, possibly another chained DEREF, etc.,
                // or at the end of the expression, an IDENTIFIER or INVOKE
                node = node.getChild(1);
            }

            assertNodeType(node, IDENTIFIER, INVOKE);

            builder.addln("return %s;", previousVariableName);

            builder.end();

            MethodSignature sig = new MethodSignature(activeType, "navigate", new Class[] {Object.class},
                                                      null);

            classFab.addMethod(Modifier.PRIVATE, sig, builder.toString());

            // So, a this point, we have the navigation method written and it covers all but the terminal
            // de-reference.  node is an IDENTIFIER or INVOKE. We're ready to use the navigation
            // method to implement get() and set().

            ExpressionTermInfo info = infoForParseNode(activeType, node);

            createSetter(sig, info);
            createGetter(sig, info);

            conduitPropertyType = info.getType();
            annotationProvider = info;
        }

        private void createSetter(MethodSignature navigateMethod,
                                  ExpressionTermInfo info)
        {
            String methodName = info.getWriteMethodName();

            if (methodName == null)
            {
                createNoOp(classFab, SET_SIGNATURE, "Expression '%s' for class %s is read-only.", expression,
                           rootClass.getName());
                return;
            }

            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s target = %s($1);",
                          ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()),
                          navigateMethod.getName());

            // I.e. due to ?. operator. The navigate method will already have checked for nulls
            // if they are not allowed.

            builder.addln("if (target == null) return;");

            String propertyTypeName = ClassFabUtils.toJavaClassName(info.getType());

            builder.addln("target.%s(%s);", methodName, ClassFabUtils.castReference("$2", propertyTypeName));

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, SET_SIGNATURE, builder.toString());
        }

        private void createGetter(MethodSignature navigateMethod,
                                  ExpressionTermInfo info)
        {
            String methodName = info.getReadMethodName();

            if (methodName == null)
            {
                createNoOp(classFab, GET_SIGNATURE, "Expression %s for class %s is write-only.", expression,
                           rootClass.getName());
                return;
            }

            BodyBuilder builder = new BodyBuilder().begin();

            builder.addln("%s target = %s($1);", ClassFabUtils.toJavaClassName(navigateMethod.getReturnType()),
                          navigateMethod.getName());

            // I.e. due to ?. operator. The navigate method will already have checked for nulls
            // if they are not allowed.

            builder.addln("if (target == null) return null;");

            builder.addln("return ($w) target.%s();", methodName);

            builder.end();

            classFab.addMethod(Modifier.PUBLIC, GET_SIGNATURE, builder.toString());
        }

        /**
         * Part of building the navigation method, adds a de-reference (the '.' or '?.' operator).
         *
         * @param builder              recieves the code for this step in the expression
         * @param activeType           the current type of the expression (this changes with each de-reference, to the
         *                             type just de-referenced)
         * @param node                 the DEREF or SAFEDEREF node
         * @param previousVariableName name of local variable holding previous step in expression
         * @param variableName         name of variable to be assigned with this step in expression
         */
        private Class addDereference(BodyBuilder builder, Class activeType, Tree node, String previousVariableName,
                                     String variableName)
        {// The first child is the term.

            Tree term = node.getChild(0);

            assertNodeType(term, IDENTIFIER, INVOKE);

            // Get info about this property or method.

            ExpressionTermInfo info = infoForParseNode(activeType, term);

            String methodName = info.getReadMethodName();

            if (methodName == null)
                throw new PropertyExpressionException(
                        ServicesMessages.writeOnlyProperty(info.getDescription(), activeType, expression), expression,
                        null);


            boolean nullable = node.getType() == SAFEDEREF;

            // If a primitive type, convert to wrapper type

            Class termType = info.getType();
            Class wrappedType = ClassFabUtils.getWrapperType(termType);

            String termJavaName = ClassFabUtils.toJavaClassName(wrappedType);
            builder.add("%s %s = ", termJavaName, variableName);

            // Casts are needed for primitives, and for the case where
            // generics are involved.

            if (termType.isPrimitive())
            {
                builder.add(" ($w) ");
            }
            else if (info.isCastRequired())
            {
                builder.add(" (%s) ", termJavaName);
            }

            builder.addln("%s.%s();", previousVariableName, info.getReadMethodName());

            if (nullable)
            {
                builder.addln("if (%s == null) return null;", variableName);
            }
            else
            {
                // Perform a null check on intermediate terms.
                builder.addln("if (%s == null) %s.nullTerm(\"%s\", \"%s\", root);",
                              variableName, PropertyConduitSourceImpl.class.getName(), info.getDescription(),
                              expression);
            }

            activeType = wrappedType;
            return activeType;
        }

        private void assertNodeType(Tree node, int... expected)
        {
            int type = node.getType();

            for (int e : expected)
            {
                if (type == e) return;
            }

            List<String> tokenNames = CollectionFactory.newList();

            for (int i = 0; i < expected.length; i++)
                tokenNames.add(PropertyExpressionParser.tokenNames[expected[i]]);

            String message =
                    String.format("Node %s (within expression '%s') was type %s, but was expected (one of) %s.",
                                  PropertyExpressionParser.tokenNames[type],
                                  InternalUtils.joinSorted(tokenNames));

            throw new PropertyExpressionException(message, expression, null);
        }

        private ExpressionTermInfo infoForParseNode(Class activeType, Tree node)
        {
            if (node.getType() == INVOKE)
                return infoForInvokeNode(activeType, node);


            String propertyName = node.getText();

            ClassPropertyAdapter classAdapter = access.getAdapter(activeType);
            final PropertyAdapter adapter = classAdapter.getPropertyAdapter(propertyName);

            if (adapter == null) throw new PropertyExpressionException(
                    ServicesMessages.noSuchProperty(activeType, propertyName, expression,
                                                    classAdapter.getPropertyNames()), expression, null);

            return new ExpressionTermInfo()
            {
                public String getReadMethodName()
                {
                    return name(adapter.getReadMethod());
                }

                public String getWriteMethodName()
                {
                    return name(adapter.getWriteMethod());
                }

                private String name(Method m)
                {
                    return m == null ? null : m.getName();
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

            final String description = methodName + "()";

            try
            {
                final Method method = findMethod(activeType, methodName);

                if (method.getReturnType().equals(void.class))
                    throw new PropertyExpressionException(
                            ServicesMessages.methodIsVoid(description, activeType, expression), expression, null);

                final Class genericType = GenericsUtils.extractGenericReturnType(activeType, method);

                return new ExpressionTermInfo()
                {
                    public String getReadMethodName()
                    {
                        return method.getName();
                    }

                    public String getWriteMethodName()
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
                        return description;
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
                        ServicesMessages.methodNotFound(description, activeType, expression), expression, ex);
            }
        }

        private Method findMethod(Class activeType, String methodName) throws NoSuchMethodException
        {
            for (Method method : activeType.getMethods())
            {

                if (method.getParameterTypes().length == 0 && method.getName().equalsIgnoreCase(methodName))
                    return method;
            }

            throw new NoSuchMethodException(ServicesMessages.noSuchMethod(activeType, methodName));
        }
    }

    public PropertyConduitSourceImpl(PropertyAccess access, @ComponentLayer ClassFactory classFactory)
    {
        this.access = access;
        this.classFactory = classFactory;
    }

    public PropertyConduit create(Class rootClass, String expression)
    {
        Defense.notNull(rootClass, "rootClass");
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
     * @param rootClass
     * @param expression
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

                return newLiteralConduit(Long.class, new Long(tree.getText()));

            case DECIMAL:

                // Leading '+' may screw this up.
                // TODO: Singleton instance for "0.0"?

                return newLiteralConduit(Double.class, new Double(tree.getText()));

            case STRING:

                return newLiteralConduit(String.class, tree.getText());

            case RANGEOP:

                // For the moment, we know that RANGEOP must be paired with two INTEGERS.

                Tree fromNode = tree.getChild(0);
                int from = Integer.parseInt(fromNode.getText());

                Tree toNode = tree.getChild(1);
                int to = Integer.parseInt(toNode.getText());

                IntegerRange ir = new IntegerRange(from, to);

                return newLiteralConduit(IntegerRange.class, ir);

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

    private <T> PropertyConduit newLiteralConduit(Class<T> type, T value)
    {
        return new LiteralPropertyConduit(type, invariantAnnotationProvider,
                                          String.format("LiteralPropertyConduit[%s]", value), value);
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
     * May be invoked from the fabricated PropertyConduit instances.
     */
    public static void nullTerm(String term, String expression, Object root)
    {
        String message = String.format("Property '%s' (within property expression '%s', of %s) is null.",
                                       term, expression, root);

        throw new NullPointerException(message);
    }
}
