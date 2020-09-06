// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test.internal;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.DefaultModuleDefImplTest}.
 */
public class ModuleWithOverriddenObjectMethods
{
  public static Object build()
  {
      return new Object();
  }

  @Override
  public int hashCode() {
    return 23;
  }
  
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
  
  @Override
  public String toString() {
    return "This is a module class that overrides Object methods";
  }
  
}
