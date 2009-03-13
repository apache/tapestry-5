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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Locatable;
import org.apache.tapestry5.ioc.Location;

/**
 * Exception class used as a replacement for {@link java.lang.RuntimeException} when the exception is related to a
 * particular location.
 */
public class TapestryException extends RuntimeException implements Locatable
{
    private static final long serialVersionUID = 6396903640977182682L;

    private transient final Location location;

    /**
     * @param message  a message (may be null)
     * @param location implements {@link Location} or {@link Locatable}
     * @param cause    if not null, the root cause of the exception
     */
    public TapestryException(String message, Object location, Throwable cause)
    {
        this(message, InternalUtils.locationOf(location), cause);
    }

    /**
     * @param message a message (may be null)
     * @param cause   if not null, the root cause of the exception, also used to set the location
     */
    public TapestryException(String message, Throwable cause)
    {
        this(message, cause, cause);
    }

    /**
     * @param message  a message (may be null)
     * @param location location to associated with the exception, or null if not known
     * @param cause    if not null, the root cause of the exception
     */
    public TapestryException(String message, Location location, Throwable cause)
    {
        super(message, cause);

        this.location = location;
    }

    public Location getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        if (location == null) return super.toString();

        return String.format("%s [at %s]", super.toString(), location);
    }

}
