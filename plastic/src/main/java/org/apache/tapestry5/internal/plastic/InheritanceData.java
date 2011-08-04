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

package org.apache.tapestry5.internal.plastic;

import java.util.Set;

/**
 * Used to track which methods are implemented by a base class, which is often needed when transforming
 * a subclass.
 */
public class InheritanceData
{
    private final InheritanceData parent;

    private final Set<String> methodNames = PlasticInternalUtils.newSet();
    private final Set<String> methods = PlasticInternalUtils.newSet();
    private final Set<String> interfaceNames = PlasticInternalUtils.newSet();

    public InheritanceData()
    {
        this(null);
    }

    private InheritanceData(InheritanceData parent)
    {
        this.parent = parent;
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
     * @param childClassName name of subclass
     * @return new method bundle
     */
    public InheritanceData createChild(String childClassName)
    {
        return new InheritanceData(this);
    }

    /**
     * Adds a new instance method. Only non-private, non-abstract methods should be added (that is, methods which might
     * be overridden in subclasses). This can later be queried to see if any base class implements the method.
     *
     * @param name name of method
     * @param desc method descriptor
     */
    public void addMethod(String name, String desc)
    {
        String value = toValue(name, desc);

        methods.add(value);
        methodNames.add(name);
    }

    /**
     * Returns true if a transformed parent class contains the indicated method.
     *
     * @param name method name
     * @param desc method descriptor
     * @return the <em>internal name</em> of the implementing base class for this method,
     *         or null if no base class implements the method
     */
    public boolean isImplemented(String name, String desc)
    {
        return checkForMethod(toValue(name, desc));
    }


    private boolean checkForMethod(String value)
    {
        if (methods.contains(value))
            return true;

        return parent == null ? false : parent.checkForMethod(value);
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

    public void addInterface(String name) {
        interfaceNames.add(name);
    }

    /**
     * Combines a method name and its desc (which describes parameter types and return value) to form
     * a value, which is how methods are tracked.
     */
    private String toValue(String name, String desc)
    {
        return name + ":" + desc;
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
