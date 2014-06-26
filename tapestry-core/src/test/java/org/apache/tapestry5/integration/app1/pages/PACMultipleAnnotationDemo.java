// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.PageActivationContext;

public class PACMultipleAnnotationDemo {
    @PageActivationContext(index=2)
    private Double two;

    @PageActivationContext(index=0)
    private String zero;

    @PageActivationContext(index=1)
    private Integer one;

    @InjectPage
    private PACMultipleAnnotationDemo otherPage;

    public void init(String zero, Integer one, Double two) {
        this.zero = zero;
        this.one = one;
        this.two = two;
    }

    public String getPACValues() {
        return String.format("zero=%s, one=%s, two=%s", toDisplayString(zero), toDisplayString(one), toDisplayString(two));
    }

    private String toDisplayString(Object o) {
        return o == null ? "NULL" : o.toString();
    }

    public Object onChangePAC(String zero, Integer one, Double two) {
        otherPage.init(zero, one, two);
        return otherPage;
    }
}