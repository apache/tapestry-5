// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.util.Formatter;

import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * A contribution into the infrastructure configuration.
 * <p>
 * TODO: There might be an order-of-operations issue here. We may need a mechanism like Tapestry 4's
 * infrastructure that defers instantiation of contributed objects until after the Infrastructure
 * service is constructed.
 * 
 * 
 */
public final class InfrastructureContribution
{
    private final String _name;

    private final String _mode;

    private final Object _object;

    /**
     * Conntributes the object with a blank mode.
     */
    public InfrastructureContribution(String name, Object object)
    {
        this(name, "", object);
    }

    public InfrastructureContribution(String name, String mode, Object object)
    {
        _name = notBlank(name, "name");
        _mode = mode;
        _object = object;
    }

    /**
     * Returns the mode of operation for this instance of Tapestry. Most of the time, this will be
     * the empty string, meaning that the contribution applies to Tapestry is any mode. In other
     * cases, the mode will be "servlet" but may be other modes via add on modules, such as
     * "portlet" or "offline".
     * 
     * @return
     */
    public String getMode()
    {
        return _mode;
    }

    /**
     * The property name that may be accessed via the Infrastructure service. The combination of
     * name and mode must be unique.
     */
    public String getName()
    {
        return _name;
    }

    /** The contributed object, which will be made available. */
    public Object getObject()
    {
        return _object;
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        Formatter formatter = new Formatter(buffer);

        formatter.format("<InfrastructureContribution: %s", _name);

        if (InternalUtils.isNonBlank(_mode))
            formatter.format(" mode:%s", _mode);

        formatter.format(" %s>", _object);

        return buffer.toString();
    }

}
