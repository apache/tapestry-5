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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class Java10And11ConcreteService
{
    
    public Java10And11ConcreteService() throws Exception 
    {
        localVariableTypeInference();
    }

    /**
     * Tests Local Variable Type Inference.
     * Examples taken from https://docs.oracle.com/en/java/javase/17/language/local-variable-type-inference.html.
     */
    @SuppressWarnings("unused")
    public void localVariableTypeInference() throws Exception 
    {
        
        var url = new URL("http://www.oracle.com/"); 
        var conn = url.openConnection(); 
        var reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()));
        
        reader.close();
        
        
        var fileName = "LICENSE.txt";
        var list = new ArrayList<String>();    // infers ArrayList<String>
        var stream = list.stream();            // infers Stream<String>
        var path = Paths.get(fileName);        // infers Path
        var bytes = Files.readAllBytes(path);  // infers bytes[]

        List<String> myList = Arrays.asList("a", "b", "c");
        for (var element : myList) { element += " " ;}  // infers String
        
        for (var counter = 0; counter < 10; counter++) { Math.max(counter, counter); }   // infers int
        
        try (var input = 
                new FileInputStream(fileName)) {}   // infers FileInputStream
        
        BiFunction<Integer, Integer, Integer> biFunction = (a, b) -> a + b;
        biFunction = (var a, var b) -> a + b;
        
    }
    
}