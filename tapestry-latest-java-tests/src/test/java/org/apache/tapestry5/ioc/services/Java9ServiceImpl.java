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
package org.apache.tapestry5.ioc.services;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class Java9ServiceImpl implements Java9Service 
{
    
    public Java9ServiceImpl() 
    {
        diamondSyntaxAndAnonymousInnerClasses();
        tryWithResources();
        callSafeVarArgs();
    }
    
    /**
     * For testing More Concise try-with-resources Statements
     * https://docs.oracle.com/en/java/javase/17/language/java-language-changes.html#GUID-A920DB06-0FD1-4F9C-8A9A-15FC979D5DA3
     */
    public void tryWithResources()
    {
        Formatter formatter = new Formatter();
        try (formatter) 
        {
            formatter.format("nothing");
        }
    }
    
    public void callSafeVarArgs()
    {
        safeVarags("1");
    }
    
    /**
     * For testing @SafeVarargs Annotation Allowed on Proviate Instance Methods.
     * https://docs.oracle.com/en/java/javase/17/language/java-language-changes.html#GUID-015392DB-F5C4-4A8E-B190-E797707E7BFB
     */
    @SafeVarargs
    private void safeVarags(String...strings)
    {
        
    }
    
    /**
     * For testing Diamond Syntax and Anonymous Inner Classes
     * https://docs.oracle.com/en/java/javase/17/language/java-language-changes.html#GUID-0DF89F53-9232-44E3-80A4-48DD0C2CF359
     */
    private class Inner
    {
        
    }
    
    private void diamondSyntaxAndAnonymousInnerClasses() {
        List<Inner> list = new ArrayList<>();
        list.add(new Inner());
    }

}