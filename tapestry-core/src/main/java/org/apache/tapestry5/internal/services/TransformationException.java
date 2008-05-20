// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.ClassTransformation;

/**
 * Exception thrown when there is a failure transforming a class, or instantiating a transformed class. The cause may be
 * an Error. The goal is to get the {@link ClassTransformation} into the exception report page, properly formatted.
 */
public class TransformationException extends TapestryException
{
    private static final long serialVersionUID = -7312854113157232961L;

    private final ClassTransformation transformation;

    public TransformationException(ClassTransformation transformation, Throwable cause)
    {
        super(cause.getMessage(), cause);

        this.transformation = transformation;
    }

    public ClassTransformation getTransformation()
    {
        return transformation;
    }
}
