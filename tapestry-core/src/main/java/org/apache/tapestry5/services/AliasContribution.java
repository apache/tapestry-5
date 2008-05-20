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

package org.apache.tapestry5.services;

import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.Formatter;

/**
 * A contribution into the {@link Alias} or AliasOverride service configuration.
 */
public final class AliasContribution<T>
{
    private final Class<T> contributionType;

    private final String mode;

    private final T object;

    /**
     * Simplifies the creation of an AliasContribution around a known type and instance of that type.
     */
    public static <X> AliasContribution<X> create(Class<X> contributionType, X object)
    {
        return new AliasContribution<X>(contributionType, object);
    }

    /**
     * Simplifies the creation of an AliasContribution around a known type, mode, and an instance of that type.
     */
    public static <X> AliasContribution<X> create(Class<X> contributionType, String mode, X object)
    {
        return new AliasContribution<X>(contributionType, mode, object);
    }

    /**
     * Conntributes the object with a blank mode.
     */
    public AliasContribution(Class<T> contributionType, T object)
    {
        this(contributionType, "", object);
    }

    public AliasContribution(Class<T> contributionType, String mode, T object)
    {
        this.contributionType = notNull(contributionType, "contributionClass");
        this.mode = notNull(mode, "mode");
        this.object = notNull(object, "object");
    }

    /**
     * Returns the mode of operation for this instance of Tapestry. Most of the time, this will be the empty string,
     * meaning that the contribution applies to Tapestry is any mode. In other cases, the mode will be "servlet" but may
     * be other modes via add on modules, such as "portlet" or "offline".
     */
    public String getMode()
    {
        return mode;
    }

    public Class<T> getContributionType()
    {
        return contributionType;
    }

    /**
     * The contributed object, which will be made available.
     */
    public T getObject()
    {
        return object;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);

        formatter.format("<AliasContribution: %s", contributionType.getName());

        if (InternalUtils.isNonBlank(mode)) formatter.format(" mode:%s", mode);

        formatter.format(" %s>", object);

        return builder.toString();
    }

}
