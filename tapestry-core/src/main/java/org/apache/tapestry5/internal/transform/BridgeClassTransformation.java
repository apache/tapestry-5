// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.services.MethodInvocationResult;
import org.apache.tapestry5.services.transform.TransformationSupport;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A re-implementation of {@link ClassTransformation} around an instance of {@link PlasticClass}, acting as a bridge
 * for code written against the 5.2 and earlier APIs to work with the 5.3 API.
 *
 * @since 5.3
 */
@SuppressWarnings("deprecation")
public class BridgeClassTransformation implements ClassTransformation
{
    private final PlasticClass plasticClass;

    private final TransformationSupport support;

    private final MutableComponentModel model;

    private static final class WrapMethodHandleAsMethodAccess implements MethodAccess
    {
        private final MethodHandle handle;

        private WrapMethodHandleAsMethodAccess(MethodHandle handle)
        {
            this.handle = handle;
        }

        public MethodInvocationResult invoke(Object target, Object... arguments)
        {
            final org.apache.tapestry5.plastic.MethodInvocationResult plasticResult = handle.invoke(target, arguments);

            return new MethodInvocationResult()
            {
                public void rethrow()
                {
                    plasticResult.rethrow();
                }

                public boolean isFail()
                {
                    return plasticResult.didThrowCheckedException();
                }

                public <T extends Throwable> T getThrown(Class<T> throwableClass)
                {
                    return plasticResult.getCheckedException(throwableClass);
                }

                public Object getReturnValue()
                {
                    return plasticResult.getReturnValue();
                }
            };
        }
    }

    private static <T> ComputedValue<T> toComputedValue(final ComponentValueProvider<T> provider)
    {
        return new ComputedValue<T>()
        {
            public T get(InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);

                return provider.get(resources);
            }
        };
    }

    private static FieldConduit<Object> toFieldConduit(final FieldValueConduit fieldValueConduit)
    {
        return new FieldConduit<Object>()
        {
            public Object get(Object instance, InstanceContext context)
            {
                return fieldValueConduit.get();
            }

            public void set(Object instance, InstanceContext context, Object newValue)
            {
                fieldValueConduit.set(newValue);
            }
        };
    }

    private static TransformMethodSignature toMethodSignature(MethodDescription description)
    {
        return new TransformMethodSignature(description.modifiers, description.returnType, description.methodName,
                description.argumentTypes, description.checkedExceptionTypes);
    }

    private static MethodDescription toMethodDescription(TransformMethodSignature signature)
    {
        return new MethodDescription(signature.getModifiers(), signature.getReturnType(), signature.getMethodName(),
                signature.getParameterTypes(), signature.getSignature(), signature.getExceptionTypes());
    }

    private static class BridgeTransformField implements TransformField
    {
        private static final class WrapFieldHandleAsFieldAccess implements FieldAccess
        {
            private final FieldHandle handle;

            private WrapFieldHandleAsFieldAccess(FieldHandle handle)
            {
                this.handle = handle;
            }

            public void write(Object instance, Object value)
            {
                handle.set(instance, value);
            }

            public Object read(Object instance)
            {
                return handle.get(instance);
            }
        }

        private static final class WrapFieldValueConduitAsFieldConduit implements FieldConduit
        {
            private final FieldValueConduit conduit;

            private WrapFieldValueConduitAsFieldConduit(FieldValueConduit conduit)
            {
                this.conduit = conduit;
            }

            public Object get(Object instance, InstanceContext context)
            {
                return conduit.get();
            }

            public void set(Object instance, InstanceContext context, Object newValue)
            {
                conduit.set(newValue);
            }
        }

        private static final class WrapFieldHandleForFieldValueConduitAsFieldConduit implements FieldConduit<Object>
        {
            private final FieldHandle conduitHandle;

            private WrapFieldHandleForFieldValueConduitAsFieldConduit(FieldHandle conduitHandle)
            {
                this.conduitHandle = conduitHandle;
            }

            private FieldValueConduit conduit(Object instance)
            {
                return (FieldValueConduit) conduitHandle.get(instance);
            }

            public Object get(Object instance, InstanceContext context)
            {
                return conduit(instance).get();
            }

            public void set(Object instance, InstanceContext context, Object newValue)
            {
                conduit(instance).set(newValue);
            }
        }

        private static final class WrapCVP_FieldValueConduit_as_CV_FieldConduit implements
                ComputedValue<FieldConduit<Object>>
        {
            private final ComponentValueProvider<FieldValueConduit> conduitProvider;

            private WrapCVP_FieldValueConduit_as_CV_FieldConduit(
                    ComponentValueProvider<FieldValueConduit> conduitProvider)
            {
                this.conduitProvider = conduitProvider;
            }

            public FieldConduit<Object> get(InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);

                FieldValueConduit fieldValueConduit = conduitProvider.get(resources);

                return toFieldConduit(fieldValueConduit);
            }
        }

        private final PlasticField plasticField;

        public BridgeTransformField(PlasticField plasticField)
        {
            this.plasticField = plasticField;
        }

        public int compareTo(TransformField o)
        {
            throw new IllegalStateException("compareTo() not yet implemented.");
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
        {
            return plasticField.getAnnotation(annotationClass);
        }

        public String getName()
        {
            return plasticField.getName();
        }

        public String getType()
        {
            return plasticField.getTypeName();
        }

        public String getSignature()
        {
            return plasticField.getGenericSignature();
        }

        public void claim(Object tag)
        {
            plasticField.claim(tag);
        }

        public void replaceAccess(final ComponentValueProvider<FieldValueConduit> conduitProvider)
        {
            plasticField.setComputedConduit(new WrapCVP_FieldValueConduit_as_CV_FieldConduit(conduitProvider));
        }

        /**
         * We assume that the conduit field contains a {@link FieldValueConduit}, and that the field
         * was introduced through this instance of BridgeClassTransformation.
         */
        public void replaceAccess(TransformField conduitField)
        {
            // Ugly:
            PlasticField conduitFieldPlastic = ((BridgeTransformField) conduitField).plasticField;

            final FieldHandle conduitHandle = conduitFieldPlastic.getHandle();

            plasticField.setConduit(new WrapFieldHandleForFieldValueConduitAsFieldConduit(conduitHandle));
        }

        public void replaceAccess(final FieldValueConduit conduit)
        {
            plasticField.setConduit(new WrapFieldValueConduitAsFieldConduit(conduit));
        }

        public int getModifiers()
        {
            return plasticField.getModifiers();
        }

        public void inject(Object value)
        {
            plasticField.inject(value);
        }

        public <T> void injectIndirect(ComponentValueProvider<T> provider)
        {
            plasticField.injectComputed(toComputedValue(provider));
        }

        public FieldAccess getAccess()
        {
            final FieldHandle handle = plasticField.getHandle();

            return new WrapFieldHandleAsFieldAccess(handle);
        }
    }

    private static BridgeTransformField toTransformField(PlasticField plasticField)
    {
        return new BridgeTransformField(plasticField);
    }

    private static Mapper<PlasticField, TransformField> TO_TRANSFORM_FIELD = new Mapper<PlasticField, TransformField>()
    {
        public TransformField map(PlasticField element)
        {
            return toTransformField(element);
        }
    };

    private static final class WrapMethodAdviceAsComponentMethodAdvice implements MethodAdvice
    {
        private final ComponentMethodAdvice advice;

        private WrapMethodAdviceAsComponentMethodAdvice(ComponentMethodAdvice advice)
        {
            this.advice = advice;
        }

        public void advise(final MethodInvocation invocation)
        {
            advice.advise(new ComponentMethodInvocation()
            {
                public ComponentResources getComponentResources()
                {
                    return invocation.getInstanceContext().get(ComponentResources.class);
                }

                public void rethrow()
                {
                    invocation.rethrow();
                }

                public void proceed()
                {
                    invocation.proceed();
                }

                public void overrideThrown(Exception thrown)
                {
                    invocation.setCheckedException(thrown);
                }

                public void overrideResult(Object newResult)
                {
                    invocation.setReturnValue(newResult);
                }

                public void override(int index, Object newParameter)
                {
                    invocation.setParameter(index, newParameter);
                }

                public boolean isFail()
                {
                    return invocation.didThrowCheckedException();
                }

                public <T extends Throwable> T getThrown(Class<T> throwableClass)
                {
                    return invocation.getCheckedException(throwableClass);
                }

                public Class getResultType()
                {
                    return method().getReturnType();
                }

                public Object getResult()
                {
                    return invocation.getReturnValue();
                }

                public Class getParameterType(int index)
                {
                    return method().getParameterTypes()[index];
                }

                public int getParameterCount()
                {
                    return method().getParameterTypes().length;
                }

                public Object getParameter(int index)
                {
                    return invocation.getParameter(index);
                }

                public String getMethodName()
                {
                    return method().getName();
                }

                private Method method()
                {
                    return invocation.getMethod();
                }

                public <T extends Annotation> T getMethodAnnotation(Class<T> annotationClass)
                {
                    return invocation.getAnnotation(annotationClass);
                }

                public Component getInstance()
                {
                    return (Component) invocation.getInstance();
                }
            });
        }
    }

    private static final class WrapAfterComponentInstanceOperationAsMethodAdvice implements MethodAdvice
    {
        private final ComponentInstanceOperation operation;

        private WrapAfterComponentInstanceOperationAsMethodAdvice(ComponentInstanceOperation operation)
        {
            this.operation = operation;
        }

        public void advise(MethodInvocation invocation)
        {
            invocation.proceed();

            operation.invoke((Component) invocation.getInstance());
        }
    }

    private static final class WrapBeforeComponentInstanceOperationAsMethodAdvice implements MethodAdvice
    {
        private final ComponentInstanceOperation operation;

        private WrapBeforeComponentInstanceOperationAsMethodAdvice(ComponentInstanceOperation operation)
        {
            this.operation = operation;
        }

        public void advise(MethodInvocation invocation)
        {
            operation.invoke((Component) invocation.getInstance());

            invocation.proceed();
        }
    }

    private class BridgeTransformMethod implements TransformMethod
    {
        private final PlasticMethod plasticMethod;

        private TransformMethodSignature signature;

        public BridgeTransformMethod(PlasticMethod plasticMethod)
        {
            this.plasticMethod = plasticMethod;
        }

        public int compareTo(TransformMethod o)
        {
            throw new IllegalStateException("compareTo() not yet implemented.");
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
        {
            return plasticMethod.getAnnotation(annotationClass);
        }

        public TransformMethodSignature getSignature()
        {
            if (signature == null)
            {
                signature = toMethodSignature(plasticMethod.getDescription());
            }

            return signature;
        }

        public String getName()
        {
            return plasticMethod.getDescription().methodName;
        }

        public MethodAccess getAccess()
        {
            final MethodHandle handle = plasticMethod.getHandle();

            return new WrapMethodHandleAsMethodAccess(handle);
        }

        public void addAdvice(final ComponentMethodAdvice advice)
        {
            MethodAdvice plasticAdvice = new WrapMethodAdviceAsComponentMethodAdvice(advice);

            plasticMethod.addAdvice(plasticAdvice);
        }

        public void addOperationBefore(final ComponentInstanceOperation operation)
        {
            plasticMethod.addAdvice(new WrapBeforeComponentInstanceOperationAsMethodAdvice(operation));
        }

        public void addOperationAfter(final ComponentInstanceOperation operation)
        {
            plasticMethod.addAdvice(new WrapAfterComponentInstanceOperationAsMethodAdvice(operation));
        }

        public String getMethodIdentifier()
        {
            return String.format("%s.%s", plasticClass.getClassName(), getSignature().getMediumDescription());
        }

        public boolean isOverride()
        {
            return plasticMethod.isOverride();
        }

        public <A extends Annotation> A getParameterAnnotation(int index, Class<A> annotationType)
        {
            return plasticMethod.getParameters().get(index).getAnnotation(annotationType);
        }
    }

    private final Mapper<PlasticMethod, TransformMethod> toTransformMethod = new Mapper<PlasticMethod, TransformMethod>()
    {
        public TransformMethod map(PlasticMethod element)
        {
            return new BridgeTransformMethod(element);
        }
    };

    public BridgeClassTransformation(PlasticClass plasticClass, TransformationSupport support,
                                     MutableComponentModel model)
    {
        this.plasticClass = plasticClass;
        this.support = support;
        this.model = model;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return plasticClass.getAnnotation(annotationClass);
    }

    public String getClassName()
    {
        return plasticClass.getClassName();
    }

    public String newMemberName(String suggested)
    {
        return newMemberName("_", PlasticInternalUtils.toPropertyName(suggested));
    }

    public String newMemberName(String prefix, String baseName)
    {
        return new StringBuilder(prefix).append(PlasticUtils.nextUID()).append(baseName).toString();
    }

    public List<TransformField> matchFieldsWithAnnotation(Class<? extends Annotation> annotationClass)
    {
        return F.flow(plasticClass.getFieldsWithAnnotation(annotationClass)).map(TO_TRANSFORM_FIELD).toList();
    }

    public List<TransformMethod> matchMethods(Predicate<TransformMethod> predicate)
    {
        return F.flow(plasticClass.getMethods()).map(toTransformMethod).filter(predicate).toList();
    }

    public List<TransformMethod> matchMethodsWithAnnotation(Class<? extends Annotation> annotationType)
    {
        return F.flow(plasticClass.getMethodsWithAnnotation(annotationType)).map(toTransformMethod).toList();
    }

    public List<TransformField> matchFields(Predicate<TransformField> predicate)
    {
        return F.flow(plasticClass.getAllFields()).map(TO_TRANSFORM_FIELD).filter(predicate).toList();
    }

    public TransformField getField(String fieldName)
    {
        for (PlasticField f : plasticClass.getAllFields())
        {
            if (f.getName().equals(fieldName))
            {
                return toTransformField(f);
            }
        }

        throw new IllegalArgumentException(String.format("Class %s does not contain a field named '%s'.",
                plasticClass.getClassName(), fieldName));
    }

    public List<TransformField> matchUnclaimedFields()
    {
        return F.flow(plasticClass.getUnclaimedFields()).map(TO_TRANSFORM_FIELD).toList();
    }

    public boolean isField(String fieldName)
    {
        throw new IllegalArgumentException("isField() not yet implemented.");
    }

    public TransformField createField(int modifiers, String type, String suggestedName)
    {
        // TODO: modifiers are ignored

        PlasticField newField = plasticClass.introduceField(type, suggestedName);

        return toTransformField(newField);
    }

    public String addInjectedField(Class type, String suggestedName, Object value)
    {
        // TODO: The injected field is not actually protected or shared

        PlasticField field = plasticClass.introduceField(type, suggestedName).inject(value);

        return field.getName();
    }

    public <T> TransformField addIndirectInjectedField(Class<T> type, String suggestedName,
                                                       ComponentValueProvider<T> provider)
    {

        PlasticField field = plasticClass.introduceField(type, suggestedName).injectComputed(toComputedValue(provider));

        return toTransformField(field);
    }

    public void addImplementedInterface(Class interfaceClass)
    {
        plasticClass.introduceInterface(interfaceClass);
    }

    public Class toClass(String type)
    {
        return support.toClass(type);
    }

    public Logger getLogger()
    {
        return model.getLogger();
    }

    public boolean isRootTransformation()
    {
        return support.isRootTransformation();
    }

    public TransformMethod getOrCreateMethod(TransformMethodSignature signature)
    {
        MethodDescription md = toMethodDescription(signature);

        PlasticMethod plasticMethod = plasticClass.introduceMethod(md);

        return new BridgeTransformMethod(plasticMethod);
    }

    public boolean isDeclaredMethod(TransformMethodSignature signature)
    {
        final MethodDescription md = toMethodDescription(signature);

        return !F.flow(plasticClass.getMethods()).filter(new Predicate<PlasticMethod>()
        {
            public boolean accept(PlasticMethod element)
            {
                return element.getDescription().equals(md);
            }
        }).isEmpty();
    }

    public void addComponentEventHandler(String eventType, int minContextValues, String methodDescription,
                                         ComponentEventHandler handler)
    {
        support.addEventHandler(eventType, minContextValues, methodDescription, handler);
    }
}
