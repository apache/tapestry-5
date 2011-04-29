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

import java.lang.annotation.Annotation;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.FieldHandle;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.MethodHandle;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentEventHandler;
import org.apache.tapestry5.services.ComponentInstanceOperation;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.MethodAccess;
import org.apache.tapestry5.services.MethodInvocationResult;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformField;
import org.apache.tapestry5.services.TransformMethod;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.services.transform.TransformationSupport;
import org.slf4j.Logger;

/**
 * A re-implementation of {@link ClassTransformation} around an instance of {@link PlasticClass}, acting as a bridge
 * for code written against the 5.2 and earlier APIs to work with the 5.3 API.
 * 
 * @since 5.3.0
 */
@SuppressWarnings("deprecation")
public class BridgeClassTransformation implements ClassTransformation
{
    private final PlasticClass plasticClass;

    private final TransformationSupport support;

    private final MutableComponentModel model;

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

    private static class BridgeTransformField implements TransformField
    {
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
            throw new IllegalStateException("getSignature() not yet implemented.");
        }

        public void claim(Object tag)
        {
            plasticField.claim(tag);
        }

        public void replaceAccess(final ComponentValueProvider<FieldValueConduit> conduitProvider)
        {
            throw new IllegalStateException("replaceAccess() not yet implemented.");
        }

        public void replaceAccess(TransformField conduitField)
        {
            throw new IllegalStateException("replaceAccess() not yet implemented.");
        }

        public void replaceAccess(final FieldValueConduit conduit)
        {
            plasticField.setConduit(new FieldConduit()
            {
                public Object get(InstanceContext context)
                {
                    return conduit.get();
                }

                public void set(InstanceContext context, Object newValue)
                {
                    conduit.set(newValue);
                }
            });
        }

        public int getModifiers()
        {
            throw new IllegalStateException("getModifiers() not yet implemented.");
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

            return new FieldAccess()
            {

                public void write(Object instance, Object value)
                {
                    handle.set(instance, value);
                }

                public Object read(Object instance)
                {
                    return handle.get(instance);
                }
            };
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

    private static class BridgeTransformMethod implements TransformMethod
    {
        private final PlasticMethod plasticMethod;

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
            throw new IllegalStateException("getSignature() not yet implemented.");
        }

        public String getName()
        {
            return plasticMethod.getDescription().methodName;
        }

        public MethodAccess getAccess()
        {
            final MethodHandle handle = plasticMethod.getHandle();

            return new MethodAccess()
            {
                public MethodInvocationResult invoke(Object target, Object... arguments)
                {
                    final org.apache.tapestry5.plastic.MethodInvocationResult plasticResult = handle.invoke(target,
                            arguments);

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
            };
        }

        public void addAdvice(final ComponentMethodAdvice advice)
        {
            MethodAdvice plasticAdvice = new MethodAdvice()
            {
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
                            return invocation.getReturnType();
                        }

                        public Object getResult()
                        {
                            return invocation.getReturnValue();
                        }

                        public Class getParameterType(int index)
                        {
                            return invocation.getParameterType(index);
                        }

                        public int getParameterCount()
                        {
                            return invocation.getParameterCount();
                        }

                        public Object getParameter(int index)
                        {
                            return invocation.getParameter(index);
                        }

                        public String getMethodName()
                        {
                            return invocation.getMethodName();
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
            };

            plasticMethod.addAdvice(plasticAdvice);
        }

        public void addOperationBefore(final ComponentInstanceOperation operation)
        {
            addAdvice(new ComponentMethodAdvice()
            {
                public void advise(ComponentMethodInvocation invocation)
                {
                    operation.invoke(invocation.getInstance());

                    invocation.proceed();
                }
            });
        }

        public void addOperationAfter(final ComponentInstanceOperation operation)
        {
            addAdvice(new ComponentMethodAdvice()
            {
                public void advise(ComponentMethodInvocation invocation)
                {
                    invocation.proceed();

                    operation.invoke(invocation.getInstance());
                }
            });
        }

        public String getMethodIdentifier()
        {
            throw new IllegalStateException("getMethodIdentifer() not yet implemented");
        }

        public boolean isOverride()
        {
            throw new IllegalStateException("isOverride() not yet implemented");
        }

        public <A extends Annotation> A getParameterAnnotation(int index, Class<A> annotationType)
        {
            return plasticMethod.getParameters().get(index).getAnnotation(annotationType);
        }

    }

    private static final Mapper<PlasticMethod, TransformMethod> TO_TRANSFORM_METHOD = new Mapper<PlasticMethod, TransformMethod>()
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
        return F.flow(plasticClass.getMethods()).map(TO_TRANSFORM_METHOD).filter(predicate).toList();
    }

    public List<TransformMethod> matchMethodsWithAnnotation(Class<? extends Annotation> annotationType)
    {
        return F.flow(plasticClass.getMethodsWithAnnotation(annotationType)).map(TO_TRANSFORM_METHOD).toList();
    }

    public List<TransformField> matchFields(Predicate<TransformField> predicate)
    {
        return F.flow(plasticClass.getUnclaimedFields()).map(TO_TRANSFORM_FIELD).filter(predicate).toList();
    }

    public TransformField getField(String fieldName)
    {
        for (PlasticField f : plasticClass.getAllFields())
        {
            if (f.getName().equals(fieldName)) { return toTransformField(f); }
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
        MethodDescription md = new MethodDescription(signature.getModifiers(), signature.getReturnType(),
                signature.getMethodName(), signature.getParameterTypes(), signature.getExceptionTypes());

        PlasticMethod plasticMethod = plasticClass.introduceMethod(md);

        return new BridgeTransformMethod(plasticMethod);
    }

    public boolean isDeclaredMethod(TransformMethodSignature signature)
    {
        throw new IllegalArgumentException("isDeclaredMethod() not yet implemented.");
    }

    // TODO: This is very handy, there should be an additional object passed around that encapsulates
    // this kind of logic.

    public void addComponentEventHandler(String eventType, int minContextValues, String methodDescription,
            ComponentEventHandler handler)
    {
        assert InternalUtils.isNonBlank(eventType);
        assert InternalUtils.isNonBlank(methodDescription);
        assert handler != null;

        model.addEventHandler(eventType);

        getOrCreateMethod(TransformConstants.DISPATCH_COMPONENT_EVENT).addAdvice(
                createEventHandlerAdvice(eventType, minContextValues, methodDescription, handler));

    }

    private static ComponentMethodAdvice createEventHandlerAdvice(final String eventType, final int minContextValues,
            final String methodDescription, final ComponentEventHandler handler)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                // Invoke the super-class implementation first.

                invocation.proceed();

                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (!event.isAborted() && event.matches(eventType, "", minContextValues))
                {
                    event.setMethodDescription(methodDescription);

                    handler.handleEvent(invocation.getInstance(), event);

                    // Ensure that the caller knows that some event handler method
                    // was invoked.
                    invocation.overrideResult(true);
                }
            }
        };
    }

}
