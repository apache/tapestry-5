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

package org.apache.tapestry5.internal.plastic;

import java.util.Map;
import java.util.Set;

/**
 * Used to track which methods are implemented by a base class, which is often needed when transforming
 * a subclass.
 */
public class InheritanceData
{
    private final InheritanceData parent;

    private final String packageName;

    private final Set<String> methodNames = PlasticInternalUtils.newSet();
    private final Map<String, Boolean> methods = PlasticInternalUtils.newMap();
    private final Set<String> interfaceNames = PlasticInternalUtils.newSet();

    public InheritanceData(String packageName)
    {
        this(null, packageName);
    }

    private InheritanceData(InheritanceData parent, String packageName)
    {
        this.parent = parent;
        this.packageName = packageName;
    }

    /**
     * Is this bundle for a transformed class, or for a base class (typically Object)?
     *
     * @return true if this bundle is for transformed class, false otherwise
     */
    public boolean isTransformed()
    {
        return parent != null;
    }

    /**
     * Returns a new MethodBundle that represents the methods of a child class
     * of this bundle. The returned bundle will always be {@linkplain #isTransformed() transformed}.
     *
     * @param packageName
     *         the package that the child class will be created in
     * @return new method bundle
     */
    public InheritanceData createChild(String packageName)
    {
        return new InheritanceData(this, packageName);
    }

    /**
     * Adds a new instance method. Only non-private methods should be added (that is, methods which might
     * be overridden in subclasses). This can later be queried to see if any base class implements the method.
     *
     * @param name
     *         name of method
     * @param desc
     *         describes the parameters and return value of the method
     * @param samePackageOnly
     *         whether the method can only be overridden in classes that are in the same package
     */
    public void addMethod(String name, String desc, boolean samePackageOnly)
    {
        methods.put(toValue(name, desc), samePackageOnly);
        methodNames.add(name);
    }


    /**
     * Returns true if this class or a transformed parent class contains an implementation of,
     * or abstract placeholder for, the method.
     *
     * @param name
     *         method name
     * @param desc
     *         method descriptor
     * @return true if this class or a base class implements the method (including abstract methods)
     */
    public boolean isImplemented(String name, String desc)
    {
        return checkForMethod(toValue(name, desc), this);
    }

    /**
     * Returns true if the method is an override of a base class method
     *
     * @param name
     *         method name
     * @param desc
     *         method descriptor
     * @return true if a base class implements the method (including abstract methods)
     */
    public boolean isOverride(String name, String desc)
    {
        return checkForMethod(toValue(name, desc), parent);
    }

    private boolean checkForMethod(String value, InheritanceData cursor)
    {

        String thisPackageName = packageName;

        while (cursor != null)
        {
            if (cursor.methods.containsKey(value))
            {
                boolean mustBeInSamePackage = cursor.methods.get(value);

                if (!mustBeInSamePackage)
                {
                    return true;
                }
                boolean isInSamePackage = thisPackageName.equals(cursor.packageName);

                if (isInSamePackage)
                {
                    return true;
                }
            }

            cursor = cursor.parent;
        }

        return false;
    }

    /**
     * Returns true if the class represented by this data, or any parent data, implements
     * the named interface.
     */
    public boolean isInterfaceImplemented(String name)
    {
        InheritanceData cursor = this;

        while (cursor != null)
        {
            if (cursor.interfaceNames.contains(name))
            {
                return true;
            }

            cursor = cursor.parent;
        }

        return false;
    }

    public void addInterface(String name)
    {
        if (!interfaceNames.contains(name))
        {
            interfaceNames.add(name);
        }
    }

    /**
     * Combines a method name and its desc (which describes parameter types and return value) to form
     * a value, which is how methods are tracked.
     */
    private static String toValue(String name, String desc)
    {
        // TAP5-2268: ignore return-type to avoid methods with the same number (and type) of parameters but different
        //            return-types which is illegal in Java.
        // desc is something like "(I)Ljava/lang/String;", which means: takes an int, returns a String. We strip
        // everything after the parameter list.
        int endOfParameterSpecIdx = desc.indexOf(')');

        return (name + ":" + desc.substring(0, endOfParameterSpecIdx+1)).intern();
    }

    /**
     * Returns the names of any methods in this bundle, or from any parent bundles.
     */
    public Set<String> methodNames()
    {
        Set<String> result = PlasticInternalUtils.newSet();

        InheritanceData cursor = this;

        while (cursor != null)
        {
            result.addAll(cursor.methodNames);
            cursor = cursor.parent;
        }

        return result;
    }
}
