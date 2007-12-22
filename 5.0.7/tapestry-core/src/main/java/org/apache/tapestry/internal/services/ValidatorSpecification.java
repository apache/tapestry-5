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

package org.apache.tapestry.internal.services;

/**
 * Validator type and constraint values parsed from a validator specification.
 */
class ValidatorSpecification
{
    private final String _validatorType;

    private final String _constraintValue;

    public ValidatorSpecification(String validatorType)
    {
        this(validatorType, null);
    }

    public ValidatorSpecification(String validatorType, String constraintValue)
    {
        _validatorType = validatorType;
        _constraintValue = constraintValue;
    }

    public String getConstraintValue()
    {
        return _constraintValue;
    }

    public String getValidatorType()
    {
        return _validatorType;
    }

    @Override
    public String toString()
    {
        return String.format("ValidatorSpecification[%s %s]", _validatorType, _constraintValue);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null || other.getClass() != getClass())
            return false;

        ValidatorSpecification ov = (ValidatorSpecification) other;

        if (!_validatorType.equals(ov._validatorType))
            return false;

        if (_constraintValue == ov._constraintValue)
            return true;

        if (_constraintValue == null)
            return false;

        return _constraintValue.equals(ov._constraintValue);
    }
}