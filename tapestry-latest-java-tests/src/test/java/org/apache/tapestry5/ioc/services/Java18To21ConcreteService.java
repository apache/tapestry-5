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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.SequencedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Java18To21ConcreteService
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Java18To21ConcreteService.class);

    /**
     * For testing SequencedCollection as a return type (JEP 431).
     * @see https://openjdk.org/jeps/431
     */
    public SequencedCollection<String> getSequencedCollection()
    {
        SequencedCollection<String> list = new ArrayList<>();
        list.add("first");
        list.add("middle");
        list.add("last");
        return list;
    }

    /**
     * For testing SequencedMap as a return type (JEP 431).
     * https://openjdk.org/jeps/431
     */
    public SequencedMap<String, Integer> getSequencedMap()
    {
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        return map;
    }

    /**
     * For testing record patterns in instanceof expressions (JEP 440).
     * @see https://openjdk.org/jeps/440
     */
    public void recordPatternsInInstanceOf()
    {
        IntExpression expr = new ConstantIntExpression(42);
        if (expr instanceof ConstantIntExpression(int v))
        {
            LOGGER.info("Constant int value: " + v);
        }

        IntTuple tuple = new IntTuple(3, 7);
        if (tuple instanceof IntTuple(int x, int y))
        {
            LOGGER.info("Tuple sum: " + (x + y));
        }
    }

    /**
     * For testing record patterns in switch expressions (JEP 440).
     * @see https://openjdk.org/jeps/440
     */
    public void recordPatternsInSwitch()
    {
        IntExpression expr = new PlusExpression(new ConstantIntExpression(2), new ConstantIntExpression(3));
        int result = evalIntExpression(expr);
        LOGGER.info("Switch record pattern result: " + result);

        IntExpression squareExpr = new SquareExpression(new ConstantIntExpression(5));
        LOGGER.info("Square result: " + evalIntExpression(squareExpr));
    }

    private int evalIntExpression(IntExpression e)
    {
        return switch (e)
        {
            case ConstantIntExpression(int v)        -> v;
            case PlusExpression(var left, var right) -> evalIntExpression(left) + evalIntExpression(right);
            case SquareExpression(var inner) ->
            {
                int v = evalIntExpression(inner); yield v * v;
            }
        };
    }
    /**
     * For testing pattern matching for switch with a sealed class hierarchy (JEP 441).
     * @see https://openjdk.org/jeps/441
     */
    public void patternMatchingForSwitchWithSealedClass()
    {
        for (Appliance appliance : new Appliance[] { new WashingMachine(), new Freezer(), new Refrigerator() })
        {
            String description = switch (appliance)
            {
                case WashingMachine wm -> "Washer: " + wm.getName();
                case Freezer f         -> "Freezer: " + f.getName();
                case Refrigerator r    -> "Fridge: " + r.getName();
                case CoolingMachine cm -> "Cooler: " + cm.getName();
            };
            LOGGER.info(description);
        }
    }

    /**
     * For testing pattern matching for switch with a sealed interface (JEP 441).
     * @see https://openjdk.org/jeps/441
     */
    public void patternMatchingForSwitchWithSealedInterface()
    {
        BinaryExpression expr = new AndExpression(
                new ConstantBinaryExpression(true),
                new OrExpression(new ConstantBinaryExpression(false), new ConstantBinaryExpression(true)));

        String description = switch (expr)
        {
            case ConstantBinaryExpression c -> "Constant: " + c.evaluate();
            case AndExpression a            -> "And: " + a.evaluate();
            case OrExpression o             -> "Or: " + o.evaluate();
        };
        LOGGER.info(description);
    }
}
