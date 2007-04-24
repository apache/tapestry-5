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

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.util.Formatter;

import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * A contribution into the {@link Alias} or AliasOverride service configuration.
 */
public final class AliasContribution
{
    private final String _name;

    private final String _mode;

    private final Object _object;

    /**
     * Conntributes the object with a blank mode.
     */
    public AliasContribution(String name, Object object)
    {
        this(name, "", object);
    }

    public AliasContribution(String name, String mode, Object object)
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
     * The property name that may be accessed via the Alias service. The combination of name and
     * mode must be unique.
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
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);

        formatter.format("<AliasContribution: %s", _name);

        if (InternalUtils.isNonBlank(_mode)) formatter.format(" mode:%s", _mode);

        formatter.format(" %s>", _object);

        return builder.toString();
    }

}
