// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import java.lang.annotation.Annotation;
import java.util.List;

import javassist.CtBehavior;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.transform.ReadOnlyFieldValueConduit;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.util.func.Predicate;
import org.slf4j.Logger;

/**
 * Contains class-specific information used when transforming a raw component class into an
 * executable component class.
 * An executable class is one that has been transformed to work within Tapestry. This includes
 * adding interfaces
 * ({@link org.apache.tapestry5.runtime.Component}) but also transforming access to fields, based on
 * annotations and
 * naming conventions. Most of the changes are provided by different implementations of
 * {@link ComponentClassTransformWorker}.
 * <p/>
 * Much of this information is somewhat like ordinary reflection, but applies to a class that has not yet been loaded.
 * Field types, return types, parameter types and exception types are represented as string names, since any of them may
 * be a class that has not yet been loaded and transformed as well.
 * <p/>
 * Transformation is primarily about identifying annotations on fields and on methods and changing the class, adding new
 * interfaces, fields and methods, and deleting some existing fields.
 * <p/>
 * A ClassTransformation contains all the state data specific to a particular class being transformed. A number of
 * <em>workers</em> will operate upon the ClassTransformation to effect the desired changes before the true class is
 * loaded into memory.
 * <p/>
 * Instances of this class are not designed to be thread safe, access to an instance should be restricted to a single
 * thread. In fact, the design of this type is to allow stateless singletons in multiple threads to work on
 * thread-specific data (within the ClassTransformation).
 * <p/>
 * The majority of methods concern the <em>declared</em> members (field and methods) of a specific class, rather than
 * any fields or methods inherited from a base class.
 * 
 * @see org.apache.tapestry5.services.TapestryModule#contributeComponentClassTransformWorker(org.apache.tapestry5.ioc.OrderedConfiguration,
 *      org.apache.tapestry5.ioc.ObjectLocator, InjectionProvider, ComponentClassResolver)
 */
public interface ClassTransformation extends AnnotationProvider
{
    /**
     * Returns the fully qualified class name of the class being transformed.
     */
    String getClassName();

    /**
     * Returns the name of a new member (field or method). Ensures that the resulting name does not
     * conflict with any
     * existing member (declared by the underlying class, or inherited from a base class).
     * 
     * @param suggested
     *            the suggested value for the member
     * @return a unique name for the member
     */
    String newMemberName(String suggested);

    /**
     * As with {@link #newMemberName(String)}, but the suggested name is constructed from the prefix
     * and base name. An
     * underscore will separate the prefix from the base name.
     * 
     * @param prefix
     *            for the generated name
     * @param baseName
     *            a name, often of an existing field or method
     * @return a unique name
     */
    String newMemberName(String prefix, String baseName);

    /**
     * Generates a list of the names of declared instance fields that have the indicated annotation.
     * Non-private and
     * static fields are ignored. Only the names of private instance fields are returned.
     * 
     * @deprecated Use {@link #matchFieldsWithAnnotation(Class)} instead
     */
    List<String> findFieldsWithAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Returns a sorted list of declared instance fields with the indicated annotation. Non-private
     * and static fields are ignored.
     * 
     * @since 5.2.0
     */
    List<TransformField> matchFieldsWithAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Finds all methods defined in the class that are marked with the provided annotation.
     * 
     * @param annotationClass
     * @return a list of method signature (which may be empty) in ascending order
     * @see #findMethods(MethodFilter)
     * @deprecated Use {@link #matchMethodsWithAnnotation(Class)} instead
     */
    List<TransformMethodSignature> findMethodsWithAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Finds all methods matched by the provided filter.
     * 
     * @param filter
     *            Passed each method signature, it may include or exclude each potential
     * @return a list of matching method signatures (which may be empty) in ascending order (by
     *         method name), but
     *         descending order (by parameter count) within overrides of a single method name.
     * @deprecated Use {@link #matchMethods(Predicate)} instead
     */
    List<TransformMethodSignature> findMethods(MethodFilter filter);

    /**
     * Finds all methods matched by the provided predicate.
     * 
     * @param predicate
     *            Used to filter the list
     * @return a list of matching methods (which may be empty) in ascending order (by
     *         method name), but descending order (by parameter count) within overrides of a single method name.
     */
    List<TransformMethod> matchMethods(Predicate<TransformMethod> predicate);

    /**
     * Finds all methods matched by the provided predicate.
     * 
     * @param annotationType
     *            Used to filter the list
     * @return a list of matching methods (which may be empty) in ascending order (by
     *         method name), but descending order (by parameter count) within overrides of a single method name.
     */
    List<TransformMethod> matchMethodsWithAnnotation(Class<? extends Annotation> annotationType);

    /**
     * Finds all unclaimed fields matched by the provided filter. Only considers private instance
     * fields.
     * 
     * @param filter
     *            passed each field name and field type
     * @return the names of all matched fields, in ascending order
     * @deprecated Use {@link #matchFields(Predicate)} instead
     */
    List<String> findFields(FieldFilter filter);

    /**
     * Finds all unclaimed fields matched by the provided predicate. Only considers instance fields.
     * Added, removed and claimed fields are excluded.
     * 
     * @param predicate
     *            used for matching
     * @return sorted list of matching fields
     * @since 5.2.0
     */
    List<TransformField> matchFields(Predicate<TransformField> predicate);

    /**
     * Finds an annotation on a declared instance field.
     * 
     * @param <T>
     *            constrains parameter and return value to Annotation types
     * @param fieldName
     *            the name of the field, which must exist
     * @param annotationClass
     *            the type of annotation to access
     * @return the annotation if present, or null otherwise
     * @throws IllegalArgumentException
     *             if the fieldName does not correspond to a declared field
     * @deprecated Use {@link TransformField#getAnnotation(Class)} instead
     */
    <T extends Annotation> T getFieldAnnotation(String fieldName, Class<T> annotationClass);

    /**
     * Locates a declared field by its field name. The field must exist.
     * 
     * @param name
     *            of declared field
     * @return field information
     * @throws RuntimeException
     *             if no such field
     * @since 5.2.0
     */
    TransformField getField(String fieldName);

    /**
     * Finds an annotation on a declared method.
     * 
     * @param <T>
     *            constrains parameter and return value to Annotation types
     * @param method
     *            the method signature to search
     * @param annotationClass
     *            the type of annotation to access
     * @return the annotation if present, or null otherwise
     * @throws IllegalArgumentException
     *             if the method signature does not correspond to a declared method
     * @deprecated Use {@link TransformMethod#getAnnotation(Class)} instead
     */
    <T extends Annotation> T getMethodAnnotation(TransformMethodSignature method, Class<T> annotationClass);

    /**
     * Claims a field so as to ensure that only a single annotation is applied to any single field.
     * When a
     * transformation occurs (driven by a field annotation), the field is claimed (using the
     * annotation object as the
     * tag). If a field has multiple conflicting annotations, this will be discovered when the code
     * attempts to claim
     * the field a second time.
     * 
     * @param fieldName
     *            the name of the field that is being claimed
     * @param tag
     *            a non-null object that represents why the field is being tagged (this is typically
     *            a specific
     *            annotation on the field)
     * @throws IllegalArgumentException
     *             if the fieldName does not correspond to a declared instance field
     * @throws IllegalStateException
     *             if the field is already claimed for some other tag
     * @deprecated Use {@link TransformField#claim(Object)} instead
     */
    void claimField(String fieldName, Object tag);

    /**
     * Changes the field to be read only. Any existing code that changes the field will cause a
     * runtime exception.
     * 
     * @param fieldName
     *            name of field to so change
     * @deprecated Use {@link TransformField#replaceAccess(TransformField)} instead
     * @see ReadOnlyFieldValueConduit
     */
    void makeReadOnly(String fieldName);

    /**
     * Finds any declared <em>instance</em> fields that have not been claimed (via {@link #claimField(String, Object)})
     * and have not been added , and returns the names of those fields. May return an empty array.
     * 
     * @deprecated Use {@link #matchUnclaimedFields()} instead
     */
    List<String> findUnclaimedFields();

    /**
     * Matches all fields that are not claimed. This may include static fields and final fields, but will not
     * include fields that have been added as part of the transformation.
     * 
     * @since 5.2.0
     * @return sorted list of unclaimed fields
     */
    List<TransformField> matchUnclaimedFields();

    /**
     * Obtains the type of a declared instance field.
     * 
     * @param fieldName
     * @return the type of the field, as a string
     * @throws RuntimeException
     *             if the fieldName does not correspond to a declared instance field
     * @deprecated Use {@link TransformField#getType()} instead
     */
    String getFieldType(String fieldName);

    /**
     * Returns true if the indicated name is a private instance field.
     * 
     * @param fieldName
     * @return true if field exists
     */
    boolean isField(String fieldName);

    /**
     * Defines a new declared field for the class. The suggestedName may be modified to ensure
     * uniqueness.
     * 
     * @param modifiers
     *            modifiers for the field (typically, {@link java.lang.reflect.Modifier#PRIVATE})
     * @param type
     *            the type for the field, as a string
     * @param suggestedName
     *            the desired name for the field, which may be modified (for uniqueness) when
     *            returned
     * @return the (uniqued) name for the field
     * @deprecated Use {@link #createField(int, String, String)} instead
     */
    String addField(int modifiers, String type, String suggestedName);

    /**
     * Defines a new declared field for the class. Suggested name may be modified to ensure uniqueness.
     * 
     * @param modifiers
     *            modifiers for the field (typically, {@link java.lang.reflect.Modifier#PRIVATE})
     * @param type
     *            the type for the field, as a string
     * @param suggestedName
     *            the desired name for the field, which may be modified (for uniqueness) when
     *            returned
     * @return new field instance
     */
    TransformField createField(int modifiers, String type, String suggestedName);

    /**
     * Defines a new <strong>protected</strong> instance variable whose initial value is provided
     * statically, via a
     * constructor parameter. The transformation caches the result, so calling this method
     * repeatedly with the same type
     * and value will return the same field name. Caching extends to the parent transformation, so
     * that a value injected
     * into a parent class will be available (via the protected instance variable) to subclasses.
     * This is primarily used to inject service dependencies into components, though it has a number
     * of other uses as well.
     * 
     * @param type
     *            the type of object to inject
     * @param suggestedName
     *            the suggested name for the new field
     * @param value
     *            to be injected. This value is retained.
     * @return the actual name of the injected field
     */
    String addInjectedField(Class type, String suggestedName, Object value);

    /**
     * Like {@link #addInjectedField(Class, String, Object)}, but instead of specifying the value,
     * a provider for the value is specified. In the generated class' constructor, the provider
     * will be passed the {@link ComponentResources} and will return the final value; thus
     * each component <em>instance</em> will receive a matching unique instance via the provider.
     * 
     * @param <T>
     * @param type
     *            type of value to inject
     * @param suggestedName
     *            suggested name for the new field
     * @param provider
     *            injected into the component to provide the value
     * @return the actual name of the injected field
     * @since 5.2.0
     */
    <T> TransformField addIndirectInjectedField(Class<T> type, String suggestedName, ComponentValueProvider<T> provider);

    /**
     * Converts an <em>existing</em> field into a read only field whose value is the provided
     * value. This is used
     * when converting an
     * existing field into a read-only injected value.
     * 
     * @param fieldName
     *            name of field to convert
     * @param value
     *            the value provided by the field
     * @deprecated Use {@link TransformField#inject(Object)} instead
     */
    void injectField(String fieldName, Object value);

    /**
     * Transforms the class to implement the indicated interface. If the class (or its super class)
     * does not already
     * implement the interface, then the interface is added, and default implementations of any
     * methods of the interface
     * are added.
     * <p/>
     * TODO: Checking that the names of methods in the interface do not conflict with the names of methods present in
     * the (unmodified) class.
     * 
     * @param interfaceClass
     *            the interface to be implemented by the class
     * @throws IllegalArgumentException
     *             if the interfaceClass argument does not represent an interface
     */
    void addImplementedInterface(Class interfaceClass);

    /**
     * Extends an existing method. The provided method body is inserted at the end of the existing
     * method (i.e. {@link javassist.CtBehavior#insertAfter(java.lang.String)}). To access or change
     * the return value, use the <code>$_</code> pseudo variable.
     * <p/>
     * The method may be declared in the class, or may be inherited from a super-class. For inherited methods, a method
     * body is added that first invokes the super implementation. Use
     * {@link #addMethod(TransformMethodSignature, String)} when it is necessary to control when the super-class method
     * is invoked.
     * <p/>
     * The extended method is considered <em>new</em>. New methods <em>are not</em> scanned for
     * {@linkplain #removeField(String)} removed}, {@linkplain #replaceReadAccess(String, String)} read replaced}, or
     * {@linkplain #replaceWriteAccess(String, String) write replaced} fields. Generally that's what you want!
     * 
     * @param methodSignature
     *            the signature of the method to extend
     * @param methodBody
     *            the body of code
     * @throws org.apache.tapestry5.internal.services.MethodCompileException
     *             if the provided Javassist method body can not be compiled
     * @deprecated Use {@link TransformMethod#addAdvice(ComponentMethodAdvice)} instead. This method is non-functional
     *             as of Tapestry 5.2.
     */
    void extendMethod(TransformMethodSignature methodSignature, String methodBody);

    /**
     * Like {@link #extendMethod(TransformMethodSignature, String)}, but the extension does not mark
     * the method as new,
     * and field changes <em>will</em> be processed. Note: at some point, this is not longer true; extend and
     * extendMethod work identically.
     * 
     * @param methodSignature
     *            signature of the method to extend
     * @param methodBody
     *            the body of code
     * @throws org.apache.tapestry5.internal.services.MethodCompileException
     *             if the provided method body can not be compiled
     * @see #prefixMethod(TransformMethodSignature, String)
     * @deprecated Use {@link TransformMethod#addAdvice(ComponentMethodAdvice) instead}. This method is non-functional
     *             as of Tapestry 5.2.
     */
    void extendExistingMethod(TransformMethodSignature methodSignature, String methodBody);

    /**
     * Inserts code at the beginning of a method body (i.e. {@link CtBehavior#insertBefore(String)}.
     * <p/>
     * The method may be declared in the class, or may be inherited from a super-class. For inherited methods, a method
     * is added that first invokes the super implementation. Use {@link #addMethod(TransformMethodSignature, String)}
     * when it is necessary to control when the super-class method is invoked.
     * <p/>
     * Like {@link #extendExistingMethod(TransformMethodSignature, String)}, this method is generally used to "wrap" an
     * existing method adding additional functionality such as caching or transaction support.
     * 
     * @param methodSignature
     * @param methodBody
     * @throws org.apache.tapestry5.internal.services.MethodCompileException
     *             if the provided method body can not be compiled
     * @deprecated Use {@link TransformMethod#addAdvice(ComponentMethodAdvice)} instead. This method is non-functional
     *             as of Tapestry 5.2.
     */
    void prefixMethod(TransformMethodSignature methodSignature, String methodBody);

    /**
     * Returns the name of a field that provides the {@link org.apache.tapestry5.ComponentResources} for the transformed
     * component. This will be a protected field, accessible to the class and subclasses.
     * 
     * @return name of field
     * @deprecated Obtain the resources from {@link ComponentMethodInvocation#getComponentResources()} or
     *             as passed to {@link ComponentValueProvider#get(ComponentResources)} instead
     */
    String getResourcesFieldName();

    /**
     * Adds a new method to the transformed class. Replaces any existing method declared for the
     * class. When overriding
     * a super-class method, you should use {@link #extendMethod(TransformMethodSignature, String)},
     * or you should
     * remember to invoke the super class implemetation explicitly. Use this method to control when
     * the super-class
     * implementation is invoked.
     * 
     * @deprecated Use {@link #getOrCreateMethod(TransformMethodSignature)} instead. This method is non-functional as of
     *             Tapestry 5.2.
     */
    void addMethod(TransformMethodSignature signature, String methodBody);

    /**
     * As with {@link #addMethod(TransformMethodSignature, String)}, but field references inside the
     * method <em>will</em> be transformed, and the method <em>must not already exist</em>.
     * 
     * @deprecated Use {@link #getOrCreateMethod(TransformMethodSignature)} instead. This method is non-functional as of
     *             Tapestry 5.2.
     */
    void addTransformedMethod(TransformMethodSignature methodSignature, String methodBody);

    /**
     * Adds a statement to the constructor. The statement is added as is, though a newline is added.
     * 
     * @param statement
     *            the statement to add, which should end with a semicolon
     * @deprecated Use methods that create or inject fields (directly or indirectly)
     * @see ComponentValueProvider
     */
    void extendConstructor(String statement);

    /**
     * Replaces all read-references to the specified field with invocations of the specified method
     * name. Replacements
     * do not occur in methods added via {@link #addMethod(TransformMethodSignature, String)} or
     * {@link #extendMethod(TransformMethodSignature, String)}.
     * 
     * @deprecated Use {@link TransformField#replaceAccess(ComponentValueProvider) instead
     */
    void replaceReadAccess(String fieldName, String methodName);

    /**
     * Replaces all write accesses to the specified field with invocations of the specified method
     * name. The method
     * should take a single parameter of the same type as the field. Replacements do not occur in
     * methods added via {@link #addMethod(TransformMethodSignature, String)} or
     * {@link #extendMethod(TransformMethodSignature, String)}.
     * 
     * @deprecated Use {@link TransformField#replaceAccess(ComponentValueProvider) instead
     */
    void replaceWriteAccess(String fieldName, String methodName);

    /**
     * Removes a field entirely; this is useful for fields that are replaced entirely by computed
     * values.
     * 
     * @param fieldName
     *            the name of the field to remove
     * @see #replaceReadAccess(String, String)
     * @see #replaceWriteAccess(String, String)
     * @deprecated This method is non-functional as of Tapestry 5.2
     */
    void removeField(String fieldName);

    /**
     * Converts a type name into a corresponding class (possibly, a transformed class). Primitive
     * type names are returned as wrapper types.
     */
    Class toClass(String type);

    /**
     * Returns a logger, based on the class name being transformed, to which warnings or errors
     * concerning the class being transformed may be logged.
     */
    Logger getLogger();

    /**
     * Returns the modifiers for the named field.
     * 
     * @deprecated Use {@link TransformField#getModifiers()} instead
     */
    int getFieldModifiers(String fieldName);

    /**
     * Converts a signature to a string used to identify the method; this consists of the
     * {@link TransformMethodSignature#getMediumDescription()} appended with source file information
     * and line number
     * information (when available).
     * 
     * @param signature
     * @return a string that identifies the class, method name, types of parameters, source file and
     *         source line number
     * @deprecated Use {@link TransformMethod#getMethodIdentifier()} instead
     */
    String getMethodIdentifier(TransformMethodSignature signature);

    /**
     * Returns true if this transformation represents a root class (one that extends directly from
     * Object), or false if this transformation is an sub-class of another transformed class.
     * 
     * @return true if root class, false if sub-class
     */
    boolean isRootTransformation();

    /**
     * Adds a catch block to the method. The body should end with a return or a throw. The special
     * Javassist variable
     * $e is the exception instance.
     * 
     * @param methodSignature
     *            method to be extended.
     * @param exceptionType
     *            fully qualified class name of exception
     * @param body
     *            code to execute
     * @deprecated Use {@link TransformMethod#addAdvice(ComponentMethodAdvice)} instead. This method is non-functional
     *             as of Tapestry 5.2.
     */
    void addCatch(TransformMethodSignature methodSignature, String exceptionType, String body);

    /**
     * Adds method advice for the indicated method.
     * 
     * @deprecated Use {@link TransformMethod#addAdvice(ComponentMethodAdvice)} instead
     */
    void advise(TransformMethodSignature methodSignature, ComponentMethodAdvice advice);

    /**
     * Returns true if the method is an override of a method from the parent class.
     * 
     * @param methodSignature
     *            signature of method to check
     * @return true if the parent class contains a method with the name signature
     * @deprecated Use {@link TransformMethod#isOverride()} instead
     */
    boolean isMethodOverride(TransformMethodSignature methodSignature);

    /**
     * Locates and returns the method if declared in this class; If not,
     * the method is added to the class. If the method is an override
     * of a base class method, then the method will delegate to the base
     * class method (invoke it, return its value). If the method is entirely
     * new, it will ignore its parameters and return a default value (null, 0 or false).
     * 
     * @param signature
     *            identifies the method to locate, override or create
     * @since 5.2.0
     */
    TransformMethod getOrCreateMethod(TransformMethodSignature signature);

    /**
     * Determines if the class being transformed includes a declared (not inherited) method
     * with the provided signature.
     * 
     * @since 5.2.0
     * @param signature
     *            identifies method to search for
     * @return true if a such a method exists
     */
    boolean isDeclaredMethod(TransformMethodSignature signature);
}
