package testsubjects;

import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformer;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.test.TestInject;

public class TestInjectTransformer<T> implements PlasticClassTransformer
{
    private final String className;

    private final T fieldValue;

    public TestInjectTransformer(Class<T> fieldType, T fieldValue)
    {
        // Limited; won't handle primitives or array types
        this.className = fieldType.getName();
        this.fieldValue = fieldValue;
    }

    @Override
    public void transform(PlasticClass plasticClass)
    {
        for (PlasticField f : plasticClass.getFieldsWithAnnotation(TestInject.class))
        {
            if (f.getTypeName().equals(className))
            {
                f.inject(fieldValue);

                f.claim(this);
            }
        }
    }
}
