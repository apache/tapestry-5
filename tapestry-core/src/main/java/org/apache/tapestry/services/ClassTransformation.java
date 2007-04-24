// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.services;

import java.lang.annotation.Annotation;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.tapestry.AnnotationProvider;

/**
 * Contains class-specific information used when transforming an raw class into an executable class.
 * Much of this information is somewhat like ordinary reflection, but applies to a class that has
 * not yet been loaded.
 * <p>
 * Transformation is primarily about identifying annotations on fields and on methods and changing
 * the class, adding new interfaces, fields and methods, and deleting some existing fields.
 * <p>
 * A ClassTransformation contains all the state data specific to a particular class being
 * transformed. A number of <em>workers</em> will operate upon the ClassTransformation to effect
 * the desired changes before the true class is loaded into memory.
 * <p>
 * Instances of this class are not designed to be thread safe, access to an instance should be
 * restricted to a single thread. In fact, the design of this type is to allow stateless singletons
 * in multiple threads to work on thread-specific data (within the ClassTransformation). *
 * <p>
 * The majority of methods concern the <em>declared</em> members (field and methods) of a specific
 * class, rather than any fields or methods inherited from a base class.
 */
public interface ClassTransformation extends AnnotationProvider
{
    /**
     * Returns the fully qualified class name of the class being transformed.
     */
    String getClassName();

    /**
     * Returns the name of a new member (field or method). Ensures that the resulting name does not
     * conflict with any existing member (declared by the underlying class, or inherited from a base
     * class).
     * 
     * @param suggested
     *            the suggested value for the member
     * @return a unique name for the member
     */
    String newMemberName(String suggested);

    /**
     * As with {@link #newMemberName(String)}, but the suggested name is constructed from the
     * prefix and base name. An underscore will seperate the prefix from the base name.
     * 
     * @param prefix
     *            for the generated name
     * @param baseName
     *            an name, often of an existing field or method
     * @return a unique name
     */
    String newMemberName(String prefix, String baseName);

    /**
     * Generates a list of the names of declared instance fields that have the indicated annotation.
     * Non-private and static fields are ignored. Only the names of private instance fields are
     * returned. Any {@link #claimField(String, Object) claimed} fields are excluded.
     */
    List<String> findFieldsWithAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Generates a list of the names of declared instance fields that exactly match the specified
     * type. Only the names of private instance fields are returned. Any
     * {@link #claimField(String, Object) claimed} fields are excluded.
     */
    List<String> findFieldsOfType(String type);

    /**
     * Finds all methods defined in the class that are marked with the provided annotation.
     * 
     * @param annotationClass
     * @return a list of method signature (which may be empty) in ascending order
     */
    List<MethodSignature> findMethodsWithAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * Finds all methods matched by the provided filter.
     * 
     * @param filter
     *            Passed each method signature, it may include or exclude each potential
     * @return a list of matching method signatures (which may be empty) in ascending order
     */
    List<MethodSignature> findMethods(MethodFilter filter);

    /**
     * Finds all unclaimed fields matched by the provided filter. Only considers unclaimed, private,
     * instance fields.
     * 
     * @param filter
     *            passed each field name and field type
     * @return the names of all matched fields, in ascending order
     */
    List<String> findFields(FieldFilter filter);

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
     */
    <T extends Annotation> T getFieldAnnotation(String fieldName, Class<T> annotationClass);

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
     */
    <T extends Annotation> T getMethodAnnotation(MethodSignature method, Class<T> annotationClass);

    /**
     * Claims a field so as to ensure that only a single annotation is applied to any single field.
     * When a transformation occurs (driven by a field annotation), the first thing that occurs is
     * to claim the field, on behalf of the annotation.
     * 
     * @param fieldName
     *            the name of the field that is being claimed
     * @param tag
     *            a non-null object that represents why the field is being tagged (this is typically
     *            a specific annotation on the field)
     * @throws IllegalArgumentException
     *             if the fieldName does not correspond to a declared instance field
     * @throws IllegalStateException
     *             if the field is already claimed for some other tag
     */
    void claimField(String fieldName, Object tag);

    /**
     * Changes the field to be read only. Any existing code that changes the field will cause a
     * runtime exception.
     * 
     * @param fieldName
     *            name of field to so change
     */
    void makeReadOnly(String fieldName);

    /**
     * Finds any declared <em>instance</em> fields that have not been claimed (via
     * {@link #claimField(String, Object)}) and returns the names of those fields. May return an
     * empty array.
     */
    List<String> findUnclaimedFields();

    /**
     * Obtains the type of a declared instance field.
     * 
     * @param fieldName
     * @return the type of the field, as a string
     * @throws IllegalArgumentException
     *             if the fieldName does not correspond to a declared instance field
     */
    String getFieldType(String fieldName);

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
     */
    String addField(int modifiers, String type, String suggestedName);

    /**
     * Defines a new <strong>protected</strong> instance variable whose initial value is provided
     * statically, via a constructor parameter. The transformation caches the result, so calling
     * this method repeatedly with the same type and value will return the same field name. Caching
     * extends to the parent transformation, so that a value injected into a parent class will be
     * available (via the protected instance variable) to subclasses.
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
     * Converts the field into a read only field whose value is the provided value. This is used
     * when converting an existing field into a read-only injected value.
     * 
     * @param fieldName
     *            name of field to convert
     * @param value
     *            the value provided by the field
     */
    void injectField(String fieldName, Object value);

    /**
     * Transforms the class to implement the indicated interface. If the class (or its super class)
     * does not already implement the interface, then the interface is added, and default
     * implementations of any methods of the interface are added.
     * <p>
     * TODO: Checking that the names of methods in the interface do not conflict with the names of
     * methods present in the (unmodified) class.
     * 
     * @param interfaceClass
     *            the interface to be implemented by the class
     * @throws IllegalArgumentException
     *             if the interfaceClass argument does not represent an interface
     */
    void addImplementedInterface(Class interfaceClass);

    /**
     * Extends an existing method. The provided method body is inserted at the end of the existing
     * method (i.e. {@link javassist.CtBehavior#insertAfter(java.lang.String)}). To access or
     * change the return value, use the <code>$_</code> pseudo variable.
     * <p>
     * The method may be declared in the class, or may be inherited from a super-class. For
     * inherited methods, a method is added that first invokes the super implementation. Use
     * {@link #addMethod(MethodSignature, String)} when it is necessary to control when the
     * super-class method is invoked.
     * 
     * @param signature
     *            the signature of the method to extend
     * @param methodBody
     *            the body of code
     * @throws IllegalArgumentException
     *             if the provided Javassist method body can not be compiled
     */
    void extendMethod(MethodSignature methodSignature, String methodBody);

    /**
     * Returns the name of a field that provides the {@link org.apache.tapestry.ComponentResources}
     * for the transformed component. This will be a protected field, accessible to the class and
     * subclasses.
     * 
     * @return name of field
     */
    String getResourcesFieldName();

    /**
     * Adds a new method to the transformed class. Replaces any existing method declared for the
     * class. When overriding a super-class method, you should use
     * {@link #extendMethod(MethodSignature, String)}, or you should remember to invoke the super
     * class implemetation explicitly. Use this method to control when the super-class
     * implementation is invoked.
     */
    void addMethod(MethodSignature signature, String methodBody);

    /**
     * Adds a statement to the constructor. The statement is added as is, though a newline is added.
     * 
     * @param statement
     *            the statement to add, which should end with a semicolon
     */
    void extendConstructor(String statement);

    /**
     * Replaces all read-references to the specified field with invocations of the specified method
     * name. Replacements do not occur in methods added via
     * {@link #addMethod(MethodSignature, String)} or {@link #extendMethod(MethodSignature, String)}.
     */
    void replaceReadAccess(String fieldName, String methodName);

    /**
     * Replaces all write accesses to the specified field with invocations of the specified method
     * name. The method should take a single parameter of the same type as the field. Replacements
     * do not occur in methods added via {@link #addMethod(MethodSignature, String)} or
     * {@link #extendMethod(MethodSignature, String)}.
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
     */
    void removeField(String fieldName);

    /**
     * Converts a type name into a corresponding class (possibly, a transformed class). Primitive
     * type names are returned as wrapper types.
     */

    Class toClass(String type);

    /**
     * Returns a log, based on the class name being transformed, to which warnings or errors
     * concerning the class being transformed may be logged.
     */
    Log getLog();

    /** Returns the modifiers for the named field. */
    int getFieldModifiers(String fieldName);

    /**
     * Converts a signature to a string used to identify the method; this consists of the
     * {@link MethodSignature#getMediumDescription()} appended with source file information and line
     * number information (when available).
     * 
     * @param signature
     * @return a string that identifies the class, method name, types of parameters, source file and
     *         source line number
     */
    String getMethodIdentifier(MethodSignature signature);
}
