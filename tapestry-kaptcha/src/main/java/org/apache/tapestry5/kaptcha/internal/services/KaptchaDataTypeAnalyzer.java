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

package org.apache.tapestry5.kaptcha.internal.services;

import org.apache.tapestry5.commons.services.DataTypeAnalyzer;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.kaptcha.annotations.Kaptcha;

public class KaptchaDataTypeAnalyzer implements DataTypeAnalyzer
{
    @Override
    public String identifyDataType(PropertyAdapter adapter)
    {
        final Kaptcha annotation = adapter.getAnnotation(Kaptcha.class);

        return annotation == null ? null : "kaptcha";
    }
}
